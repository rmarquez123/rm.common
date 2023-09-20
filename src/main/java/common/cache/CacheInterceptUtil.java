package common.cache;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ricardo Marquez
 */
public class CacheInterceptUtil {

  private CacheInterceptUtil() {
  }
  
  /**
   * 
   * @param <T>
   * @param a
   * @param interfaceClass
   * @return 
   */
  public static <T> T intercept(Object a, Class<T> interfaceClass) {
    Method[] methods = a.getClass().getDeclaredMethods();
    List<Method> methodslist = new ArrayList<>();
    for (Method method : methods) {
      if (method.getDeclaredAnnotation(CacheIntercept.class) != null) {
        methodslist.add(method); 
      }
    }
    T p = getProxy(a, interfaceClass, methodslist);
    return p;
  }
  
  /**
   * 
   * @param <T>
   * @param t
   * @return 
   */
  @SuppressWarnings("unchecked")
  private static <T> T getProxy(Object t, Class<T> interfaceClass, List<Method> method) {
    MethodCacheInterceptor handler = new MethodCacheInterceptor(t, method);
    ClassLoader classLoader = interfaceClass.getClassLoader();
    Class<?>[] name = new Class<?>[]{interfaceClass};
    T result = (T) Proxy.newProxyInstance(classLoader, name, handler);
    return result;
  }
}
