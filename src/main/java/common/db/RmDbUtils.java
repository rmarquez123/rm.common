package common.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author Ricardo Marquez
 */
public class RmDbUtils {

  private static Map<String, EntityManagerFactory> EMFS = new HashMap<>();

  /**
   * Creates a cached entity manager factory based on the persistence unit name. This is
   * analogous to eager initialization of an entity manger.
   *
   * @param pu
   */
  public static void installPu(String pu) {
    if (!EMFS.containsKey(pu)) {
      EntityManagerFactory emf = Persistence.createEntityManagerFactory(pu);
      EMFS.put(pu, emf);
    }
  }

  /**
   *
   * @param dbConnection
   */
  public static EntityManager createEntityManager(String pu, String connUrl, String user, String password) {
    if (!EMFS.containsKey(pu)) {
      EMFS.put(pu, Persistence.createEntityManagerFactory(pu));
    }
    EntityManagerFactory emf = EMFS.get(pu);
    long curr = System.currentTimeMillis();
    HashMap<String, String> credentials = new HashMap<String, String>() {
      {
        put("hibernate.connection.url", connUrl);
        put("hibernate.connection.username", user);
        put("hibernate.connection.password", password);
      }
    };
    EntityManager result = Persistence.createEntityManagerFactory("wpls_idaho_power_pu", credentials)
      .createEntityManager();
    System.out.println(String.format("Created entity manager in '%d' seconds", System.currentTimeMillis() - curr));
    return result;
  }

  /**
   *
   * @param rs
   * @param datetimecolumn
   * @param zone
   * @return
   */
  public static ZonedDateTime getZonedDateTime(ResultSet rs, String datetimecolumn, ZoneId zone) {
    Timestamp timeStamp;
    try {
      timeStamp = rs.getTimestamp(datetimecolumn);
    } catch (SQLException ex) {
      throw new RuntimeException(ex); 
    }
    ZonedDateTime dt = new Date(timeStamp.getTime()).
      toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
      .atZone(zone);
    return dt;
  }

  private RmDbUtils() {
  }
}
