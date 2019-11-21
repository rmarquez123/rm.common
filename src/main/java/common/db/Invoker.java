
package common.db;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rmarquez
 */
public class Invoker {
  /**
   * list of listeners.
   */
  private final List<Listener> listeners = new ArrayList<>();

  /**
   * Add an Event listener.
   *
   * @param listener
   */
  public void addImportEventListener(Listener listener) {
    this.listeners.add(listener);
  }

  /**
   * Remove an Event listener. Removal is based on equals override
   *
   * @param listener (Should override equals if not the originally added
   * instance.)
   */
  public void removeImportEventListener(Listener listener) {
    this.listeners.remove(listener);
  }
  
  
  public void addListener(Listener listener) {
    this.listeners.add(listener); 
  }
  
  
  public void invoke() {
    List<Listener> copy = new ArrayList<>(this.listeners);
    Event evt = new Event();
    for (Listener listener : copy) {
      listener.onEvent(evt); 
    }
  }
  
  /**
   * 
   */
  public static class Event {
    
  }
  
  /**
   * 
   */
  public static interface Listener {
    void onEvent(Event evt);
  }
}
