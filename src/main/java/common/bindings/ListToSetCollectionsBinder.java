package common.bindings;

import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

/**
 *
 * @author Ricardo Marquez
 */
class ListToSetCollectionsBinder<T> {

  private final ObservableList<T> target;
  private final ObservableSet<T> reference;

  /**
   *
   * @param target
   * @param reference
   */
  public ListToSetCollectionsBinder(ObservableList<T> target, ObservableSet<T> reference) {
    this.target = target;
    this.reference = reference;
  }

  /**
   *
   */
  void bind() {
    this.reference.addListener(this::onChanged);
    this.target.setAll(this.reference);
  }

  /**
   *
   * @param change
   */
  private void onChanged(SetChangeListener.Change<? extends T> change) {
    if (change.wasAdded()) {
      T item = change.getElementAdded();
      this.target.add(item);
    } else if (change.wasRemoved()) {
      T item = change.getElementRemoved();
      this.target.remove(item);
    }
  }

}
