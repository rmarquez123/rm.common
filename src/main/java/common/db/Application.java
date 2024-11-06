package common.db;

/**
 *
 * @author rmarq
 */
public interface Application {

  public String getName();
  
  
  public static Application create(String name) {
    return () -> name;
  }
}
