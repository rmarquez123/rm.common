package common.bindings;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

/**
 *
 * @author Ricardo Marquez
 */
public class SetCollectionsBinder<T2, T1> {

  private final Collection<T2> target;
  private final ObservableSet<T1> reference;
  private final Function<T1, T2> conveter;

  public SetCollectionsBinder(Collection<T2> target, ObservableSet<T1> reference, Function<T1, T2> converter) {
    this.target = target;
    this.reference = reference;
    this.conveter = converter;
  }
  
  public SetCollectionsBinder(Collection<T2> target, ObservableSet<T1> reference) {
    this.target = target;
    this.reference = reference;
    this.conveter = (r)->(T2)r;
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
   * @param change
   */
  private void onChanged(SetChangeListener.Change<? extends T1> change) {
    if (change.wasAdded()) {
      T1 item = change.getElementAdded();
      this.target.add(this.conveter.apply(item));
    } else if (change.wasRemoved()) {
      T1 item = change.getElementRemoved();
      this.target.remove(this.conveter.apply(item));
    }
  }
}
