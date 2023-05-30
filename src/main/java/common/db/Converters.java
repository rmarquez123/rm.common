package common.db;

import java.io.Serializable;
import javax.measure.Measure;
import javax.measure.unit.SI;

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
    Measure.valueOf(0.1, SI.METRE); 
    String result;
    if (object instanceof Number) {
      result = String.valueOf(object);
    } else {
      result = "'" + String.valueOf(object) + "'";
    }
    return result;
  }
   
}
