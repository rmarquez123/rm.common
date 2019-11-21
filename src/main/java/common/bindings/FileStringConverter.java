package common.bindings;

import java.io.File;
import javafx.util.StringConverter;

/**
 *
 * @author Ricardo Marquez
 */
public class FileStringConverter extends StringConverter<File> {

  @Override
  public String toString(File object) {
    return object == null ? "" : object.toString();
  }

  @Override
  public File fromString(String string) {
    return (string == null || string.isEmpty()) ? null : new File(string); 
  }
  
}
