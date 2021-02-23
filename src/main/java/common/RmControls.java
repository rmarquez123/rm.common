package common;

import java.util.function.Supplier;

/**
 *
 * @author Ricardo Marquez
 */
public class RmControls {

  private RmControls() {

  }

  public static ControlBuilder ifTrue(Supplier<Boolean> predicate) {
    ControlBuilder builder = new ControlBuilder();
    builder.setPredicate(predicate);
    return builder;
  }

  public static class ControlBuilder {
    private Supplier<Boolean> predicate;
    private Runnable ifDo;
    private Runnable elseDo;

    public void setPredicate(Supplier<Boolean> predicate) {
      this.predicate = predicate;
    }

    public ControlBuilder thenDo(Runnable ifDo) {
      this.ifDo = ifDo;
      return this;
    }

    public ControlBuilder elseDo(Runnable elseDo) {
      this.elseDo = elseDo;
      return this;
    }
    
    public void start() {
      if (this.predicate.get()) {
        this.ifDo.run();
      } else {
        this.elseDo.run();
      }
    }
  }
}
