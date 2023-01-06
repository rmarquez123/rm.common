package common;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.mutable.MutableObject;

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
   * @param s_v
   * @param k
   * @param v
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
    File result = new File(filename);
    if (!result.exists()) {
      RmExceptions.throwException("file '%s' does not exist.", filename);
    }
    return result;
  }
  
  /**
   * 
   * @param file
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
   * @return 
   */
  public static String fileExists(String file, String format, Object... args) {
    File f = fileExists(new File(file), format, args);
    String result = f.getAbsolutePath();
    return result; 
  }
}
