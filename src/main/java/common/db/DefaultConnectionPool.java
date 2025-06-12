package common.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Ricardo Marquez
 */
public class DefaultConnectionPool implements ConnectionPool {

  private final String user;
  private final String password;
  private final String databaseName;
  private final String url;
  private final Integer port;

  public DefaultConnectionPool(String user, String password, String databaseName, String url, Integer port) {
    this.user = user;
    this.password = password;
    this.databaseName = databaseName;
    this.url = url;
    this.port = port;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getDatabaseName() {
    return databaseName;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public Integer getPort() {
    return port;
  }

  @Override
  public Connection getConnection() {
    Connection result;
    try {
      String _url = this.getConnectionUrl();
      String _username = this.user;
      String _password = this.password;
      try {
        Class.forName("org.postgresql.Driver");
      } catch (ClassNotFoundException ex) {
        throw new RuntimeException(ex);
      }
      result = DriverManager.getConnection(_url, _username, _password);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    return result;
  }

  /**
   *
   * @param application
   * @return
   */
  @Override
  public Connection getConnection(Application application) {
    Connection result;
    try {
      String _url = this.getConnectionUrl(application);
      String _username = this.user;
      String _password = this.password;
      try {
        Class.forName("org.postgresql.Driver");
      } catch (ClassNotFoundException ex) {
        throw new RuntimeException(ex);
      }
      result = DriverManager.getConnection(_url, _username, _password);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    return result;
  }

  /**
   *
   * @return
   */
  @Override
  public DbConnection parentDb() {
    return new DbConnection(user, password, url, port);
  }

  @Override
  public Properties properties() {
    Properties result = new Properties();
    result.put("url", this.url);
    result.put("databaseName", this.databaseName);
    result.put("password", this.password);
    result.put("port", this.port);
    result.put("user", this.user);
    return result;
  }

  /**
   *
   * @return
   */
  @Override
  public String getConnectionUrl() {
    String sep = "//";
    String _url = "jdbc:postgresql:" + sep + this.url
            + ":" + this.port
            + "/" + this.databaseName;
    return _url;
  }

  /**
   *
   * @return
   */
  @Override
  public String getConnectionUrl(Application application) {
    String appName = application.getName();
    String sep = "//";
    String _url = "jdbc:postgresql:" + sep + this.url
            + ":" + this.port
            + "/" + this.databaseName
            + "?ApplicationName=" + appName;
    
    return _url;
  }

  /**
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException {
  }

}
