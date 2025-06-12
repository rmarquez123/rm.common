package common.db;

import common.process.ProcessFacade;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.Pair;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;

/**
 *
 * @author rmarquez
 */
public class DbConnection implements Serializable {

  private static final int DEFAULT_FETCHSIZE = 0;

  private Connection connection;
  private final ConnectionPool connPool;
  private final Converters converters;

  public DbConnection(String user, String password, String databaseName, String url, Integer port) {
    this.connPool = new DefaultConnectionPool(user, password, databaseName, url, port);
    this.converters = new Converters();
  }

  public DbConnection(ConnectionPool connPool) {
    this.connPool = connPool;
    this.converters = new Converters();
  }

  /**
   *
   * @return
   */
  public ConnectionPool getConnPool() {
    return connPool;
  }

  /**
   * The parent database. <code> new DbConnection(user, password, url, port);
   * </code>
   *
   * @return
   */
  public DbConnection parentDb() {
    return connPool.parentDb();
  }

  /**
   *
   * @param user
   * @param password
   * @param url
   * @param port
   */
  public DbConnection(String user, String password, String url, Integer port) {
    this(user, password, "postgres", url, port);
  }

  /**
   *
   * @param table
   * @param record
   * @return
   */
  public int executeInsert(String table, RecordValue record) {
    Set<String> keySet = record.keySetNoPk();
    List<Object> values = new ArrayList<>(record.valueSetNoPk());
    String separator = ", ";
    String columns = String.join(separator, keySet);
    String valuePlaceHolders = StringUtils.repeat("?", separator, values.size());
    String sql = String.format("insert into %s (%s) values (%s)",
            new Object[]{table, columns, valuePlaceHolders});
    int effectedRows;
    Connection conn = this.getConnection();
    PreparedStatement statement;
    try {
      statement = conn.prepareStatement(sql);
      this.setParamValues(values, statement);
      effectedRows = statement.executeUpdate();
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    } finally {
      try {
        conn.close();
      } catch (SQLException ex) {
        Logger.getLogger(DbConnection.class.getName())
                .log(Level.SEVERE, "An error occurred while closing the connection. ", ex);
      }
    }
    return effectedRows;
  }

  /**
   *
   */
  public void close() {
    try {
      this.connPool.close();
    } catch (IOException ex) {
      Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   *
   * @param <T>
   * @param sql
   * @param mapper
   * @return
   */
  public <T> T executeSingleResultQuery(String sql, ResultMapper<T> mapper) {
    List<T> list = this.executeQuery(sql, mapper);
    if (list.size() > 1) {
      throw new RuntimeException("Query returned more than 1 result.");
    }
    T result = list.get(0);
    return result;
  }

  /**
   *
   * @param <T>
   * @param sql
   * @param column
   * @param clazz
   * @return
   */
  public <T> T executeSingleResultQuery(String sql, String column, Class<T> clazz) {
    List<T> list = this.executeQuery(sql, (ResultSet rs) -> {
      try {
        return (T) rs.getObject(column);
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
    });
    if (list.size() > 1) {
      throw new RuntimeException("Query returned more than 1 result.");
    }
    T result = list.isEmpty() ? null : list.get(0);
    return result;
  }

  /**
   *
   * @param <T>
   * @param sql
   * @param mapper
   * @return
   */
  public <T> List<T> executeQuery(String sql, ResultMapper<T> mapper) {
    List<T> result = new ArrayList<>();
    this.executeQuery(sql, (rs) -> {
      result.add(mapper.map(rs));
    });
    return result;
  }

  /**
   *
   * @param sql
   * @param consumer
   */
  public void executeQuery(String sql, Consumer<ResultSet> consumer) {
    this.executeQuery(null, sql, DEFAULT_FETCHSIZE, consumer);
  }

  /**
   *
   * @param application
   * @param sql
   * @param consumer
   */
  public void executeQuery(Application application, String sql, Consumer<ResultSet> consumer) {
    this.executeQuery(application, sql, DEFAULT_FETCHSIZE, consumer);
  }

  /**
   *
   * @param <T>
   * @param sql
   * @param consumer
   * @return
   */
  public <T> T executeQuerySingleResult(String sql, Function<ResultSet, T> consumer) {
    MutableObject<T> obj = new MutableObject<>(null);
    this.executeQuery(null, sql, DEFAULT_FETCHSIZE, (rs) -> {
      obj.setValue(consumer.apply(rs));
    });
    return obj.getValue();
  }

  /**
   *
   * @param <T>
   * @param application
   * @param sql
   * @param consumer
   * @return
   */
  public <T> T executeQuerySingleResult(Application application, String sql, Function<ResultSet, T> consumer) {
    MutableObject<T> obj = new MutableObject<>(null);
    this.executeQuery(null, sql, DEFAULT_FETCHSIZE, (rs) -> {
      obj.setValue(consumer.apply(rs));
    });
    return obj.getValue();
  }

  /**
   *
   * @param application
   * @param sql
   * @param fetchSize
   * @param consumer
   */
  public void executeQuery(Application application, String sql, int fetchSize, Consumer<ResultSet> consumer) {
    Connection conn = this.getConnection();

    try (PreparedStatement statement = conn.prepareCall(sql)) {
      statement.setFetchSize(fetchSize);
      conn.setAutoCommit(false);
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        consumer.accept(resultSet);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    } finally {
      try {
        conn.close();
      } catch (SQLException ex) {
        Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + Objects.hashCode(this.connPool);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DbConnection other = (DbConnection) obj;
    if (!Objects.equals(this.connPool, other.connPool)) {
      return false;
    }
    return true;
  }

  /**
   *
   * @return
   */
  @Override
  public String toString() {
    return this.getConnectionUrl();
  }

  /**
   * Returns a new connection.
   *
   * @return
   */
  public Connection getConnection() {
    return this.connPool.getConnection();
  }

  /**
   * Returns a new connection.
   *
   * @param application
   * @return
   */
  public Connection getConnection(Application application) {
    return this.connPool.getConnection(application);
  }

  /**
   *
   * @return
   */
  public String getConnectionUrl() {
    return this.connPool.getConnectionUrl();
  }

  /**
   *
   * @param connUrl
   * @param user
   * @param password
   * @return
   */
  public static DbConnection create(String connUrl, String user, String password) {
    String[] parts = connUrl.replace("jdbc:postgresql://", "").replace(":", ",").replace("/", ",").split(",");
    String url = parts[0];
    String database = parts[2];
    Integer port = Integer.valueOf(parts[1]);
    DbConnection result = new DbConnection.Builder()
            .setUrl(url)
            .setPort(port)
            .setDatabaseName(database)
            .setUser(user)
            .setPassword(password)
            .createDbConnection();
    return result;
  }

  /**
   *
   * @return
   */
  public Exception test() {
    Exception result;
    try {
      this.connection = this.getConnection();
      this.connection.close();
      this.connection = null;
      result = null;
    } catch (SQLException ex) {
      result = ex;
    } catch (Exception ex) {
      result = ex;
    }
    return result;
  }

  /**
   *
   * @param tablename
   * @return
   */
  public Exception tableExists(String tablename) {
    MutableObject<Exception> result = new MutableObject<>(null);
    String schema;
    String table_name;
    if (!tablename.contains(".")) {
      schema = "public";
      table_name = tablename;
    } else {
      String[] parts = tablename.split("\\.");
      schema = parts[0];
      table_name = parts[1];
    }
    String query = "SELECT EXISTS (\n"
            + "   SELECT 1\n"
            + "   FROM   information_schema.tables \n"
            + "   WHERE  table_schema = '" + schema + "'\n"
            + "   AND    table_name = '" + table_name + "'\n"
            + "   );";
    this.executeQuery(query, (r) -> {
      try {
        boolean tableExists = r.getBoolean(1);
        if (!tableExists) {
          result.setValue(new Exception("Table '" + tablename + "' does not exist."));
        }
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    });
    return result.getValue();
  }

  /**
   *
   * @param table
   * @param record
   * @param pk
   * @return
   */
  public int executeUpsert(String table, RecordValue record, String... pk) {
    Objects.requireNonNull(pk, "Primary key field name cannot be null");
    Objects.requireNonNull(record, "Record cannot be null");
    Objects.requireNonNull(table, "table cannot be null");
    if (pk.length == 0) {
      throw new IllegalArgumentException("No primary key field names specified cannot be empty");
    }
    if (table.trim().isEmpty()) {
      throw new IllegalArgumentException("table string cannot be empty");
    }
    Set<String> keySet = record.keySetNoPk();
    List<Object> values = new ArrayList<>(record.valueSetNoPk());
    String separator = ", ";
    String columns = String.join(separator, keySet);
    String valuePlaceHolders = StringUtils.repeat("?", separator, values.size());
    HashSet<String> keySetNoPk = new HashSet<>(keySet);
    keySetNoPk.removeIf((k) -> Arrays.asList(pk).contains(k));
    String columns_no_pk = String.join(separator, keySetNoPk);
    List<Object> values_no_pk = record.valueSet(keySetNoPk);
    String valuePlaceHoldersNoPk = StringUtils.repeat("?", separator, values_no_pk.size());

    String sql;
    if (valuePlaceHoldersNoPk.length() == 1) {
      sql = String.format("insert into %s \n(%s) \n values (%s) \n"
              + " on conflict (%s) do update\n "
              + " set %s = ? ",
              new Object[]{table,
                columns,
                valuePlaceHolders,
                String.join(",", pk),
                keySetNoPk.iterator().next(),
                values_no_pk.get(0)
              }
      );
    } else {
      sql = String.format("insert into %s \n(%s) \n values (%s) \n"
              + " on conflict (%s) do update\n "
              + " set (%s) = (%s) ",
              new Object[]{table,
                columns,
                valuePlaceHolders,
                String.join(",", pk),
                columns_no_pk
              }
      );
    }

    Connection conn = this.getConnection();
    int effectedRows;
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      List<Object> allValues = new ArrayList<>();
      allValues.addAll(values);
      allValues.addAll(values_no_pk);
      this.setParamValues(allValues, statement);
      effectedRows = statement.executeUpdate();
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    } finally {
      try {
        conn.close();
      } catch (SQLException ex) {
        Logger.getLogger(DbConnection.class.getName())
                .log(Level.SEVERE, "An error occurred while closing the connection. ", ex);
      }
    }
    return effectedRows;
  }

  /**
   *
   * @param table
   * @param records
   * @param pk
   * @return
   */
  public int executeUpsert(String table, List<RecordValue> records, String... pk) {
    Objects.requireNonNull(pk, "Primary key field name cannot be null");
    Objects.requireNonNull(records, "Records cannot be null");
    Objects.requireNonNull(table, "table cannot be null");
    if (pk.length == 0) {
      throw new IllegalArgumentException("No primary key field names specified cannot be empty");
    }
    if (table.trim().isEmpty()) {
      throw new IllegalArgumentException("table string cannot be empty");
    }
    int effectedRows;
    if (!records.isEmpty()) {
      Set<String> keySet = records.get(0).keySet();
      String separator = ", ";
      String columns = String.join(separator, keySet);
      String valuePlaceHolders = records.stream()
              .map(
                      (r) -> keySet
                              .stream()
                              .map((k) -> this.converters.convert(r.get(k)))
                              .collect(Collectors.joining(","))
              ).collect(Collectors.joining(")\n" + separator + "("));
      Set<String> keySetNoPk = records.get(0).keySetNoPk();
      List<String> keySetNoPkWithExcludePrefix = keySetNoPk
              .stream()
              .map((e) -> "excluded." + e)
              .collect(Collectors.toList());
      String columns_no_pk = String.join(separator, keySetNoPkWithExcludePrefix);

      String sql = String.format("insert into %s \n(%s) \n values (%s) \n"
              + " on conflict (%s) do update\n ",
              new Object[]{
                table,
                columns,
                valuePlaceHolders,
                String.join(",", pk)
              });
      String set;
      if (keySetNoPkWithExcludePrefix.size() < 2) {
        set = String.format(" set %s = %s",
                String.join(",", keySetNoPk),
                columns_no_pk);
      } else {
        set = String.format(" set (%s) = (%s)",
                String.join(",", keySetNoPk),
                columns_no_pk);
      }
      sql = sql + "\n " + set;
      Connection conn = this.getConnection();
      try {
        PreparedStatement statement = conn.prepareStatement(sql);
        effectedRows = statement.executeUpdate();
      } catch (SQLException ex) {
        throw new RuntimeException(
                String.format("Error running statement: '%s'", sql), // 
                ex);
      } finally {
        try {
          conn.close();
        } catch (SQLException ex) {
          Logger.getLogger(DbConnection.class.getName())
                  .log(Level.SEVERE, "An error occurred while closing the connection. ", ex);
        }
      }
    } else {
      effectedRows = 0;
    }
    return effectedRows;
  }

  /**
   *
   * @param values
   * @param statement
   * @throws SQLException
   */
  private void setParamValues(List<Object> values, PreparedStatement statement) throws SQLException {
    for (int columnIndex = 1; columnIndex <= values.size(); columnIndex++) {
      Object objectValue = values.get(columnIndex - 1);
      if (objectValue instanceof ZonedDateTime) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(((ZonedDateTime) objectValue).getZone()));
        ZonedDateTime withZoneSameInstant = ((ZonedDateTime) objectValue);
        long epochMilli = withZoneSameInstant.toInstant()
                .toEpochMilli();
        Timestamp p = new Timestamp(epochMilli);
        statement.setTimestamp(columnIndex, p, cal);
      } else {
        statement.setObject(columnIndex, objectValue);
      }
    }
  }

  /**
   * *
   *
   * @return
   */
  public Properties properties() {
    return this.connPool.properties();
  }

  /**
   * Creates a db connection from properties object. The properties object
   * contains the keys:
   * <ul>
   * <li> url(String)</li>
   * <li> port(int)</li>
   * <li> databaseName(String)</li>
   * <li> user(String)</li>
   * <li> password(String)</li>
   * </ul>
   *
   * @param p
   * @return
   */
  public static DbConnection fromProperties(Properties p) {
    String portKey = "port";
    Object portVal = p.get(portKey);
    if (portVal == null) {
      throw new NullPointerException("port is not defined");
    }
    int port = (portVal instanceof String)
            ? Integer.parseInt((String) portVal)
            : (Integer) portVal;
    DbConnection createDbConnection = new DbConnection.Builder()
            .setUrl(p.getProperty("url"))
            .setDatabaseName(p.getProperty("databaseName"))
            .setPassword(p.getProperty("password"))
            .setPort(port)
            .setUser(p.getProperty("user"))
            .createDbConnection();
    return createDbConnection;
  }

  /**
   *
   * @param statement
   * @return
   */
  public synchronized int executeStatement(String statement) {
    int result;
    try (Connection conn = this.getConnection()) {
      PreparedStatement preparedStatement;
      try {
        preparedStatement = conn.prepareStatement(statement);
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
      try {
        result = preparedStatement.executeUpdate();
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return result;
  }
  
  /**
   * 
   * @param application
   * @param statement
   * @return 
   */
  public synchronized int executeStatement(Application application, String statement) {
    int result;
    try (Connection conn = this.getConnection(application)) {
      PreparedStatement preparedStatement;
      try {
        preparedStatement = conn.prepareStatement(statement);
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
      try {
        result = preparedStatement.executeUpdate();
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return result;
  }

  /**
   *
   * @param <R>
   * @param application
   * @param statementTemplate
   * @param records
   * @param updateMe
   * @return
   */
  public <R> int[] executeStatementsBatch(Application application,
          String statementTemplate, List<R> records, Consumer<Pair<PreparedStatement, R>> updateMe) {
    Connection conn = this.getConnection(application);
    return executeStatementsBatch(conn, statementTemplate, records, updateMe);
  }

  /**
   *
   * @param <R>
   * @param statementTemplate
   * @param records
   * @param updateMe
   * @return
   */
  public <R> int[] executeStatementsBatch(
          String statementTemplate, List<R> records, Consumer<Pair<PreparedStatement, R>> updateMe) {
    Connection conn = this.getConnection();
    return executeStatementsBatch(conn, statementTemplate, records, updateMe);
  }

  /**
   *
   * @param <R>
   * @param preStatement
   * @param postStatement
   * @param statementTemplate
   * @param records
   * @param updateMe
   * @return
   */
  public <R> int[] executeStatementsBatch(String preStatement, String postStatement,
          String statementTemplate, List<R> records, Consumer<Pair<PreparedStatement, R>> updateMe) {

    Connection conn = this.getConnection();

    int[] result;

    try (PreparedStatement statement = conn.prepareStatement(statementTemplate)) {

      conn.setAutoCommit(false);

      try (Statement stmt = conn.createStatement()) {
        stmt.execute(preStatement);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }

      for (R record : records) {
        updateMe.accept(Pair.of(statement, record));
        statement.addBatch();
      }
      result = statement.executeBatch();
      try (Statement stmt = conn.createStatement()) {
        stmt.execute(postStatement);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
      try {
        conn.commit();
      } catch (SQLException ex) {
        throw new RuntimeException();
      }
    } catch (SQLException ex) {
      try {
        conn.rollback();
      } catch (SQLException rollbackEx) {
        throw new RuntimeException("Rollback failed: " + rollbackEx.getMessage(), rollbackEx);
      }
      throw new RuntimeException("Batch execution failed: " + ex.getMessage(), ex);
    } finally {
      try {
        conn.close();
      } catch (SQLException ex) {
        throw new RuntimeException("Failed to close connection: " + ex.getMessage(), ex);
      }
    }

    return result;
  }

  /**
   * Executes: 1) preStatement (e.g. CREATE TEMP TABLE ...), 2) copy data into
   * the table (using COPY FROM STDIN), 3) postStatement (e.g. UPDATE main_table
   * FROM temp_table).
   *
   * @param preStatement a SQL statement to run before COPY (may be DDL)
   * @param postStatement a SQL statement to run after COPY (may be an UPDATE)
   * @param copyTarget the COPY command target (e.g. "temp_table (col1, col2,
   * ...)")
   * @param records the data objects you want to copy
   * @param recordToCsvLine a function that converts each record into a CSV line
   * (no trailing newline)
   * @return long[] with [0] = the count of rows copied, [1] = any other measure
   * (you can refine)
   */
  public <R> long[] executeDirectCopyToTable(
          String preStatement,
          String postStatement,
          String copyTarget,
          List<R> records,
          Function<R, String> recordToCsvLine
  ) {
    // We'll return how many rows got copied, possibly more details if you like
    long[] result = new long[2];
    Connection conn = this.getConnection();    
    try {
      conn.setAutoCommit(false);

      // 1. Execute preStatement (e.g. create temp table)
      try (Statement stmt = conn.createStatement()) {
        stmt.execute(preStatement);
      }
      
      // 2. Prepare CSV data in memory
      StringBuilder sb = new StringBuilder();
      for (R record : records) {
        sb.append(recordToCsvLine.apply(record)).append("\n");
      }
      byte[] csvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
      ByteArrayInputStream bais = new ByteArrayInputStream(csvBytes);
      
      // 3. Use CopyManager to COPY the data into the table
      PGConnection pgConn = conn.unwrap(PGConnection.class);
      CopyManager copyManager = pgConn.getCopyAPI();
      // e.g. "COPY temp_table (col1, col2, col3) FROM STDIN (FORMAT csv)"
      String copySql = "COPY " + copyTarget + " FROM STDIN (FORMAT csv)";
      
      // do the copy
      long rowCount = copyManager.copyIn(copySql, bais);
      result[0] = rowCount;  // number of rows copied
      
      // 4. Execute postStatement (e.g. update the main table from the temp table)
      try (Statement stmt = conn.createStatement()) {
        stmt.execute(postStatement);
      }
      
      // Commit everything
      conn.commit();
    } catch (Exception ex) {
      // Rollback on any error
      try {
        conn.rollback();
      } catch (SQLException rollbackEx) {
        throw new RuntimeException("Rollback failed: " + rollbackEx.getMessage(), rollbackEx);
      }
      throw new RuntimeException("Direct COPY failed: " + ex.getMessage(), ex);
    } finally {
      // Close connection
      try {
        conn.close();
      } catch (SQLException ex) {
        throw new RuntimeException("Failed to close connection: " + ex.getMessage(), ex);
      }
    }
    return result;
  }

  /**
   *
   * @param <R>
   * @param conn
   * @param statementTemplate
   * @param records
   * @param updateMe
   * @param result
   * @return
   * @throws RuntimeException
   */
  private <R> int[] executeStatementsBatch(Connection conn,
          String statementTemplate, List<R> records, Consumer<Pair<PreparedStatement, R>> updateMe) {
    int[] result;

    try (PreparedStatement statement = conn.prepareStatement(statementTemplate)) {
      conn.setAutoCommit(false);
      for (R record : records) {
        updateMe.accept(Pair.of(statement, record));
        statement.addBatch();
      }
      result = statement.executeBatch();
      conn.commit();
    } catch (SQLException ex) {
      try {
        conn.rollback();
      } catch (SQLException rollbackEx) {
        throw new RuntimeException("Rollback failed: " + rollbackEx.getMessage(), rollbackEx);
      }
      throw new RuntimeException("Batch execution failed: " + ex.getMessage(), ex);
    } finally {
      try {
        conn.close();
      } catch (SQLException ex) {
        throw new RuntimeException("Failed to close connection: " + ex.getMessage(), ex);
      }
    }
    return result;
  }

  /**
   *
   * @param statements
   * @return
   */
  public int[] executeStatements(String... statements) {
    int[] result = new int[statements.length];
    int i = -1;
    Connection conn = this.getConnection();
    try {
      conn.setAutoCommit(false);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    for (String statementText : statements) {
      i++;
      PreparedStatement statement;
      try {
        statement = conn.prepareStatement(statementText.replace("NaN", "null"));
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
      try {
        result[i] = statement.executeUpdate();
      } catch (SQLException ex) {
        throw new RuntimeException("Error on statement: " + statementText, ex);
      }
    }
    try {
      conn.commit();
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    try {
      conn.close();
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    return result;
  }

  /**
   *
   * @param dbBinDir
   * @param schemaSqlFile
   * @param outputConsumer
   */
  public void runSqlFile(File dbBinDir, File schemaSqlFile, Consumer<String> outputConsumer) {
    String statement // 
            = String.format("cmd.exe /c %s\\psql.exe -h %s -d %s -U %s -p %d -a -q -f \"%s\"",//
                    dbBinDir, //
                    this.connPool.getUrl(), this.connPool.getDatabaseName(), // 
                    this.connPool.getUser(), this.connPool.getPort(), schemaSqlFile.getAbsolutePath());
    String _password = this.connPool.getPassword();

    HashMap<String, String> hashMap = new HashMap<>();
    hashMap.put("PGPASSWORD", _password);
    new ProcessFacade.Builder()
            .withStatement(statement)
            .withEnvironmentVars(hashMap)
            .withOutputProcessor(outputConsumer)
            .run();
  }

  /**
   *
   * @param postGresBin
   * @param dataFolder
   */
  public void startDb(File postGresBin, File dataFolder) {
    this.startDb(postGresBin, dataFolder, System.out::println);
  }

  /**
   *
   * @param postGresBin
   * @param dataFolder
   * @param consumer
   */
  public void startDb(File postGresBin, File dataFolder, Consumer<String> consumer) {
    String statement // 
            = String.format("%s\\pg_ctl.exe restart -D \"%s\" -o \"--port=%d\" ", // 
                    postGresBin, dataFolder, this.connPool.getPort());

    MutableObject<Boolean> done = new MutableObject<>(false);
    new ProcessFacade.Builder()
            .withStatement(statement)
            .withOutputProcessor((s) -> {
              if (s.contains("server started")) {
                done.setValue(Boolean.TRUE);
              }
              if (s.contains("could not start server")) {
                done.setValue(Boolean.TRUE);
              }
              consumer.accept(s);
            })
            .runOnNewThread(() -> {
              done.setValue(Boolean.TRUE);
            });
    while (!done.getValue()) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  /**
   *
   * @param postGresBin
   * @param dataFolder
   */
  public void stopDb(File postGresBin, File dataFolder) {
    this.stopDb(postGresBin, dataFolder, System.out::println);
  }

  /**
   *
   * @param postGresBin
   * @param dataFolder
   * @param consumer
   */
  public void stopDb(File postGresBin, File dataFolder, Consumer<String> consumer) {
    String statement // 
            = String.format("%s\\pg_ctl.exe stop -D \"%s\"", //
                    postGresBin, dataFolder);
    new ProcessFacade.Builder()
            .withStatement(statement)
            .withOutputProcessor(consumer)
            .run();
  }

  /**
   *
   * @param column
   * @param table
   * @return
   */
  public int getNextSequence(String column, String table) {
    String query = String.format("SELECT COALESCE(MAX(%s), 0) as column FROM %s", column, table);
    int result = this.executeSingleResultQuery(query, "column", Integer.class) + 1;
    return result;
  }

  /**
   *
   * @param column
   * @param table
   * @return
   */
  public long getNextSequenceLong(String column, String table) {
    String query = String.format("SELECT COALESCE(MAX(%s), 0) as column FROM %s", column, table);
    long result = this.executeSingleResultQuery(query, "column", Long.class) + 1L;
    return result;
  }

  /**
   *
   * @param <T>
   * @param column
   * @param table
   * @param clazz
   * @return
   */
  public <T extends Number> T getLastSequence(String column, String table, Class<T> clazz) {
    String query = String.format("SELECT COALESCE(MAX(%s), 0) as column FROM %s", column, table);
    T result = this.executeSingleResultQuery(query, "column", clazz);
    return result;
  }

  /**
   *
   */
  public static class Builder {

    private String user;
    private String password;
    private String databaseName;
    private String url;
    private Integer port;

    public Builder() {
    }

    public Builder setUser(String user) {
      this.user = user;
      return this;
    }

    public Builder setPassword(String password) {
      this.password = password;
      return this;
    }

    public Builder setDatabaseName(String databaseName) {
      this.databaseName = databaseName;
      return this;
    }

    public Builder setUrl(String url) {
      this.url = url;
      return this;
    }

    public Builder setPort(Integer port) {
      this.port = port;
      return this;
    }

    public DbConnection createDbConnection() {
      Objects.requireNonNull(this.user, "user cannot be null");
      Objects.requireNonNull(this.password, "password cannot be null");
      Objects.requireNonNull(this.databaseName, "databaseName cannot be null");
      Objects.requireNonNull(this.url, "url cannot be null");
      Objects.requireNonNull(this.port, "port cannot be null");
      return new DbConnection(this.user, this.password, this.databaseName, this.url, this.port);
    }
  }
}
