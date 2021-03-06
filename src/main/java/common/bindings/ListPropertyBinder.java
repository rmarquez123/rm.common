package common.bindings;

import java.util.List;
import javafx.beans.property.ListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author Ricardo Marquez
 */
class ListPropertyBinder<T> {

  private final ListProperty<T> listProperty;
  private final ObservableList<T> items;
  private final ListChangeListener<T> obsListListener;

  /**
   *
   * @param listProperty
   * @param items
   */
  ListPropertyBinder(ListProperty<T> listProperty, ObservableList<T> items) {
    this.listProperty = listProperty;
    this.items = items;
    this.obsListListener = ((c) -> {
      if (c.next()) {
        if (c.wasAdded()) {
          List<? extends T> added = c.getAddedSubList();
          for (T t : added) {
            if (!this.items.contains(t)) {
              this.items.add(t);
            }
          }
        } else if (c.wasRemoved()) {
          List<? extends T> removed = c.getRemoved();
          this.items.removeAll(removed);
        }
      }
    });
  }

  /**
   *
   */
  void bind() {
    this.listProperty.addListener((obs, old, change) -> {
      if (old != change) {
        if (old != null) {
          old.removeListener(this.obsListListener);
        }
        this.items.clear();
        if (change != null) {
          change.addListener(this.obsListListener);
          this.items.addAll(change);
        }
      }
    });
    if (this.listProperty.get() != null) {
      this.items.addAll(this.listProperty.getValue());
    }
  }

}
