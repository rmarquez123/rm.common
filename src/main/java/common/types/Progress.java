package common.types;

/**
 *
 * @author Ricardo Marquez
 */
public class Progress {

  private final String message;
  private final double progress;
    
  /**
   * 
   * @param progress
   * @param message 
   */
  public Progress(double progress, String message) {
    this.progress = progress;
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public double getProgress() {
    return progress;
  }

  @Override
  public String toString() {
    return "Progress{" + "message=" + message + ", progress=" + progress + '}';
  }
  
}
