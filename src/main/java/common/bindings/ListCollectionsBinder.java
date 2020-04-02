package common.bindings;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author Ricardo Marquez
 */
public class ListCollectionsBinder<T2, T1> {

  private final Collection<T2> target;
  private final ObservableList<T1> reference;
  private final Function<T1, T2> conveter;

  public ListCollectionsBinder(Collection<T2> target, ObservableList<T1> reference, Function<T1, T2> converter) {
    this.target = target;
    this.reference = reference;
    this.conveter = converter;
  }

  public ListCollectionsBinder(Collection<T2> target, ObservableList<T1> reference) {
    this.target = target;
    this.reference = reference;
    this.conveter = (r) -> (T2) r;
  }

  /**
   *
   */
  void bind() {
    this.reference.addListener(this::onChanged);
    this.target.clear();
    List<T2> converted = this.reference.stream()
      .map(this.conveter)
      .collect(Collectors.toList());
    this.target.addAll(converted);
  }

  /**
   *
   */
  public void unbind() {
    this.reference.removeListener(this::onChanged);
  }

  /**
   *
   * @param change
   */
  private void onChanged(ListChangeListener.Change<? extends T1> change) {
    while (change.next()) {
      if (change.wasAdded()) {
        List<? extends T1> items = change.getAddedSubList();
        items.forEach((item) -> this.target.add(this.conveter.apply(item)));
      } else if (change.wasRemoved()) {
        List<? extends T1> items = change.getRemoved();
        items.forEach((item) -> this.target.remove(this.conveter.apply(item)));
      }
    }
  }
}
