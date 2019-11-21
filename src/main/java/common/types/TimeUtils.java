package common.types;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;

/**
 *
 * @author Ricardo Marquez
 */
public class TimeUtils {
    
  
  public static ZonedDateTime truncate(ZonedDateTime datetime, TemporalAmount temporalAmount) {
    long ticks = temporalAmount.get(ChronoUnit.SECONDS);
    long elapsedTicks = temporalAmount.get(ChronoUnit.SECONDS); 
    long adjustedElapsedTickes = (long) (Math.floor(elapsedTicks / ticks) * ticks);
    ZonedDateTime t = ZonedDateTime.ofInstant(Instant.ofEpochSecond(adjustedElapsedTickes), datetime.getZone());
    return t;
  }
}
