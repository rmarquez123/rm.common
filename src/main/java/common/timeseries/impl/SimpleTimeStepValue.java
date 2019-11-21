package common.timeseries.impl;

import common.timeseries.TimeStepValue;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 *
 * @author Ricardo Marquez
 */
public class SimpleTimeStepValue<T> implements TimeStepValue<T> {

  private final ZonedDateTime dateTime;
  private final T userObject;

  /**
   *
   * @param dateTime
   * @param userObject
   */
  public SimpleTimeStepValue(ZonedDateTime dateTime, T userObject) {
    Objects.requireNonNull(dateTime, "Date Time cannot be null"); 
    Objects.requireNonNull(dateTime, "User Object cannot be null");
    this.dateTime = dateTime;
    this.userObject = userObject;
  }

  /**
   *
   * @return
   */
  @Override
  public T getUserObject() {
    return this.userObject;
  }

  /**
   *
   * @return
   */
  @Override
  public ZonedDateTime getZoneDateTime() {
    return this.dateTime;
  }

  /**
   *
   * @param o
   * @return
   */
  @Override
  public int compareTo(TimeStepValue o) {
    return this.dateTime.compareTo(o.getZoneDateTime());
  }

  /**
   *
   * @return
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 41 * hash + Objects.hashCode(this.dateTime);
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
    final SimpleTimeStepValue<?> other = (SimpleTimeStepValue<?>) obj;
    if (!Objects.equals(this.dateTime, other.dateTime)) {
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
    return "SimpleTimeStepValue{" + "dateTime=" + dateTime + ", userObject=" + userObject + '}';
  }

}
