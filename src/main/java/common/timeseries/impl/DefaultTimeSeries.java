package common.timeseries.impl;

import common.timeseries.TimeSeries;
import common.timeseries.TimeStepValue;
import common.types.DateRange;
import common.types.DateTimeRange;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import org.apache.commons.math3.util.Pair;

/**
 *
 * @author Ricardo Marquez
 */
public final class DefaultTimeSeries<R> implements TimeSeries<TimeStepValue<R>> {

  private final DateTimeRange dateTimeRange;
  private final TemporalAmount timeInterval;
  private final List<TimeStepValue<R>> records;

  /**
   *
   */
  public static <R> DefaultTimeSeries<R>
    create(TemporalAmount timeInterval, List<? extends TimeStepValue<R>> records) {
    return new DefaultTimeSeries<>(timeInterval, records);
  }
    
  /**
   *
   */
  public static <R, T> DefaultTimeSeries<R>
    create(TemporalAmount timeInterval, Collection<T> records, Function<T, TimeStepValue<R>> mapper) {
    List<TimeStepValue<R>> mapped = new ArrayList<>();
    for (T record : records) {
      TimeStepValue<R> tsValue = mapper.apply(record);
      mapped.add(tsValue); 
    }
    return new DefaultTimeSeries<>(timeInterval, mapped);
  }

  /**
   *
   * @param dateTimeRange
   * @param timeStepValues
   */
  public DefaultTimeSeries(TemporalAmount timeInterval, List<? extends TimeStepValue<R>> records) {
    Objects.requireNonNull(timeInterval);
    Objects.requireNonNull(records);
    if (records.isEmpty()) {
      throw new IllegalArgumentException("Records cannot be empty");
    }
    this.records = new ArrayList<>(records);
    Collections.sort(this.records, (TimeStepValue<R> o1, TimeStepValue<R> o2) 
      -> o1.getZoneDateTime().compareTo(o2.getZoneDateTime()));
    ZonedDateTime startDate = this.records.get(0).getZoneDateTime();
    ZonedDateTime endDate = this.records.get(this.records.size() - 1).getZoneDateTime();
    this.dateTimeRange = new DateTimeRange(startDate, endDate);
    this.timeInterval = timeInterval;
    this.validate();
  }

  /**
   *
   * @throws RuntimeException
   */
  private void validate() throws RuntimeException {
    for (int i = 1; i < this.records.size(); i++) {
      TimeStepValue<R> previousRecord = this.records.get(i - 1);
      TimeStepValue<R> currentRecord = this.records.get(i);
      ZonedDateTime expectedPreviousDateTime = currentRecord.getZoneDateTime().minus(this.timeInterval);
      ZonedDateTime previousDateTime = previousRecord.getZoneDateTime();
      if (!expectedPreviousDateTime.equals(previousDateTime)) {
        throw new RuntimeException(
          String.format("Invalid time steps found at index: %d, where expected date time is : %s ", 
            i, expectedPreviousDateTime)
        );
      }
    }
  }

  /**
   *
   * @return
   */
  @Override
  public ZoneId getZoneId() {
    return this.dateTimeRange.getStartDate().getZone();
  }

  /**
   *
   * @return
   */
  @Override
  public DateTimeRange getDateTimeRange() {
    return this.dateTimeRange;
  }

  /**
   *
   * @return
   */
  @Override
  public DateRange getDateRange() {
    return this.dateTimeRange.getDateRange();
  }

  /**
   *
   * @return
   */
  @Override
  public int size() {
    return this.records.size();
  }

  /**
   *
   * @param timeStepValue
   * @return
   */
  @Override
  public boolean contains(TimeStepValue<R> timeStepValue) {
    return this.dateTimeRange.contains(timeStepValue.getZoneDateTime());
  }

  /**
   *
   * @param zonedDateTime
   * @return
   */
  @Override
  public boolean contains(ZonedDateTime zonedDateTime) {
    return this.dateTimeRange.contains(zonedDateTime);
  }

  /**
   *
   * @return
   */
  @Override
  public TimeStepValue<R> getFirst() {
    TimeStepValue<R> result = this.records.get(0);
    return result;
  }

  /**
   *
   * @return
   */
  @Override
  public TimeStepValue<R> getLast() {
    TimeStepValue<R> result = this.records.get(this.records.size() - 1);
    return result;
  }

  /**
   *
   * @param previous
   * @return
   */
  @Override
  public TimeStepValue<R> getNext(TimeStepValue<R> previous) {
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @param after
   * @return
   */
  @Override
  public TimeStepValue<R> getPrevious(TimeStepValue<R> after) {
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @return
   */
  @Override
  public boolean isRegularTimeIntervaled() {
    return true;
  }

  /**
   *
   * @return
   */
  @Override
  public TemporalAmount getTimeInterval() {
    return this.timeInterval;
  }

  /**
   *
   * @param dateTime
   * @return
   */
  @Override
  public TimeStepValue<R> getTimeStepValue(ZonedDateTime dateTime) {
    TimeStepValue<R> result = null;
    if (this.dateTimeRange.contains(dateTime)) {
      for (TimeStepValue<R> record : this.records) {
        if (Objects.equals(record.getZoneDateTime(), dateTime)) {
          result = record;
        }
      }
    }
    return result;
  }

  /**
   *
   * @return
   */
  @Override
  public Iterator<TimeStepValue<R>> iterator() {
    return this.records.iterator();
  }

  /**
   *
   * @param timeInterval
   * @return
   */
  @Override
  public TimeSeries<TimeStepValue<R>> average(TemporalAmount timeInterval,
    Function<Pair<ZonedDateTime, Set<TimeStepValue<R>>>, TimeStepValue<R>> averaging) {
    SortedSet<TimeStepValue<R>> copy = new TreeSet<>(this.records);
    Iterable<ZonedDateTime> iterator = this.dateTimeRange.iterator(timeInterval);
    List<TimeStepValue<R>> newRecords = new ArrayList<>();
    for (ZonedDateTime current : iterator) {
      ZonedDateTime nextDt = current.plus(timeInterval);
      Set<TimeStepValue<R>> toAverage = new HashSet<>();
      Iterator<TimeStepValue<R>> copyIterator = copy.iterator();
      while (copyIterator.hasNext()) {
        TimeStepValue<R> r = copyIterator.next();
        if ((current.isBefore(r.getZoneDateTime()) || current.isEqual(r.getZoneDateTime()))
          && r.getZoneDateTime().isBefore(nextDt)) {
          toAverage.add(r);
          copyIterator.remove();
        } else {
          newRecords.add(averaging.apply(Pair.create(nextDt, toAverage)));
          break;
        }
      }
    }
    TimeSeries<TimeStepValue<R>> result = new DefaultTimeSeries<>(timeInterval, newRecords);
    return result;
  }

  /**
   *
   * @return
   */
  @Override
  public String toString() {
    return "DefaultTimeSeries{" + "dateTimeRange=" + dateTimeRange + ", timeInterval=" + timeInterval + ", records=" + records + '}';
  }

}
