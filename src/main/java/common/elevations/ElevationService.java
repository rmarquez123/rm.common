package common.elevations;

/**
 *
 * @author Ricardo Marquez
 */
public interface ElevationService {
  
  
  /**
   * 
   * @param lat
   * @param lon
   * @return 
   */
  public double getElevation(double lat, double lon);
}
