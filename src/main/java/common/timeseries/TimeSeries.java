package common.timeseries;

import common.types.DateRange;
import common.types.DateTimeRange;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.math3.util.Pair;

/**
 * Time series class for common use of time series data structures. This data structure
 * may be used in various program implementations including object relational mapping,
 * user interfaces, and analysis.
 *
 * @author Ricardo Marquez
 * @param <TS>
 */
public interface TimeSeries<TS extends TimeStepValue<?>> extends Iterable<TS> {

  /**
   *
   * @return
   */
  public ZoneId getZoneId();

  /**
   * *
   *
   * @return
   */
  public int size();

  /**
   * *
   *
   * @param timeStepValue
   * @return
   */
  public boolean contains(TS timeStepValue);

  /**
   * *
   *
   * @param zonedDateTime
   * @return
   */
  public boolean contains(ZonedDateTime zonedDateTime);

  /**
   *
   * @return
   */
  public TS getFirst();

  /**
   *
   * @return
   */
  public TS getLast();

  /**
   *
   * @param previous
   * @return
   */
  public TS getNext(TS previous);

  /**
   *
   * @param previous
   * @return
   */
  public TS getPrevious(TS after);

  /**
   *
   * @return
   */
  public boolean isRegularTimeIntervaled();

  /**
   *
   * @return
   */
  public TemporalAmount getTimeInterval();

  /**
   *
   * @param dateTime
   * @return
   */
  public TS getTimeStepValue(ZonedDateTime dateTime);

  /**
   *
   * @return
   */
  public DateTimeRange getDateTimeRange();

  /**
   *
   * @return
   */
  public DateRange getDateRange();

  /**
   *
   * @param timeInterval
   * @param averaging
   * @return
   */
  public TimeSeries<TS> average(TemporalAmount timeInterval, 
    Function<Pair<ZonedDateTime, Set<TS>>, TS> averaging);
}
