package common;

import java.util.Objects;

/**
 *
 * @author Ricardo Marquez
 */
public class RmDelayedTask {
  
  /**
   * 
   */
  public static void create(long delayInMillis, Runnable runnable) {
    Objects.requireNonNull(runnable);
    new Thread(() -> {
      try {
        Thread.sleep(delayInMillis);
        runnable.run();
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }).start();
  }
}
