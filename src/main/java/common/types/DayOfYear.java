package common.types;

import java.time.Month;
import java.util.Objects;

/**
 *
 * @author Ricardo Marquez
 */
public class DayOfYear implements Comparable<DayOfYear> {

  private final Month month;
  private final int day;

  /**
   *
   * @param month
   * @param day
   */
  DayOfYear(Month month, int day) {
    this.month = month;
    this.day = day;
  }

  /**
   *
   * @param month
   * @param day
   * @return
   */
  public static DayOfYear ofMonthAndDay(Month month, int day) {
    return new DayOfYear(month, day);
  }

  public Month getMonth() {
    return month;
  }

  public int getDay() {
    return day;
  }

  /**
   *
   * @return
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + Objects.hashCode(this.month);
    hash = 89 * hash + this.day;
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
    final DayOfYear other = (DayOfYear) obj;
    if (this.day != other.day) {
      return false;
    }
    if (this.month != other.month) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "DayOfYear{" + "month=" + month + ", day=" + day + '}';
  }

  /**
   *
   * @param o
   * @return
   */
  @Override
  public int compareTo(DayOfYear o) {
    if (o == null) {
      return 1;
    }
    int result;
    if ((result = this.month.compareTo(o.month)) != 0) {
      return result;
    }
    if ((result = Integer.compare(this.day, o.day)) != 0) {
      return result;
    }
    return 0;
  }

}
