package common.stringconverters;

import java.util.function.Function;
import javafx.util.StringConverter;

/**
 *
 * @author Ricardo Marquez
 */
public class SimpleStringConvertor<T> extends StringConverter<T>{

  private final Function<T, String> delegate;
  public SimpleStringConvertor(Function<T, String> delegate) {
    this.delegate = delegate;
  }
  
  @Override
  public String toString(T object) {
    return this.delegate.apply(object);
  }
  
  @Override
  public T fromString(String string) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
