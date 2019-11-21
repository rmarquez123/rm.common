package common.types;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

/**
 *
 * @author Ricardo Marquez
 */
public class ZoneIdCollection {

  private final ObservableSet<ZoneId> zoneIds = FXCollections.observableSet();

  /**
   *
   */
  public ZoneIdCollection() {
  }

  /**
   *
   * @param zoneIdText
   */
  public void add(String zoneIdText) {
    this.zoneIds.add(ZoneId.of(zoneIdText));
  }
  
  /**
   *
   * @param zoneIdText
   */
  public void remove(String zoneIdText) {
    this.zoneIds.remove(ZoneId.of(zoneIdText));
  }

  /**
   *
   */
  public Set<ZoneId> asSet() {
    return new HashSet<>(this.zoneIds);
  }

  /**
   *
   */
  public ObservableSet<ZoneId> asObservableSet() {
    return this.zoneIds;
  }

  /**
   *
   * @param zoneIdTexts
   * @return
   */
  public static ZoneIdCollection create(String... zoneIdTexts) {
    ZoneIdCollection result = new ZoneIdCollection();
    for (String zoneIdText : zoneIdTexts) {
      result.add(zoneIdText);
    }
    return result;
  }

  
  /**
   * 
   * @param of
   * @return 
   */
  public ZoneId substitute(ZoneId of) {
    return this.zoneIds.stream()
      .filter((z)->z.getRules().equals(of.getRules()))
      .findAny()
      .orElse(of);
  }

}
