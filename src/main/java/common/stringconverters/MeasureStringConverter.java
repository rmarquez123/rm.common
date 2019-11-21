package common.stringconverters;

import javafx.util.StringConverter;
import javax.measure.Measure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

/**
 *
 */
public class MeasureStringConverter<T extends Quantity> extends StringConverter<Measure<T>> {

  private final Unit<T> unit;

  public MeasureStringConverter(Unit<T> unit) {
    this.unit = unit;
  }

  /**
   *
   * @param object
   * @return
   */
  @Override
  public String toString(Measure<T> object) {
    return object == null ? "" : String.valueOf(object.doubleValue(this.unit));
  }

  /**
   *
   * @param string
   * @return
   */
  @Override
  public Measure<T> fromString(String string) {
    return string == null || string.isEmpty() ? null : Measure.valueOf(Double.parseDouble(string), this.unit);
  }
  
}
