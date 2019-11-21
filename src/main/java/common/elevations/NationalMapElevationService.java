package common.elevations;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

/**
 *
 * @author Ricardo Marquez
 */
public class NationalMapElevationService implements ElevationService {

  /**
   *
   * @param lat
   * @param lon
   * @return
   */
  @Override
  public double getElevation(double lat, double lon) {
//    String urlText = String.format("https://nationalmap.gov/epqs/pqs.php?x=%f&y=%f&units=Meters&output=json", lon, lat);
    String urlText = String.format("https://ned.usgs.gov/epqs/pqs.php?x=%f&y=%f&units=Meters&output=json", lon, lat);
    double elevation;
    try {
      String jsonText;
      try (InputStream is = new URL(urlText).openStream()) {
        jsonText = String.join("\n", IOUtils.readLines(is, Charset.forName("utf8")));
      }
      JSONObject jsonObj = new JSONObject(jsonText);
      JSONObject elevQuery = jsonObj
        .getJSONObject("USGS_Elevation_Point_Query_Service")
        .getJSONObject("Elevation_Query");
      elevation = elevQuery.getDouble("Elevation");
    } catch (Exception ex) {
      throw new RuntimeException(
        String.format("Error on getting elevation for lat, lon = (%f, %f)\n. Url text is : %s", lat, lon, urlText), 
        ex); 
    }
    return elevation;
  }

}
