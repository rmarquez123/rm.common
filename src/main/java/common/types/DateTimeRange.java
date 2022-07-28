package common.types;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Iterator;
import java.util.Objects;

/**
 *
 * @author Ricardo Marquez
 */
public class DateTimeRange {

  /**
   * 
   * @param datetimerange1
   * @param datetimerange2
   * @return 
   */
  public static DateTimeRange intersection(DateTimeRange datetimerange1, DateTimeRange datetimerange2) {
    ZonedDateTime startDt1 = datetimerange1.startDt;
    ZonedDateTime startDt2 = datetimerange2.startDt;
    ZonedDateTime startDt = startDt1.isBefore(startDt2)? startDt2:startDt1;
    ZonedDateTime endDt1 = datetimerange1.endDt;
    ZonedDateTime endDt2 = datetimerange2.endDt;
    ZonedDateTime endDt = endDt1.isAfter(endDt2)? endDt2:endDt1;
    DateTimeRange result = new DateTimeRange(startDt, endDt); 
    return result; 
  }

  private final ZonedDateTime startDt;
  private final ZonedDateTime endDt;
  private ZoneId zoneId;

  /**
   *
   * @param startDt
   * @param endDt
   */
  public DateTimeRange(ZonedDateTime startDt, ZonedDateTime endDt) {
    if (startDt == null) {
      throw new NullPointerException("start date cannot be null");
    }
    if (endDt == null) {
      throw new NullPointerException("end date cannot be null");
    }
    if (!startDt.getZone().equals(endDt.getZone())) {
      throw new IllegalArgumentException("zone ids are not equal.");
    }
    this.startDt = startDt;
    this.endDt = endDt;
    this.zoneId = startDt.getZone();
  }
  
  /**
   * 
   * @return 
   */
  public ZoneId getZoneId() {
    return this.zoneId;
  }

  /**
   *
   * @param zoneId
   * @param pattern
   * @param startDtText
   * @param endDateText
   * @return
   * @throws ParseException
   */
  public static DateTimeRange of(ZoneId zoneId, String pattern, String startDtText, String endDateText)
    throws ParseException {
    DateFormat format = new SimpleDateFormat(pattern);
    ZonedDateTime startDt = ZonedDateTime
      .ofInstant(format.parse(startDtText).toInstant(), ZoneId.systemDefault()).toOffsetDateTime()
      .atZoneSimilarLocal(zoneId);
    ZonedDateTime endDt = ZonedDateTime
      .ofInstant(format.parse(endDateText).toInstant(), ZoneId.systemDefault()).toOffsetDateTime()
      .atZoneSimilarLocal(zoneId);
    DateTimeRange result = new DateTimeRange(startDt, endDt);
    return result;
  }
  
  /**
   * 
   * @param format
   * @return 
   */
  public String getStartDateText(String format, ZoneId zoneId) {
    DateTimeFormatter formatter =new DateTimeFormatterBuilder()
      .appendPattern(format)
      .toFormatter();
    ZonedDateTime atZoneSameInstant = this.startDt.toOffsetDateTime().atZoneSameInstant(zoneId); 
    String result = formatter.format(atZoneSameInstant); 
    return result; 
  }
  
  /**
   * 
   * @param format
   * @return 
   */
  public String getEndDateText(String format, ZoneId zoneId) {
    DateTimeFormatter formatter =new DateTimeFormatterBuilder()
      .appendPattern(format)
      .toFormatter();
    return formatter.format(this.endDt.toOffsetDateTime().atZoneSameInstant(zoneId)); 
  }

  /**
   *
   * @return
   */
  public ZonedDateTime getStartDate() {
    
    return this.startDt;
  }

  /**
   *
   * @return
   */
  public ZonedDateTime getEndDate() {
    return this.endDt;
  }

  /**
   *
   * @return
   */
  public DateRange getDateRange() {
    LocalDate starDtAsDate = this.startDt.toLocalDate();
    LocalDate endDtAsDate = this.endDt.toLocalDate();
    DateRange result = new DateRange(starDtAsDate, endDtAsDate);
    return result;
  }

  /**
   *
   * @param dateTime
   * @return
   */
  public boolean contains(ZonedDateTime dateTime) {
    boolean afterOrEquals = this.startDt.isBefore(dateTime) || this.startDt.isEqual(dateTime);
    boolean beforeOrEquals = this.endDt.isEqual(dateTime) || this.endDt.isAfter(dateTime);
    return afterOrEquals && beforeOrEquals;
  }

  /**
   *
   * @param timeStep
   * @return
   */
  public int getNumberOfSteps(TemporalAmount timeStep) {
    TemporalUnit unit = timeStep.getUnits().get(0);
    long until = this.startDt.until(this.endDt, unit);
    long amount = timeStep.get(unit);
    long result = until / amount + 1l;
    return (int) result;
  }

  /**
   *
   * @param temporalUnit
   * @return
   */
  public Iterable<ZonedDateTime> iterator(TemporalAmount temporalUnit) {
    return () -> new DateTimeRangeIterator(this, temporalUnit);
  }

  /**
   *
   * @return
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 41 * hash + Objects.hashCode(this.startDt);
    hash = 41 * hash + Objects.hashCode(this.endDt);
    return hash;
  }

  /**
   *
   * @param obj
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DateTimeRange other = (DateTimeRange) obj;
    if (!Objects.equals(this.startDt, other.startDt)) {
      return false;
    }
    if (!Objects.equals(this.endDt, other.endDt)) {
      return false;
    }
    return true;
  }
  
  /**
   * 
   * @return 
   */
  @Override
  public String toString() {
    return "DateTimeRange{" + "startDt=" + startDt + ", endDt=" + endDt + '}';
  }
  
  /**
   * 
   * @param of
   * @return 
   */
  public DateTimeRange toZoneId(ZoneId of) {
    ZonedDateTime s = this.startDt.toOffsetDateTime().atZoneSameInstant(of);
    ZonedDateTime e = this.endDt.toOffsetDateTime().atZoneSameInstant(of);
    DateTimeRange result = new DateTimeRange(s, e);
    return result;
  }



  /**
   *
   */
  private static class DateTimeRangeIterator implements Iterator<ZonedDateTime> {

    private final DateTimeRange host;
    private final TemporalAmount temporalUnit;
    int count = 0;

    /**
     *
     * @param host
     * @param temporalUnit
     */
    public DateTimeRangeIterator(DateTimeRange host, TemporalAmount temporalUnit) {
      this.host = host;
      this.temporalUnit = temporalUnit;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasNext() {
      ZonedDateTime startDt = this.host.startDt;
      ZonedDateTime endDt = this.host.endDt;
      TemporalUnit unit = temporalUnit.getUnits().get(0);
      long originalCount = temporalUnit.get(unit);
      ZonedDateTime plus = startDt.plus(count * originalCount, unit);
      boolean result = plus.isBefore(endDt)
        || plus.isEqual(endDt);
      return result;
    }

    /**
     *
     * @return
     */
    @Override
    public ZonedDateTime next() {
      TemporalUnit unit = temporalUnit.getUnits().get(0);
      long originalCount = temporalUnit.get(unit);
      ZonedDateTime result = this.host.startDt.plus(count * originalCount, unit);
      count++;
      return result;
    }
  }

}
