package common.elevations;

import common.process.ProcessFacade;
import java.io.File;
import java.util.HashMap;
import org.junit.Test;

/**
 *
 * @author Ricardo Marquez
 */
public class RmThreadUtilsIT {

  @Test
  public void test_01() throws Exception {
    File file = new File("E:\\installer\\installs\\data\\dumps\\nypa_model_data.sql");
    File dbBinDir = new File("E:\\tests\\wpl\\installation_07\\applications\\PostGres\\bin");
    String url = "localhost";
    String dbName = "nypa";
    String user = "postgres";
    int port = 5436;
    String password = "postgres";
    String statement = String
      .format("%s\\psql.exe -h %s -d %s -U %s -p %d -a -q -f \"%s\"", 
        dbBinDir, url, dbName, user, port, file);
    HashMap<String, String> hashMap = new HashMap<>();
    hashMap.put("PGPASSWORD", password);
    new ProcessFacade.Builder()
      .withStatement(statement)
      .withEnvironmentVars(hashMap)
      .withOutputProcessor(System.out::println)
      .run();
    System.out.println("done");
  }
}
