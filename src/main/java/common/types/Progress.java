package common.types;

/**
 *
 * @author Ricardo Marquez
 */
public class Progress {

  /**
   * 
   * @param i
   * @param size
   * @param messageFormat
   * @return 
   */
  public static Progress create(int i, int size, String messageFormat) {
    String message = String.format(messageFormat, i, size);
    double progressValue = (double) i/((double) size) * 100;
    Progress result = new Progress(progressValue, message);
    return result;
  }

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
