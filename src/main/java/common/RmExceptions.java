package common;

import java.lang.annotation.Annotation;

/**
 *
 * @author Ricardo Marquez
 */
public final class RmExceptions {

  /**
   *
   * @param format
   * @param objects
   */
  public static RuntimeException create(Exception ex, String format, Object... objects) {
    String message;
    if (objects == null || objects.length == 0) {
      message = format;
    } else {
      message = String.format(format, objects);
    }
    return new RuntimeException(message, ex);
  }

  /**
   * 
   * @param format
   * @param objects
   */
  public static RuntimeException create(String format, Object... objects) {
    String message;
    if (objects == null || objects.length == 0) {
      message = format;
    } else {
      message = String.format(format, objects);
    }
    return new RuntimeException(message);
  }

  /**
   *
   * @param format
   * @param objects
   */
  public static void throwException(String format, Object... objects) {
    String message;
    if (objects != null && objects.length != 0) {
      message = String.format(format, objects);
    } else {
      message = format;
    }
    throw new RuntimeException(message);
  }

  /**
   *
   * @param r
   */
  public static void runAndEncloseException(RunnableWithException r) {
    try {
      r.run();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   *
   * @param ex
   */
  public static void throwException(Exception ex) {
    throw new RuntimeException(ex);
  }

  /**
   *
   * @param ex
   */
  public static void throwException(Exception ex, String format, Object... objs) {
    String message = String.format(format, objs);
    throw new RuntimeException(message, ex);
  }

  /**
   *
   * @param ex
   */
  public static void throwException(Exception ex, String format) {
    throw new RuntimeException(ex);
  }

  /**
   *
   * @param aClass
   * @param aClass0
   * @return
   */
  public static void requiresDeclaredAnnotation( //
    Class<?> aClass, Class<? extends Annotation> aClass0) {
    if (!aClass.isAnnotationPresent(aClass0)) {
      String template = "Annotation '%s' is not present in class '%s'";
      RuntimeException result = RmExceptions.create(template, aClass0, aClass);
      throw result;
    }
  }

  public static interface RunnableWithException {

    public void run() throws Exception;
  }

  private RmExceptions() {
  }

}
