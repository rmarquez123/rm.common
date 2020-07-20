package common.timeseries;

import common.timeseries.impl.CustomTimeStepTimeSeries;
import common.types.DateTimeRange;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.Pair;


/**
 *
 * @author Ricardo Marquez
 */
public class TimeSeriesUtils {

  
  /**
   * 
   * @param <T>
   * @param xseries
   * @param yseries
   * @param supplier
   * @return 
   */
  public static <T extends TimeStepValue<?>> TimeSeries<T>  combine( ///
    TimeSeries xseries, TimeSeries yseries, //
    Function<Pair<TimeStepValue, TimeStepValue>, T> supplier) {
    DateTimeRange xdatetimerange = xseries.getDateTimeRange();
    DateTimeRange ydatetimerange = yseries.getDateTimeRange();
    DateTimeRange datetimerange = DateTimeRange.intersection(xdatetimerange, ydatetimerange);
    List<T> records = new ArrayList<>();
    for (ZonedDateTime refdate : datetimerange.iterator(xseries.getTimeInterval())) {
      TimeStepValue xvalue = xseries.getTimeStepValue(refdate); 
      TimeStepValue yvalue = yseries.getTimeStepValue(refdate);
      T v = supplier.apply(Pair.of(xvalue, yvalue));
      records.add(v);
    }
    TimeSeries<T> result = new CustomTimeStepTimeSeries<>(xseries.getTimeInterval(), records);
    return result;
  }  
}
