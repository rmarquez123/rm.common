package common.objects;

import common.geom.SridUtils;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;

/**
 *
 * @author rmarq
 */
public class SridUtilsTest {
  
  /**
   * 
   * @throws Exception 
   */
  @Test
  public void test() throws Exception {
    String polygon = "POLYGON ((-119.10636581500032 41.73321543400003, -119.10636581500032 47.50333288300006, -108.79033968600017 47.50333288300006, -108.79033968600017 41.73321543400003, -119.10636581500032 41.73321543400003))"; 
    GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    WKTReader reader = new WKTReader(factory);
    Geometry geometry = reader.read(polygon);
    Geometry transformed = SridUtils.transform(geometry, 3857);
    System.out.println(transformed);
  }
}
