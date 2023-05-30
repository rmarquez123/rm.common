package common;

/**
 *
 * @author Ricardo Marquez
 */
public class RmTimer {
  
  /**
   * 
   */
  private long startTime;
  
  /**
   * 
   * @return 
   */
  public static RmTimer start() {
    RmTimer result = new RmTimer(System.currentTimeMillis());
    return result;
  }


  /**
   * 
   * @param startTime 
   */
  private RmTimer(long startTime) {
    this.startTime = startTime;
  }
  
  /**
   * 
   */
  public void endAndPrint() {
    long endTime = System.currentTimeMillis();
    System.out.println("Elapsed time : " + (endTime - this.startTime) / 1000 + "s");
    this.startTime = endTime;
  }
  
  /**
   * 
   */
  public void endAndPrint(String message) {
    long endTime = System.currentTimeMillis();
    System.out.println(message + "Elapsed time : " + (endTime - this.startTime) / 1000 + "s");
    this.startTime = endTime;
  }
}
