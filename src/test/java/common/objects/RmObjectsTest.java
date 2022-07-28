package common.objects;

import common.RmObjects;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Ricardo Marquez
 */
@RunWith(JUnitParamsRunner.class)
public class RmObjectsTest {

  @Test
  @Parameters({
    "wpls.cmd.fcst.202005061400.log, true",
    "a wpls.cmd.fcst.202005061400.log, false",
    "wpls.cmd.fcst.202005061400_00.log, false"
  })
  public void test(String text, boolean expresult) throws Exception {
    String expression = "^wpls\\.cmd\\.fcst\\.(\\d{12})\\.log$";
    boolean result = RmObjects.contains(text, expression);
    Assert.assertEquals("", result, expresult);
  }
}
