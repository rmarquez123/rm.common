package common.db;

import java.sql.ResultSet;

/**
 *
 * @author rmarq
 */
public interface ResultMapper<T> {

  /**
   *
   * @param result
   * @return
   */
  T map(ResultSet result);
}
