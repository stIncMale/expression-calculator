package stinc.male.exprcalculator.arg;

import com.beust.jcommander.ParameterException;
import java.math.MathContext;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public final class TestArgs {
  public TestArgs() {
  }

  @Test
  public final void loggingLevel1() {
    assertEquals(Level.ERROR, new Args().getLoggingLevel());//default value
  }

  @Test
  public final void loggingLevel2() {
    assertEquals(Level.OFF, new Args(new String[]{"-v", "OFF"}).getLoggingLevel());
  }

  @Test
  public final void loggingLevel3() {
    assertEquals(Level.WARN, new Args(new String[]{"-log", "warn"}).getLoggingLevel());
  }

  @Test(expected = ParameterException.class)
  public final void loggingLevel4() {
    new Args(new String[]{"-v"});
  }

  @Test(expected = ParameterException.class)
  public final void loggingLevel5() {
    new Args(new String[]{"-log", ""});
  }

  @Test(expected = ParameterException.class)
  public final void loggingLevel6() {
    new Args(new String[]{"-v", "fck"});
  }

  @Test
  public final void mathContext1() {
    assertEquals(MathContext.DECIMAL32, new Args().getMathContext());//default value
  }

  @Test
  public final void mathContext2() {
    assertEquals(10, new Args(new String[]{"-p", "10"}).getMathContext().getPrecision());
  }

  @Test
  public final void constructor1() {
    assertEquals(Level.OFF, new Args(new String[]{"-v", "OFF", "input"}, 0, 1).getLoggingLevel());
  }

  @Test(expected = ParameterException.class)
  public final void constructor2() {
    new Args(new String[]{"-v", "OFF", "input"}, 0, 2);
  }
}