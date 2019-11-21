package common.db;

/**
 *
 * @author rmarquez
 */
public class RecordValueQuery {
  private final String idField; 
  private final String query; 

  public RecordValueQuery(String idField, String query) {
    if (idField == null || idField.isEmpty()) {
      throw new IllegalArgumentException("id field cannot be null or empty"); 
    }
    this.idField = idField;
    this.query = query;
  }

  public String getIdField() {
    return idField;
  }

  public String getQuery() {
    return query;
  }
  
  
  @Override
  public String toString() {
    return "RecordValueQuery{" + "idField=" + idField + ", query=" + query + '}';
  }
}
