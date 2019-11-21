
package common.colormap;

import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 *
 * @author Ricardo Marquez
 */
public class LinearRangeColorModel implements ColorModel{

  private final Stop minStop;

  private final Stop maxStop;
  
  /**
   *
   * @param minStop
   * @param maxStop
   */
  public LinearRangeColorModel(Stop minStop, Stop maxStop) {
    if (minStop.getOffset() > maxStop.getOffset()) {
      throw new IllegalArgumentException("Min stop offset is greater than max stop offset"); 
    }
    this.minStop = minStop;
    this.maxStop = maxStop;
  }
  
  /**
   * 
   * @param value
   * @return 
   */
  @Override
  public Color getColor(double value) {
    double minVal = this.minStop.getOffset(); 
    double maxVal = this.maxStop.getOffset(); 
    Color result;
    if (value < minVal) {
      result = this.minStop.getColor(); 
    } else if (value > maxVal) {
      result = this.maxStop.getColor();
    } else {
      result = this.interpolateImpl(value); 
    }
    return result;
  }

  /**
   * 
   * @param value
   * @return
   */
  private Color interpolateImpl(double value) {
    Color result;
    LinearInterpolator interpolator = new LinearInterpolator();
    double[] x = new double[]{this.minStop.getOffset(), this.maxStop.getOffset()};
    double[] reds = new double[]{
      this.minStop.getColor().getRed(),
      this.maxStop.getColor().getRed()
    };
    double[] blues = new double[]{
      this.minStop.getColor().getBlue(),
      this.maxStop.getColor().getBlue()
    };
    double[] greens = new double[]{
      this.minStop.getColor().getGreen(),
      this.maxStop.getColor().getGreen()
    };
    PolynomialSplineFunction red = interpolator.interpolate(x, reds);
    PolynomialSplineFunction blue = interpolator.interpolate(x, blues);
    PolynomialSplineFunction green = interpolator.interpolate(x, greens);
    result = Color.color(red.value(value), green.value(value), blue.value(value));
    return result;
  }

  /**
   * 
   * @return 
   */
  public Stop[] getStops() {
    return new Stop[]{this.minStop, this.maxStop}; 
  }
  
}
