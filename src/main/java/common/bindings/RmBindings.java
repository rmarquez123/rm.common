package common.bindings;

import java.util.function.Function;
import java.util.function.Supplier;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.util.StringConverter;

/**
 *
 * @author Ricardo Marquez
 */
public class RmBindings {

  /**
   *
   * @param <T1>
   * @param <T2>
   * @param target
   * @param bag
   * @param reference
   * @param predicate
   */
  public static <T1, T2> void bindObject(Property<T1> target, ObservableSet<T1> bag,
    Property<T2> reference, MatchPredicate<T1, T2> predicate) {
    Supplier<T1> callable = () -> {
      T2 selected = reference.getValue();
      T1 r;
      if (selected != null) {
        r = bag.stream()
          .filter((e) -> predicate.matches(e, selected))
          .findFirst()
          .orElseThrow(null);
      } else {
        r = null;
      }
      return r;
    };
    RmBindings.bindObject(target, callable, reference);
    bag.addListener((SetChangeListener.Change<? extends T1> change) -> {
      try {
        if (target.getValue() == null) {
          T1 a = callable.get();
          target.setValue(a);
        }
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    });
  }

  /**
   *
   * @param <T>
   * @param records
   * @param result
   */
  public static <T> void bindToFirstValueInSet(ObservableSet<T> records, Property<T> result) {
    SetChangeListener<T> listlistener = (c) -> {
      if (!c.getSet().contains(result.getValue())) {
        result.setValue(c.getSet().stream().findFirst().orElse(null));
      }
    };
    records.addListener(listlistener);
    result.setValue(records.stream().findFirst().orElse(null));
  }

  /**
   *
   * @param <T>
   * @param records
   * @param result
   */
  public static <T> void bindToFirstValueInList(ListProperty<T> records, Property<T> result) {
    if (records.get() != null) {
      result.setValue(records.stream().findFirst().orElse(null));
    }
    ListChangeListener<T> listlistener = (c) -> {
      while (c.next()) {
        if (!c.getList().contains(result.getValue())) {
          result.setValue(c.getList().stream().findFirst().orElse(null));
        }
      }
    };
    records.addListener((obs, old, change) -> {
      if (old != null) {
        old.removeListener(listlistener);
      }
      if (change != null) {
        result.setValue(change.stream().findFirst().orElse(null));
        change.addListener(listlistener);
      } else {
        result.setValue(null);
      }
    });

  }

  private RmBindings() {
  }

  /**
   *
   * @param listProperty
   * @param items
   */
  public static <T> void bindToListProperty(ListProperty<T> listProperty, ObservableList<T> items) {
    ListPropertyBinder<T> binder = new ListPropertyBinder<>(listProperty, items);
    binder.bind();
  }

  /**
   *
   * @param listProperty
   * @param items
   */
  public static <T> void bindSetToListProperty(ListProperty<T> listProperty, ObservableSet<T> items) {
    ListPropertyBinderWithSet<T> binder = new ListPropertyBinderWithSet<>(listProperty, items);
    binder.bind();
  }

  /**
   * Binds the reference items to the target.
   *
   * @param target
   * @param reference
   */
  public static <T> SetCollectionsBinder<T, T> bindCollections(ObservableSet<T> target, ObservableSet<T> reference) {
    SetCollectionsBinder<T, T> binder = new SetCollectionsBinder<>(target, reference);
    binder.bind();
    return binder;
  }
  
  /**
   * Binds the reference items to the target.
   *
   * @param target
   * @param reference
   */
  public static <T> ListCollectionsBinder<T, T> bindCollections(ObservableList<T> target, ObservableList<T> reference) {
    ListCollectionsBinder<T, T> binder = new ListCollectionsBinder<>(target, reference);
    binder.bind();
    return binder;
  }
  
  /**
   * Binds the reference items to the target.
   *
   * @param target
   * @param reference
   */
  public static <T> void bindCollections(ObservableList<T> target, ObservableSet<T> reference) {
    SetCollectionsBinder<T, T> binder = new SetCollectionsBinder<>(target, reference);
    binder.bind();
  }

  /**
   * Binds the reference items to the target.
   *
   * @param target
   * @param reference
   */
  public static <T2, T1> void bindCollections(ObservableSet<T2> target, // 
    ObservableSet<T1> reference, Function<T1, T2> converter) {
    SetCollectionsBinder<T2, T1> binder = new SetCollectionsBinder<>(target, reference, converter);
    binder.bind();
  }

  /**
   *
   * @param listProperty
   * @param items
   */
  public static <T, R> void bindToListProperty(ListProperty<T> listProperty, //
    ObservableList<R> items, Function<T, R> converter) {

    ListPropertyBinderWithConverter<T, R> binder = new ListPropertyBinderWithConverter<>(listProperty, items, converter);
    binder.bind();
  }

  /**
   *
   * @param selectedProperty
   * @param property
   * @param targetvalue
   */
  public static <T> void bindTrueIfPropertyValue(BooleanProperty selectedProperty, Property<T> property, T targetvalue) {
    property.addListener((obs, old, change) -> {
      selectedProperty.set(change == targetvalue);
    });
    selectedProperty.set(property.getValue() == targetvalue);
  }

  /**
   *
   * @param property
   * @param value
   * @param selectedProperty
   */
  public static <T> void bindPropertyValueIfTrue(Property<T> property, T value, BooleanProperty selectedProperty) {
    selectedProperty.addListener((obs, old, change) -> {
      if (change) {
        property.setValue(value);
      }
    });
    if (selectedProperty.get()) {
      property.setValue(value);
    }
  }

  /**
   *
   * @param observables
   */
  public static void toggle(Property<Boolean>... observables) {
    for (Property<Boolean> observable : observables) {
      observable.addListener((obs, old, change) -> {
        if (change == true) {
          for (Property<Boolean> observable1 : observables) {
            observable1.setValue(observable.equals(observable1));
          }
        }
      });
    }
  }

  /**
   * Applies a 2-way binding between obs1 and obs2 with obs1 initialized to the value of
   * obs2.
   *
   * @param <T>
   * @param obs1
   * @param obs2
   */
  public static <T> void bind1To2(Property<T> obs1, Property<T> obs2) {
    obs2.addListener((obs, old, change) -> {
      try {
        obs1.setValue(change);
      } catch(Exception ex) {
        throw new RuntimeException(
          String.format("Attempting to set value '%s' to observable '%s'", change, obs1), ex);
      }
    });
    obs1.addListener((obs, old, change) -> {
      try {
        obs2.setValue(change);
      } catch(Exception ex) {
        throw new RuntimeException(
          String.format("Attempting to set value '%s' to observable '%s'", change, obs1), ex);
      }
    });
    obs1.setValue(obs2.getValue());
  }
  

  /**
   *
   * @param <T>
   * @param obs1
   * @param obs2
   */
  public static <T> void bindToStringProperty(Property<String> obs1,
    Property<T> obs2, StringConverter<T> converter) {
    obs1.addListener((obs, old, change) -> {
      obs2.setValue(converter.fromString(change));
    });
    obs2.addListener((obs, old, change) -> {
      obs1.setValue(converter.toString(obs2.getValue()));
    });
    obs1.setValue(converter.toString(obs2.getValue()));
  }

  /**
   *
   * @param <T>
   * @param obs1
   * @param obs2
   */
  public static void bindToStringProperty(Property<String> obs1, Property<String> obs2) {
    if (obs1 == obs2) {
      throw new IllegalArgumentException("Observables are the same instance");
    }
    obs1.addListener((obs, old, change) -> {
      obs2.setValue(change);
    });
    obs1.setValue(obs1.getValue());
  }

  /**
   *
   * @param <T>
   * @param obs1
   * @param supplier
   */
  public static void bindBoolean(Property<Boolean> obs1, //
    Supplier<Boolean> supplier, Property<?>... observables) {
    for (Property<?> observable : observables) {
      observable.addListener((obs, old, change) -> {
        setValueFromCallable(supplier, obs1);
      });
    }
    setValueFromCallable(supplier, obs1);
  }

  /**
   *
   * @param <T>
   * @param obs1
   * @param supplier
   */
  public static <T> void bindObject(Property<? super T> obs1, // 
    Supplier<? extends T> supplier, Property<?>... observables) {
    for (Property<?> observable : observables) {
      observable.addListener((obs, old, change) -> {
        try {
          setObjectValueFromCallable(supplier, obs1);
        } catch (Exception ex) {
          String message = String.format("An error occurred setting value to observable from callable.  "
            + "Check args: {"
            + "observable : '%s'"
            + ", callable : '%s'"
            + "}", obs1, supplier);
          throw new RuntimeException(message, ex);
        }
      });
    }
    setObjectValueFromCallable(supplier, obs1);
  }

  /**
   *
   * @param obs2
   * @param obs1
   * @throws RuntimeException
   */
  private static void setValueFromCallable(Supplier<Boolean> obs2, Property<Boolean> obs1) {
    Boolean r;
    try {
      r = obs2.get();
    } catch (Exception ex) {
      throw new RuntimeException("An error occurred while obtaining bound boolean value.", ex);
    }
    obs1.setValue(r);
  }

  /**
   *
   * @param obs2
   * @param obs1
   * @throws RuntimeException
   */
  private static <T> void setObjectValueFromCallable(Supplier<? extends T> obs2, Property<? super T> obs1) {
    T r;
    try {
      r = obs2.get();
    } catch (Exception ex) {
      throw new RuntimeException("An error occurred while obtaining bound boolean value.", ex);
    }
    try {
      obs1.setValue(r);
    } catch(Exception ex) {
      throw new RuntimeException(
        String.format("Attempting to set value '%s' to observable '%s'", r, obs1), ex);
    }
    
  }

  /**
   *
   * @param obs1
   * @param observables
   */
  public static void bindTrueIfAnyNullOrEmpty(Property<Boolean> obs1,
    Property<? extends Object>... observables) {
    for (Property<? extends Object> observable : observables) {
      observable.addListener((obs, old, change) -> {
        if (anyNull(observables)) {
          obs1.setValue(true);
        } else {
          obs1.setValue(false);
        }
      });
    }
    if (anyNull(observables)) {
      obs1.setValue(true);
    } else {
      obs1.setValue(false);
    }
  }

  /**
   *
   * @param obs1
   * @param observables
   */
  public static void bindNullIfAnyChange(Property<? extends Object> obs1,
    Property<? extends Object>... observables) {
    for (Property<? extends Object> observable : observables) {
      observable.addListener((obs, old, change) -> {
        obs1.setValue(null);
      });
    }
    obs1.setValue(null);
  }

  /**
   *
   * @param obs1
   * @param observables
   */
  public static void bindFalseIfAnyNullOrEmpty(Property<Boolean> obs1,
    Property<? extends Object>... observables) {
    for (Property<? extends Object> observable : observables) {
      observable.addListener((obs, old, change) -> {
        if (anyNull(observables)) {
          obs1.setValue(false);
        } else {
          obs1.setValue(true);
        }
      });
    }
    if (anyNull(observables)) {
      obs1.setValue(false);
    } else {
      obs1.setValue(true);
    }
  }

  /**
   *
   * @return
   */
  public static boolean anyNull(Property<? extends Object>... observables) {
    boolean result = false;
    for (Property<? extends Object> observable : observables) {
      Object value = observable.getValue();
      if (value == null) {
        result = true;
        break;
      }
      if (value instanceof String && ((String) value).isEmpty()) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   *
   * @return
   */
  public static boolean anyNull(Object... objects) {
    boolean result = false;
    for (Object value : objects) {
      if (value == null) {
        result = true;
        break;
      }
      if (value instanceof String && ((String) value).isEmpty()) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   *
   * @param textProperty
   * @param observables
   */
  public static void bindClearIfAnyChange(StringProperty textProperty,
    Property<? extends Object>... observables) {
    for (Property<? extends Object> observable : observables) {
      observable.addListener((obs, old, change) -> {
        textProperty.set(null);
      });
    }
  }

  /**
   *
   */
  public static void bindActionOnAnyChange(Runnable runnable, ReadOnlyProperty<? extends Object>... observables) {
    for (ReadOnlyProperty<? extends Object> observable : observables) {
      observable.addListener((obs, old, change) -> {
        runnable.run();
      });
    }
  }

  /**
   *
   */
  public static void bindActionOnAnyChange(Runnable runnable, Property<? extends Object>... observables) {
    for (ReadOnlyProperty<? extends Object> observable : observables) {
      observable.addListener((obs, old, change) -> {
        runnable.run();
      });
    }
  }
}
