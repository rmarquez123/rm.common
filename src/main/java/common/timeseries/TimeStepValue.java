package common.timeseries;

import java.time.ZonedDateTime;

/**
 * A class associated with a {@linkplain TimeSeries} for the purpose of representing
 * values at time steps. This class holds a reference to the actual time step and the
 * value object with generic parameter <code>R</code>.
 *
 * @param R the type of record the time step value holds.
 * @author Ricardo Marquez
 *
 */
public interface TimeStepValue<R> extends Comparable<TimeStepValue<R>> {

  /**
   *
   * @return
   */
  public R getUserObject();

  /**
   *
   * @return
   */
  public ZonedDateTime getZoneDateTime();
}
