package common.db;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
import org.apache.commons.lang3.Range;
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
   * @return
   */
  public static EntityManager createEntityManager(DbConnection conn) {
    HashMap<String, String> credentials = new HashMap<>();
    credentials.put("hibernate.connection.url", conn.getConnectionUrl());
    credentials.put("hibernate.connection.username", conn.getConnPool().getUser());
    credentials.put("hibernate.connection.password", conn.getConnPool().getPassword());
    EntityManager result = Persistence //
            .createEntityManagerFactory("wpls_idaho_power_pu", credentials)
            .createEntityManager();
    return result;
  }

  /**
   *
   * @param conn
   * @param schema
   * @return
   */
  public static EntityManager createEntityManager(DbConnection conn, String schema) {
    HashMap<String, String> credentials = new HashMap<>();
    credentials.put("hibernate.connection.url", conn.getConnectionUrl());
    credentials.put("hibernate.connection.username", conn.getConnPool().getUser());
    credentials.put("hibernate.connection.password", conn.getConnPool().getPassword());
    credentials.put("hibernate.connection.schema", schema);

    EntityManager result = Persistence //
            .createEntityManagerFactory("wpls_idaho_power_pu", credentials)
            .createEntityManager();
    return result;
  }

  /**
   *
   * @param em
   * @param schema
   * @return
   */
  public static EntityManager createEntityManager(EntityManager em, String schema) {
    Map<String, Object> props = em.getEntityManagerFactory().getProperties();
    props.put("hibernate.connection.schema", schema);
    return em;
  }

  /**
   *
   * @param resultSet
   * @param name
   * @return
   */
  public static Object stringValueDecoded(ResultSet resultSet, String name) {
    try {
      return URLDecoder.decode(stringValue(resultSet, name), "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   *
   * @param rs
   * @param lowerleft
   * @param upperright
   * @return
   */
  public static Range<Double> doubleValueRange(ResultSet rs, String lowerleft, String upperright) {
    double min = RmDbUtils.doubleValue(rs, lowerleft);
    double max = RmDbUtils.doubleValue(rs, upperright);
    Range<Double> result = Range.between(min, max);
    return result;
  }

  /**
   *
   */
  private RmDbUtils() {
  }

  /**
   * Creates a cached entity manager factory based on the persistence unit name.
   * This is analogous to eager initialization of an entity manger.
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
   * @param pu
   * @param connUrl
   * @param user
   * @param password
   * @return
   */
  public static EntityManager createEntityManager(String pu, // 
          String connUrl, String user, String password) {

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
    Measure< E> result = Measure.valueOf(aDouble, unit);
    return result;
  }

  /**
   *
   * @param rs
   * @param col
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
   * @param rs
   * @param col
   * @param srid
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
   * @param srid
   * @return
   */
  public static Geometry geometryValue(ResultSet rs, String col, int srid) {
    Geometry result;
    try {
      GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
      WKBReader reader = new WKBReader(factory);
      Geometry geometry = reader.read(rs.getBytes(col));

      result = geometry;
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
  public static <E extends Quantity> Measure< E> // 
          intValue(ResultSet rs, String col, Unit<E> unit) {
    double aDouble;
    try {
      aDouble = new Integer(rs.getInt(col)).doubleValue();
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    Measure< E> result = Measure.valueOf(aDouble, unit);
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
