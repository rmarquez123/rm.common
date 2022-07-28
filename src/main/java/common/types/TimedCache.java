package common.types;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Ricardo Marquez
 */
public class TimedCache<T> {
  private final Timer timer;
  private final Supplier<T> supplier;
  private final Property<T> property = new SimpleObjectProperty<>();
  
  /**
   * 
   * @param timer
   * @param supplier 
   */
  public TimedCache(String name, Supplier<T> supplier) {
    this.timer = new Timer(name);
    this.supplier = supplier;
  }
  
  /**
   * 
   * @return 
   */
  public ReadOnlyProperty<T> valueProperty() {
    return this.property;
  }
  
  
  /**
   * 
   */
  public synchronized void start(long periodInMillis) {
    this.timer.schedule(new TimerTask() {
      @Override
      public void run() {
        updateValue();
      }
    }, new Date(), periodInMillis);
  }
  
  
  /**
   * 
   */
  public void cancel() {
    this.timer.cancel();
  }
  
  /**
   * 
   */
  public void forceUpdate() {
    this.updateValue();
  }
  
  /**
   * 
   */
  private void updateValue() {
    T logs = this.supplier.get();
    this.property.setValue(logs);
  }
}
