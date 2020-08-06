package common.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 * @author rmarquez
 */
public class RecordValue {

  private final String idField;
  private final Map<String, Object> values = new TreeMap<>();

  /**
   * If idField is a composite, then the name must end with pk or the object value must be
   * a map.
   *
   * @param idField
   * @param values
   */
  public RecordValue(String idField, Map<String, Object> values) {
    Objects.requireNonNull(idField, "Id field cannot be null");
    Objects.requireNonNull(values, "Values cannot be null");
    this.idField = idField;
    this.values.putAll(values);
  }

  /**
   *
   * @param key
   * @param value
   * @return
   */
  public RecordValue set(String key, Object value) {
    Map<String, Object> map = new HashMap<>(this.values);
    map.put(key, value);
    RecordValue result = new RecordValue(this.idField, map);
    return result;
  }

  /**
   *
   * @return
   */
  public Object getIdValue() {
    Object result = this.get(this.idField);
    return result;
  }

  /**
   *
   * @param key
   * @return
   */
  public Object get(String key) {
    Object result;
    if (this.values.containsKey(key)) {
      result = this.values.get(key);
    } else if (this.getIdValue() instanceof Map) {
      result = ((Map<String, Object>) this.getIdValue()).get(key);
    } else {
      result = null;
    }
    return result;
  }

  @Override
  public String toString() {
    return "RecordValue{" + "idField=" + idField + ", values=" + values + '}';
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 89 * hash + Objects.hashCode(this.getIdValue());
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
    final RecordValue other = (RecordValue) obj;
    if (!Objects.equals(this.getIdValue(), other.getIdValue())) {
      return false;
    }
    return true;
  }

  /**
   *
   * @return
   */
  public Set<String> keySet() {
    Set<String> result;
    if (this.get(this.idField) instanceof Map) {
      Map<String, Object> composite = (Map<String, Object>) this.get(this.idField);
      result = new HashSet<>();
      for (String string : composite.keySet()) {
        result.add(string);
      }
      result.addAll(this.keySetNoPk());
    } else {
      result = new HashSet<>(this.values.keySet());
    }
    return result;
  }

  /**
   *
   * @return
   */
  public List<Object> valueSet() {
    return new ArrayList<>(this.values.values());
  }
  
  
  /**
   * 
   * @return 
   */
  public Set<String> keySetNoPk() {
    Set<String> keySet = new HashSet<>(this.values.keySet());
    keySet.removeIf((e) -> e.endsWith("pk") || this.get(e) instanceof Map);
    return keySet;
  }

  /**
   *
   * @return
   */
  public List<Object> valueSetNoPk() {
    List<Object> result = this.keySetNoPk().stream()
      .map((k) -> this.values.get(k))
      .collect(Collectors.toList());
    return result;
  }

  /**
   *
   * @param keys
   * @return
   */
  public List<Object> valueSet(HashSet<String> keys) {
    List<Object> result = keys.stream()
      .map((k) -> this.values.get(k))
      .collect(Collectors.toList());
    return result;
  }

}
