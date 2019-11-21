package common.bindings;

/**
 *
 * @author Ricardo Marquez
 */
public interface ObjectConverter<T1, T2> {
  
  public T2 toObject(T1 reference);
  public T1 fromObject(T2 reference);
}
