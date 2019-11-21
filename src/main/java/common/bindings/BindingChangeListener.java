package common.bindings;

import java.util.Objects;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Ricardo Marquez
 */
public class BindingChangeListener<T> implements  ChangeListener<T> {

  private final Property<T> obs;
  
  public BindingChangeListener(Property<T> obs) {
    this.obs = obs;
  }
  
  

  @Override
  public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
    obs.setValue(newValue);
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 61 * hash + Objects.hashCode(this.obs);
    return hash;
  }

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
    final BindingChangeListener<?> other = (BindingChangeListener<?>) obj;
    if (!Objects.equals(this.obs, other.obs)) {
      return false;
    }
    return true;
  }
  
  
}
