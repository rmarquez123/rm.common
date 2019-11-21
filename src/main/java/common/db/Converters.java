package common.db;

/**
 *
 * @author Ricardo Marquez
 */
public class Converters {
  
  /**
   * 
   * @param object
   * @return 
   */
  public String convert(Object object) {
    String result;
    if (object instanceof Number) {
      result = String.valueOf(object);
    } else {
      result = "'" + String.valueOf(object) + "'";
    }
    return result;
  }
   
}
