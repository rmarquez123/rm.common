package common.db;

import java.io.Serializable;

/**
 *
 * @author Ricardo Marquez
 */
public class Converters implements Serializable{
  
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
