package common.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rmarquez
 */
public final class SqlUtils {
  /**
   *
   */
  private SqlUtils() {
  }

  /**
   *
   * @param connection
   * @param query
   * @return
   */
  public static List<RecordValue> executeListQuery(DbConnection connection,
          String query, QueryParameters queryParams, String idField) throws SQLException {
    Connection conn = connection.getConnection();
    NamedParamStatement statement = new NamedParamStatement(conn, query);
    for (QueryParameter queryParam : queryParams) {
      String paramName = queryParam.getName();
      Object paramValue = queryParam.getValue();
      statement.setObject(paramName, paramValue);
    }
    
    ResultSetMetaData metaData = statement.getPreparedStatement().getMetaData();
    if (metaData == null) {
      throw new SQLException("Invalid query : '" + query  + "'"); 
    }
    int colCont = metaData.getColumnCount();
    List<RecordValue> result = new ArrayList<>();
    ResultSet r = statement.executeQuery();
    while (r.next()) {
      Map<String, Object> values = new HashMap<>();
      for (int colIndex = 1; colIndex <= colCont; colIndex++) {
        Object object = r.getObject(colIndex); 
        String colName = metaData.getColumnName(colIndex);
        values.put(colName, object); 
      }
      if (!values.containsKey(idField)) {
        throw new RuntimeException("Id field is not present in result set.  Check args: {"
                + "idField = "  + idField
                + ", query = "  + query
                + "} "); 
      }
      RecordValue record = new RecordValue(idField, values); 
      result.add(record); 
    }
    return result;
  }
  

}
