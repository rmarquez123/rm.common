
package common.elevations;

import org.junit.Test;

/**
 *
 * @author Ricardo Marquez
 */
public class NationalMapElevationServiceIT {
  
  public NationalMapElevationServiceIT() {
  }
  
  /**
   * 
   */
  @Test
  public void testSomeMethod() {
    NationalMapElevationService service = new NationalMapElevationService();
    double elevation = service.getElevation(37.36, -120.43); 
    System.out.println("elevation = " + elevation);
  }
  
}
