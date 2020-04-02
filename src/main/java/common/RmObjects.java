package common;

import java.util.function.Consumer;
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
}
