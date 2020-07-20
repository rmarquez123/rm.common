package common.cache;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Ricardo Marquez
 */
public class MethodCacheInterceptor implements InvocationHandler {

  private final Object t;
  private final List<Method> methods;
  private final Map<MethodAndArgs, Object> cache = new HashMap<>();
  
  public MethodCacheInterceptor(Object t, List<Method> method) {
    this.t = t;
    this.methods = method;
  }

  /**
   *
   * @param proxy
   * @param method
   * @param args
   * @return
   * @throws Throwable
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object result;
    if (this.methods.stream().anyMatch(m -> matches(m, method))) {
      MethodAndArgs methodAndArgs = new MethodAndArgs(method, args);
      if (!this.cache.containsKey(methodAndArgs)) {
        Object r = method.invoke(t, args);
        this.cache.put(methodAndArgs, r);
      } 
      result = this.cache.get(methodAndArgs);
    } else {
      result = method.invoke(t, args);
    }
    return result;
  }

  /**
   *
   * @param m
   * @param method
   * @return
   */
  private static boolean matches(Method m, Method method) {
    return m.getName().equals(method.getName());
  }

  private static class MethodAndArgs {
    private final Method method;
    private final Object[] args;

    public MethodAndArgs(Method method, Object[] args) {
      this.method = method;
      this.args = args;
    }

    @Override
    public int hashCode() {
      int hash = 5;
      hash = 41 * hash + Objects.hashCode(this.method);
      hash = 41 * hash + Arrays.deepHashCode(this.args);
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final MethodAndArgs other = (MethodAndArgs) obj;
      if (!Objects.equals(this.method, other.method)) {
        return false;
      }
      if (!Arrays.deepEquals(this.args, other.args)) {
        return false;
      }
      return true;
    }
    
  }
}
