package common.types;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Iterator;

/**
 *
 * @author Ricardo Marquez
 */
public class DateRange implements Iterable<LocalDate> {

  private final LocalDate startDt;

  private final LocalDate endDt;

  /**
   *
   * @param startDt
   * @param endDt
   */
  public DateRange(LocalDate startDt, LocalDate endDt) {
    if (startDt == null) {
      throw new NullPointerException("Start date  cannot be null");
    }
    if (endDt == null) {
      throw new NullPointerException("End date  cannot be null");
    }
    if (endDt.isBefore(startDt)) {
      throw new IllegalArgumentException("End date cannot be before start date");
    }
    this.startDt = startDt;
    this.endDt = endDt;
  }

  @Override
  public String toString() {
    return "DateRange{" + "startDt=" + startDt + ", endDt=" + endDt + '}';
  }

  /**
   *
   * @return
   */
  @Override
  public Iterator<LocalDate> iterator() {
    DateRangeIterator result = new DateRangeIterator(this);
    return result;

  }

  /**
   *
   * @param dateTime
   * @return
   */
  public boolean contains(ZonedDateTime dateTime) {
    return this.startDt.isBefore(dateTime.toLocalDate()) && this.endDt.isAfter(dateTime.toLocalDate());
  }

  /**
   *
   * @return
   */
  public LocalDate getStartDate() {
    return this.startDt;
  }

  /**
   *
   * @return
   */
  public LocalDate getEndDate() {
    return this.endDt;
  }

  /**
   *
   */
  private static class DateRangeIterator implements Iterator<LocalDate> {

    private final DateRange host;
    int count = 0;

    public DateRangeIterator(DateRange host) {
      this.host = host;
    }

    @Override
    public boolean hasNext() {
      LocalDate startDt = this.host.startDt;
      LocalDate endDt = this.host.endDt;
      LocalDate plusDays = startDt.plusDays(count);
      boolean result = plusDays.isBefore(endDt)
        || plusDays.isEqual(endDt);
      return result;
    }

    @Override
    public LocalDate next() {
      LocalDate result = this.host.startDt.plusDays(count);
      count++;
      return result;
    }
  }

}
