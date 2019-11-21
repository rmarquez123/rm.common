package common.bindings;

import java.util.function.UnaryOperator;
import javafx.scene.control.TextFormatter;

/**
 *
 * @author Ricardo Marquez
 */
public class IntegerTextFormatter extends TextFormatter<String> {

  public IntegerTextFormatter() {
    super(filter());
  }

  private static UnaryOperator<Change> filter() {
    UnaryOperator<Change> filter = change -> {
      String text = change.getText();
      if (text.matches("[0-9]*")) {
        return change;
      }
      return null;
    };
    return filter;
  }
}
