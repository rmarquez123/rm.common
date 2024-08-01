/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common.timeseries;

import common.types.DateRange;
import common.types.DateTimeRange;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.math3.util.Pair;


public class TimeSeriesImpl<TS extends TimeStepValue<?>> implements TimeSeries<TS> {

  @Override
  public ZoneId getZoneId() {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int size() {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean contains(TS timeStepValue) {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean contains(ZonedDateTime zonedDateTime) {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public TS getFirst() {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public TS getLast() {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public TS getNext(TS previous) {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public TS getPrevious(TS after) {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isRegularTimeIntervaled() {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public TemporalAmount getTimeInterval() {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public TS getTimeStepValue(ZonedDateTime dateTime) {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public DateTimeRange getDateTimeRange() {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public DateRange getDateRange() {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public TimeSeries<TS> average(TemporalAmount timeInterval, Function<Pair<ZonedDateTime, Set<TS>>, TS> averaging) {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Iterator<TS> iterator() {
    // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
