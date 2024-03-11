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
    double elapsedTime = ((double) endTime - (double) this.startTime) / 1000d;
    System.out.println("Elapsed time : " + elapsedTime + "s");
    this.startTime = endTime;
  }

  /**
   *
   */
  public void endAndPrintInMillis() {
    long endTime = System.currentTimeMillis();
    System.out.println("Elapsed time : " + (endTime - this.startTime) + "ms");
    this.startTime = endTime;
  }

  /**
   *
   * @param message
   */
  public void endAndPrint(String message) {
    long endTime = System.currentTimeMillis();
    double elapsedTime = ((double) endTime - (double) this.startTime) / 1000d;
    System.out.println(message + "Elapsed time : " + elapsedTime + "s");
    this.startTime = endTime;
  }
}
