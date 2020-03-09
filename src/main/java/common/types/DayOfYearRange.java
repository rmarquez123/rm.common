package common.types;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 *
 * @author Ricardo Marquez
 */
public class DayOfYearRange implements Comparable<DayOfYearRange> {

  private final DayOfYear startDoy;
  private final DayOfYear endDoy;
  private final ZonedDateTime zonedDateTime;

  /**
   *
   * @param startDoy
   * @param endDoy
   * @param zonedDateTime
   */
  public DayOfYearRange(DayOfYear startDoy, DayOfYear endDoy, ZonedDateTime zonedDateTime) {
    this.startDoy = startDoy;
    this.endDoy = endDoy;
    this.zonedDateTime = zonedDateTime;
  }

  /**
   *
   * @return
   */
  public LocalDateTime getRerenceDateTime() {
    return (this.zonedDateTime == null) ? null : this.zonedDateTime.toLocalDateTime();
  }

  /**
   *
   * @return
   */
  public ZoneId getReferenceZoneId() {
    return (this.zonedDateTime == null) ? null : this.zonedDateTime.getZone();
  }
  
  /**
   * 
   * @param datetime 
   */
  public boolean contains(ZonedDateTime datetime) {
    int year = datetime.getYear();
    ZoneId zone = datetime.getZone();
    DateTimeRange dateTimerange = this.toDateRange(year, zone);
    boolean result = dateTimerange.contains(datetime); 
    if (!result) {
      System.out.println("what");
    }
    return result;
  }
  
  /**
   *
   * @return
   */
  public static DayOfYearRange calendarYear(ZonedDateTime zonedDateTime) {
    DayOfYear startDoy = DayOfYear.ofMonthAndDay(Month.JANUARY, 1);
    DayOfYear endDoy = DayOfYear.ofMonthAndDay(Month.DECEMBER, 31);
    DayOfYearRange result = new DayOfYearRange(startDoy, endDoy, zonedDateTime);
    return result;
  }

  public Month startMonth() {
    return this.startDoy.getMonth();
  }

  /**
   *
   * @return
   */
  public int startDayOfMonth() {
    return this.startDoy.getDay();
  }

  public Month endMonth() {
    return this.endDoy.getMonth();
  }

  /**
   *
   * @return
   */
  public int endDayOfMonth() {
    return this.endDoy.getDay();
  }

  /**
   *
   * @return
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + Objects.hashCode(this.startDoy);
    hash = 37 * hash + Objects.hashCode(this.endDoy);
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
    final DayOfYearRange other = (DayOfYearRange) obj;
    if (!Objects.equals(this.startDoy, other.startDoy)) {
      return false;
    }
    if (!Objects.equals(this.endDoy, other.endDoy)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "DayOfYearRange{" + "startDoy=" + startDoy + ", endDoy=" + endDoy + '}';
  }

  @Override
  public int compareTo(DayOfYearRange o) {
    if (o == null) {
      return 1;
    }
    int result;
    if ((result = this.startDoy.compareTo(o.startDoy)) != 0) {
      return result;
    }
    if ((result = this.endDoy.compareTo(o.endDoy)) != 0) {
      return result;
    }
    if (this.zonedDateTime == null && o.zonedDateTime != null) {
      return -1;
    }
    if (this.zonedDateTime != null && o.zonedDateTime == null) {
      return 1;
    }
    if (this.zonedDateTime == null && o.zonedDateTime == null) {
      return 0;
    } else if ((result = this.zonedDateTime.compareTo(o.zonedDateTime)) != 0) {
      return result;
    }
    return 0;
  }

  /**
   * 
   * @param year
   * @return 
   */
  public  DateTimeRange toDateRange(int year, ZoneId zone) {
    ZonedDateTime startDt = ZonedDateTime.of(year, this.startMonth().getValue(), 
      this.startDayOfMonth(), 0, 0, 0, 0, zone);
    ZonedDateTime endDt = ZonedDateTime.of(year, this.endMonth().getValue(), 
      this.endDayOfMonth(), 0, 0, 0, 0, zone);
    DateTimeRange result = new DateTimeRange(startDt, endDt);
    return result;
  }

  public static class Builder {

    private Month startMonth;
    private int startDay;
    private int endDay;
    private Month endMonth;
    private ZonedDateTime zonedDateTime;

    public Builder() {

    }

    public Builder setDateTime(ZonedDateTime zonedDateTime) {
      this.zonedDateTime = zonedDateTime;
      return this;
    }

    public Builder setStartMonth(Month startMonth) {
      this.startMonth = startMonth;
      return this;
    }

    public Builder setStartDay(int startDay) {
      this.startDay = startDay;
      return this;
    }

    public Builder setEndMonth(Month endMonth) {
      this.endMonth = endMonth;
      return this;
    }

    public Builder setEndDay(int endDay) {
      this.endDay = endDay;
      return this;
    }

    public DayOfYearRange build() {
      DayOfYear startDoy = new DayOfYear(startMonth, startDay);
      DayOfYear endDoy = new DayOfYear(endMonth, endDay);
      return new DayOfYearRange(startDoy, endDoy, this.zonedDateTime);
    }

  }

}
