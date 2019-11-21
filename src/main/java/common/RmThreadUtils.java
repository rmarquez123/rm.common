package common;

/**
 *
 * @author Ricardo Marquez
 */
public class RmThreadUtils {

  public static class ThreadBuilder {

    private String name;
    private Runnable runnable;
    private Runnable onFinally;

    public ThreadBuilder(String name) {
      this.name = name;
    }

    public ThreadBuilder setRunnable(Runnable runnable) {
      this.runnable = runnable;
      return this;
    }

    public ThreadBuilder setOnFinally(Runnable onFinally) {
      this.onFinally = onFinally;
      return this;
    }

    /**
     *
     */
    public void start() {
      this.create().start();
    }

    /**
     *
     * @return
     */
    public Thread create() {
      Runnable wrapper = () -> {
        try {
          this.runnable.run();
        } finally {
          if (this.onFinally != null) {
            this.onFinally.run();
          }

        }
      };
      return new Thread(wrapper, this.name);
    }
  }
}
