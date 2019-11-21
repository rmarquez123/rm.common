/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedParamStatement {

  private final PreparedStatement prepStmt;
  private final List<String> fields = new ArrayList<>();

  public NamedParamStatement(Connection conn, String sql) throws SQLException {
    Pattern findParametersPattern = Pattern.compile("(?<!')(:[\\w]*)(?!')");
    Matcher matcher = findParametersPattern.matcher(sql);
    while (matcher.find()) {
      String substring = matcher.group().substring(1);
      if (!substring.isEmpty()) {
        fields.add(substring);
      }
    }
    prepStmt = conn.prepareStatement(sql.replaceAll(findParametersPattern.pattern(), "?"));
  }

  public PreparedStatement getPreparedStatement() {
    return prepStmt;
  }

  public ResultSet executeQuery() throws SQLException {
    return prepStmt.executeQuery();
  }

  public void close() throws SQLException {
    prepStmt.close();
  }

  public void setInt(String name, int value) throws SQLException {
    List<Integer> index = getIndex(name);
    for (Integer integer : index) {
      prepStmt.setInt(integer, value);
    }
  } 
    
  public void setObject(String name, Object value) throws SQLException {
    List<Integer> index = getIndex(name);
    for (Integer integer : index) {
      if (value instanceof Date) {
        prepStmt.setObject(integer, new java.sql.Date(((Date) value).getTime()));
      } else {
        prepStmt.setObject(integer, value);
      }
      
    }
  }

  private List<Integer> getIndex(String name) {
    List<Integer> result = new ArrayList<>();
    int index = 0;
    for (String field : this.fields) {
      index++;
      if (field.equals(name)) {
        result.add(index);
      }
    }
    return result;
  }

}
