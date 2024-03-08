package common;

import javax.measure.Measure;
import javax.measure.quantity.Angle;
import javax.measure.unit.SI;

/**
 *
 * @author rmarq
 */
public class RmMath {
  
  
  public static double cos(Measure<Angle> angle) {
    return Math.cos(angle.doubleValue(SI.RADIAN)); 
  }
  
  public static double sin(Measure<Angle> angle) {
    return Math.sin(angle.doubleValue(SI.RADIAN)); 
  }
  
  public static double tan(Measure<Angle> angle) {
    return Math.tan(angle.doubleValue(SI.RADIAN)); 
  }
  
  
  public static Measure<Angle> atan(double number) {
    return Measure.valueOf(Math.atan(number), SI.RADIAN); 
  }
  
  public static Measure<Angle> acos(double number) {
    return Measure.valueOf(Math.acos(number), SI.RADIAN); 
  }
  
  public static Measure<Angle> asin(double number) {
    return Measure.valueOf(Math.asin(number), SI.RADIAN); 
  }
}
