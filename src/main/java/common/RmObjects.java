package common;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.mutable.MutableObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 *
 * @author Ricardo Marquez
 */
public class RmObjects {

  /**
   *
   * @param object
   * @param runnable
   */
  public static void ifNotNull(Object object, Runnable runnable) {
    if (object != null) {
      runnable.run();
    }
  }

  /**
   *
   * @param <T>
   * @param object
   * @param runnable
   */
  public static <T> void ifMutableObjectValueNotNull(MutableObject<T> object, Consumer<T> runnable) {
    if (object.getValue() != null) {
      runnable.accept(object.getValue());
    }
  }

  /**
   *
   * @param eps
   * @param x1
   * @param x2
   * @return
   */
  public static boolean doubleEquals(double eps, Double x1, Double x2) {
    if (x1 == null && x2 == null) {
      return true;
    }
    if (x1 != null && x2 == null) {
      return false;
    }
    if (x1 == null && x2 != null) {
      return false;
    }
    if (Double.isNaN(x1) && Double.isNaN(x2)) {
      return true;
    }
    if (Double.isInfinite(x1) && Double.isInfinite(x2)) {
      return true;
    }
    return Math.abs(x1 - x2) < eps;
  }

  /**
   *
   * @param text
   * @param expression
   * @return
   */
  public static boolean contains(String text, String expression) {
    Pattern p = Pattern.compile(expression);
    Matcher matcher = p.matcher(text);
    boolean result = matcher.matches();
    return result;
  }

  /**
   *
   * @param <T>
   * @param supplier
   * @return
   */
  public static <T> T execute(Supplier<T> supplier) {
    T result;
    try {
      result = supplier.get();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return result;
  }

  /**
   * System.out.println(String.format(s_v, args));
   *
   * @param s_v
   * @param args
   */
  public static void println(String s_v, Object... args) {
    System.out.println(String.format(s_v, args));
  }

  /**
   *
   * @param filename
   * @return
   */
  public static File fileExists(String filename) {
    File result = new File(filename.replaceAll("\\\\", File.separator));
    if (!result.exists()) {
      RmExceptions.throwException("file '%s' does not exist.", filename);
    }
    return result;
  }

  /**
   *
   * @param file
   * @param format
   * @param args
   * @return
   */
  public static File fileExists(File file, String format, Object... args) {
    if (!file.exists()) {
      RmExceptions.throwException(format, args);
    }
    return file;
  }

  /**
   *
   * @param file
   * @param format
   * @param args
   * @return
   */
  public static String fileExists(String file, String format, Object... args) {
    File f = fileExists(new File(file.replaceAll("\\\\", File.separator)), format, args);
    String result = f.getAbsolutePath();
    return result;
  }

  /**
   *
   * @param file
   */
  public static void createFileIfDoesNotExists(File file) {
    try {
      File parentFile = file.getParentFile();
      createDirectoryIfDoesNotExist(parentFile);
      if (!file.exists()) {
        file.createNewFile();
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static boolean createDirectoryIfDoesNotExist(File dir) {

    if (!dir.exists()) {
      String os = System.getProperty("os.name").toLowerCase();
      if (os.contains("win")) {
        dir.mkdirs();
      } else {
        try {
          Files.createDirectories(dir.toPath());
          Files.setPosixFilePermissions(dir.toPath(), // 
                  PosixFilePermissions.fromString("rwxrwxrwx"));
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    }
    boolean result = dir.exists() && dir.isDirectory();
    return result;
  }

  /**
   *
   * @return
   */
  public static boolean isWindows() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("win");
  }

  /**
   *
   * @param datetime
   * @param format
   * @param zoneId
   *
   * @return
   */
  public static String format(ZonedDateTime datetime, String format, ZoneId zoneId) {
    String result = datetime.toOffsetDateTime()
            .atZoneSameInstant(zoneId)
            .format(DateTimeFormatter.ofPattern(format));
    return result;
  }
  
  /**
   *
   * @param datetime
   * @param format
   *
   * @return
   */
  public static String formatUtc(ZonedDateTime datetime, String format) {
    ZoneId zoneId = ZoneId.of("UTC");
    String result = format(datetime, format, zoneId); 
    return result;
  }
  
  /**
   * 
   * @param format
   * @param datetimetext
   * @return 
   */
  public static ZonedDateTime dateTimeOfInUtc(String format, String datetimetext) {
    LocalDateTime datetime = LocalDateTime.parse(datetimetext, DateTimeFormatter.ofPattern(format));
    ZonedDateTime result = ZonedDateTime.of(datetime, ZoneId.of("UTC")); 
    return result;
  }

  /**
   * 
   * @param datetime
   * @return 
   */
  public static String formatUtcForDbStatement(ZonedDateTime datetime) {
    String format = "yyyy/MM/dd HH:mm"; 
    String dbformat = "yyyy/mm/dd HH24:mi";
    String datetimetext = formatUtc(datetime, format);
    String result = String.format("to_timestamp('%s', '%s')", //
            datetimetext, dbformat); 
    return result;
  }

  /**
   * 
   * @param lat
   * @param lon
   * @return 
   */
  public static Point pointWgs84(double lat, double lon) {
    PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FLOATING);
    GeometryFactory factory = new GeometryFactory(precisionModel, 4326);
    Coordinate coordinate = new Coordinate(lon, lat); 
    Point result = factory.createPoint(coordinate);
    return result;
  }
}
