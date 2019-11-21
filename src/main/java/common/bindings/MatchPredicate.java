package common.bindings;

/**
 *
 * @author Ricardo Marquez
 */
@FunctionalInterface
public interface MatchPredicate<T1, T2> {
    
  /**
   * 
   * @param o1
   * @param o2
   * @return 
   */
  public boolean matches(T1 o1, T2 o2);
  
}
