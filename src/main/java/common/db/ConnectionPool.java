package common.db;

import java.io.Closeable;
import java.sql.Connection;
import java.util.Properties;

/**
 *
 * @author Ricardo Marquez
 */
public interface ConnectionPool extends Closeable {

  public Connection getConnection();
  
  public Connection getConnection(Application application);

  public String getConnectionUrl();
  
  /**
   * 
   * @param application
   * @return 
   */
  public String getConnectionUrl(Application application);

  public DbConnection parentDb();

  public Properties properties();

  public String getUrl();

  public Integer getPort();

  public String getDatabaseName();

  public String getUser();

  public String getPassword();
}
