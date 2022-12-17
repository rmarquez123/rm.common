package common;

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
  public static void throwException(String format, Object... objects) {
    String message; 
    if (objects == null || objects.length == 0) {
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
  public static void doIt(RunnableWithException r) {
    try {
      r.run();
    } catch(Exception ex) {
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
    throw new RuntimeException(ex); 
  }
  
  /**
   * 
   * @param ex 
   */
  public static void throwException(Exception ex, String format) {
    throw new RuntimeException(ex); 
  }
  
  public static interface RunnableWithException {
    public void run() throws Exception; 
  }
  
  private RmExceptions() {
  }
  
}
