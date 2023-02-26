package common.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.measure.Measure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKBReader;

/**
 *
 * @author Ricardo Marquez
 */
public class RmDbUtils {

  private static final Map<String, EntityManagerFactory> EMFS = new HashMap<>();

  /**
   *
   * @param conn
   */
  public static EntityManager createEntityManager(DbConnection conn) {
    HashMap<String, String> credentials = new HashMap<>();
    credentials.put("hibernate.connection.url", conn.getConnectionUrl());
    credentials.put("hibernate.connection.username", conn.getUser());
    credentials.put("hibernate.connection.password", conn.getPassword());
    EntityManager result = Persistence //
      .createEntityManagerFactory("wpls_idaho_power_pu", credentials)
      .createEntityManager();
    return result;
  }

  /**
   *
   * @param conn
   */
  public static EntityManager createEntityManager(DbConnection conn, String schema) {
    HashMap<String, String> credentials = new HashMap<>();
    credentials.put("hibernate.connection.url", conn.getConnectionUrl());
    credentials.put("hibernate.connection.username", conn.getUser());
    credentials.put("hibernate.connection.password", conn.getPassword());
    credentials.put("hibernate.connection.schema", schema);
    
    EntityManager result = Persistence //
      .createEntityManagerFactory("wpls_idaho_power_pu", credentials)
      .createEntityManager();
    return result;
  }
  
  
  /**
   *
   * @param conn
   */
  public static EntityManager createEntityManager(EntityManager em, String schema) {
    Map<String, Object> props = em.getEntityManagerFactory().getProperties();
    props.put("hibernate.connection.schema", schema);
    return em;
  }

  /**
   *
   */
  private RmDbUtils() {
  }

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
  public static EntityManager createEntityManager(String pu, // 
    String connUrl, String user, String password) {
    if (!EMFS.containsKey(pu)) {
      System.out.println("creating entity manager factory");
      EntityManagerFactory emf = Persistence.createEntityManagerFactory(pu);
      EMFS.put(pu, emf);
    }
    EntityManagerFactory emf = EMFS.get(pu);
    long curr = System.currentTimeMillis();
    HashMap<String, String> credentials = new HashMap<>();
    credentials.put("hibernate.connection.url", connUrl);
    credentials.put("hibernate.connection.username", user);
    credentials.put("hibernate.connection.password", password);

    EntityManager result = Persistence //
      .createEntityManagerFactory("wpls_idaho_power_pu", credentials)
      .createEntityManager();
    long elapsedMillis = System.currentTimeMillis() - curr;
    System.out.println( //
      String.format("Created entity manager in '%d' seconds", elapsedMillis));
    return result;
  }

  /**
   *
   * @param rs
   * @param datetimecolumn
   * @param zone
   * @return
   */
  public static ZonedDateTime getZonedDateTime( //
    ResultSet rs, String datetimecolumn, ZoneId zone) {
    ZonedDateTime dt;
    try {
      Timestamp timeStamp = rs.getTimestamp(datetimecolumn);
      dt = new Date(timeStamp.getTime()).
        toInstant().atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .atZone(zone);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    return dt;
  }

  /**
   *
   * @param <E>
   * @param rs
   * @param col
   * @param unit
   * @return
   */
  public static <E extends Quantity> Measure<E> // 
    doubleValue(ResultSet rs, String col, Unit<E> unit) {
    double aDouble;
    try {
      aDouble = rs.getDouble(col);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    Measure<E> result = Measure.valueOf(aDouble, unit);
    return result;
  }

  /**
   *
   * @param raster_id
   * @return
   */
  public static long longValue(ResultSet rs, String col) {
    long result;
    try {
      result = rs.getLong(col);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    return result;
  }

  /**
   *
   * @param <E>
   * @param rs
   * @param col
   * @param unit
   * @return
   */
  public static double doubleValue(ResultSet rs, String col) {
    double aDouble;
    try {
      aDouble = rs.getDouble(col);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    return aDouble;
  }

  /**
   *
   * @param <E>
   * @param rs
   * @param col
   * @param unit
   * @return
   */
  public static Point pointValue(ResultSet rs, String col, int srid) {
    Point result;
    try {
      GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
      WKBReader reader = new WKBReader(factory);
      Geometry geometry = reader.read(rs.getBytes(col));
      result = (Point) geometry;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return result;
  }

  /**
   *
   * @param rs
   * @param col
   * @return
   */
  public static int intValue(ResultSet rs, String col) {
    int result;
    try {
      result = rs.getInt(col);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    return result;
  }

  /**
   *
   * @param <E>
   * @param rs
   * @param col
   * @param unit
   * @return
   */
  public static <E extends Quantity> Measure<E> // 
    intValue(ResultSet rs, String col, Unit<E> unit) {
    int aDouble;
    try {
      aDouble = rs.getInt(col);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    Measure<E> result = Measure.valueOf(aDouble, unit);
    return result;
  }

  public static boolean booleanValue(ResultSet rs, String col) {
    try {
      return rs.getBoolean(col);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  
  public static String stringValue(ResultSet rs, String col) {
    try {
      return rs.getString(col);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static ZoneId zoneIdValue(ResultSet rs, String col) {
    try {
      return ZoneId.of(rs.getString(col));
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

}
