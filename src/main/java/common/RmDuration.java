package common;

import java.time.temporal.TemporalUnit;

/**
 *
 * @author Ricardo Marquez
 */
public class RmDuration {

  private final long count;

  private final TemporalUnit unit;
  
  /**
   *
   * @param count
   * @param unit
   */
  public RmDuration(long count, TemporalUnit unit) {
    this.count = count;
    this.unit = unit;
  }

  public long count() {
    return count;
  }
  
  public TemporalUnit unit() {
    return unit;
  }
  
  /**
   * 
   * @return 
   */
  @Override
  public String toString() {
    return "{" + "count=" + count + ", unit=" + unit + '}';
  }
  
  
  
}
