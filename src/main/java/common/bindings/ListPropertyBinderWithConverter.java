package common.bindings;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.beans.property.ListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author Ricardo Marquez
 */
public class ListPropertyBinderWithConverter<T, R> {

  private final ListProperty<T> listProperty;
  private final ObservableList<R> items;

  private final ListChangeListener<T> obsListListener;
  private final Function<T, R> converter;

  /**
   *
   * @param listProperty
   * @param items
   * @param converter
   */
  public ListPropertyBinderWithConverter(ListProperty<T> listProperty, ObservableList<R> items, Function<T, R> converter) {
    this.listProperty = listProperty;
    this.items = items;
    this.converter = converter;
    this.obsListListener = this::handleListChange;
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
          List<R> list = change.stream()
            .map(this.converter::apply)
            .collect(Collectors.toList());
          this.items.addAll(list);
        }
      }
    });
    if (this.listProperty.get() != null) {
      List<R> list = this.listProperty.getValue().stream()
        .map(converter::apply)
        .collect(Collectors.toList());
      this.items.addAll(list);
    }
  }

  /**
   *
   * @param converter1
   * @return
   */
  private void handleListChange(ListChangeListener.Change<? extends T> c) {
    if (c.next()) {
      if (c.wasAdded()) {
        List<? extends T> added = c.getAddedSubList();
        for (T t : added) {
          R asItem = this.converter.apply(t);
          if (!this.items.contains(asItem)) {
            this.items.add(asItem);
          }
        }
      } else if (c.wasRemoved()) {
        List<? extends R> removed = c.getRemoved().stream()
          .map(this.converter::apply)
          .collect(Collectors.toList());
        this.items.removeAll(removed);
      }
    }
  }
;
}
