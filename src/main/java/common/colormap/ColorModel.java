package common.colormap;

import javafx.scene.paint.Color;

/**
 *
 * @author Ricardo Marquez
 */

public interface ColorModel {
    
  /**
   * 
   * @param value
   * @return 
   */
  public Color getColor(double value); 
  
}
