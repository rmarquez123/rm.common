package common.timeseries.impl;

import common.timeseries.TimeSeries;
import common.timeseries.TimeStepValue;
import common.types.DateRange;
import common.types.DateTimeRange;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
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
public class CustomTimeStepTimeSeries<T extends TimeStepValue<?>> implements TimeSeries<T> {

  private final DateTimeRange dateTimeRange;
  private final TemporalAmount timeInterval;
  private final List<T> records;

  /**
   *
   * @param timeInterval
   * @param records
   */
  public CustomTimeStepTimeSeries(TemporalAmount timeInterval, List<T> records) {
    Objects.requireNonNull(timeInterval);
    Objects.requireNonNull(records);
    if (records.isEmpty()) {
      throw new IllegalArgumentException("Records cannot be empty");
    }
    this.records = new ArrayList<>(records);
    Collections.sort(this.records, (T o1, T o2)
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
      T previousRecord = this.records.get(i - 1);
      T currentRecord = this.records.get(i);
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
  public boolean contains(T timeStepValue) {
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
  public T getFirst() {
    T result = this.records.get(0);
    return result;
  }

  /**
   *
   * @return
   */
  @Override
  public T getLast() {
    T result = this.records.get(this.records.size() - 1);
    return result;
  }

  /**
   *
   * @param previous
   * @return
   */
  @Override
  public T getNext(T previous) {
    //To change body of generated methods, choose Tools | Templates.
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @param after
   * @return
   */
  @Override
  public T getPrevious(T after) {
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
  public T getTimeStepValue(ZonedDateTime dateTime) {
    T result = null;
    if (this.dateTimeRange.contains(dateTime)) {
      for (T record : this.records) {
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
  public Iterator<T> iterator() {
    return this.records.iterator();
  }

  /**
   *
   * @param timeInterval
   * @return
   */
  @Override
  public TimeSeries<T> average(TemporalAmount timeInterval,
    Function<Pair<ZonedDateTime, Set<T>>, T> averaging) {
    SortedSet<T> copy = new TreeSet<>(this.records);
    Iterable<ZonedDateTime> iterator = this.dateTimeRange.iterator(timeInterval);
    List<T> newRecords = new ArrayList<>();
    for (ZonedDateTime current : iterator) {
      ZonedDateTime nextDt = current.plus(timeInterval);
      Set<T> toAverage = new HashSet<>();
      Iterator<T> copyIterator = copy.iterator();
      while (copyIterator.hasNext()) {
        T r = copyIterator.next();
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
    TimeSeries<T> result = new CustomTimeStepTimeSeries<>(timeInterval, newRecords);
    return result;
  }

  /**
   *
   * @return
   */
  @Override
  public String toString() {
    return "{" + "dateTimeRange=" + dateTimeRange + ", timeInterval=" + timeInterval + ", records=" + records + '}';
  }

}
