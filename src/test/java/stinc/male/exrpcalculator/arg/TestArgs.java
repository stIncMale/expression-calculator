package stinc.male.exrpcalculator.arg;

import com.beust.jcommander.ParameterException;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public final class TestArgs {
  public TestArgs() {
  }

  @Test
  public final void testLoggingLevel1() {
    assertEquals(Level.ERROR, new Args().getLoggingLevel());//default value
  }

  @Test
  public final void testLoggingLevel2() {
    assertEquals(Level.OFF, new Args(new String[] {"-v", "OFF"}).getLoggingLevel());
  }

  @Test
  public final void testLoggingLevel3() {
    assertEquals(Level.WARN, new Args(new String[] {"-log", "warn"}).getLoggingLevel());
  }

  @Test(expected = ParameterException.class)
  public final void testLoggingLevel4() {
    new Args(new String[] {"-v"});
  }

  @Test(expected = ParameterException.class)
  public final void testLoggingLevel5() {
    new Args(new String[] {"-log", ""});
  }

  @Test(expected = ParameterException.class)
  public final void testLoggingLevel6() {
    new Args(new String[] {"-v", "fck"});
  }

  @Test
  public final void testConstructor1() {
    assertEquals(Level.OFF, new Args(new String[] {"-v", "OFF", "input"}, 0, 1).getLoggingLevel());
  }

  @Test(expected = ParameterException.class)
  public final void testConstructor2() {
    new Args(new String[] {"-v", "OFF", "input"}, 0, 2);
  }
}