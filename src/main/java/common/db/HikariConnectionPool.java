package common.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Ricardo Marquez
 */
public class HikariConnectionPool extends DefaultConnectionPool {

  private final HikariDataSource dataSource;

  public HikariConnectionPool(String url, Integer port, String databaseName, String user, String password) {
    super(user, password, databaseName, url, port);
    String jdbcUrl = "jdbc:postgresql://" + url + ":" + port + "/" + databaseName;
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(user);
    config.setPassword(password);
    config.setMaximumPoolSize(80);
    config.setLeakDetectionThreshold(2000); // 2 seconds
    config.setConnectionTimeout(30000); // 30 seconds
    config.setIdleTimeout(600000); // 10 minutes
    config.setMaxLifetime(600000); // 10 minutes
    config.setDriverClassName("org.postgresql.Driver");
    this.dataSource = new HikariDataSource(config);
  }

  /**
   *
   * @return
   */
  @Override
  public Connection getConnection() {
    try {
      return this.dataSource.getConnection();
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException {
    this.dataSource.close();
  }

  /**
   *
   */
  public static class PoolBuilder {

    private String url;
    private Integer port;
    private String databaseName;
    private String user;
    private String password;

    public PoolBuilder setUrl(String url) {
      this.url = url;
      return this;
    }

    public PoolBuilder setPort(Integer port) {
      this.port = port;
      return this;
    }

    public PoolBuilder setDatabaseName(String databaseName) {
      this.databaseName = databaseName;
      return this;
    }

    public PoolBuilder setUser(String user) {
      this.user = user;
      return this;
    }

    public PoolBuilder setPassword(String password) {
      this.password = password;
      return this;
    }

    public HikariConnectionPool build() {
      return new HikariConnectionPool(url, port, databaseName, user, password);
    }
  }

}
