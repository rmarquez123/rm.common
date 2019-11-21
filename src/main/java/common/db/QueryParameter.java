package common.db;

import javafx.beans.property.Property;

/**
 *
 * @author rmarquez
 */
public class QueryParameter {
  
  private final String name;
  private final Property<?> value;
  
  /**
   * 
   * @param name
   * @param observable 
   */
  public QueryParameter(String name, Property<?> observable) {
    this.name = name;
    this.value = observable;
  }

  public String getName() {
    return name;
  }
  
  /**
   * 
   * @return 
   */
  public Object getValue() {
    return this.value.getValue();
  }
  
  public Property<?> getValueProperty() {
    return value;
  }
  
  
}
