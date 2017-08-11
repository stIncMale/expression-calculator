package stinc.male.exrpcalculator.arg;

import com.beust.jcommander.ParameterException;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public final class TestInputAndArgs {
  public TestInputAndArgs() {
  }

  @Test
  public final void constructor1() {
    final InputAndArgs o = new InputAndArgs(new String[] {"-v", "OFF", "input"}, null);
    assertEquals("input", o.getInput());
    assertEquals(Level.OFF, o.getArguments().getLoggingLevel());
  }

  @Test
  public final void constructor2() {
    final InputAndArgs o = new InputAndArgs(new String[] {"-v"}, null);
    assertEquals("-v", o.getInput());
    assertEquals(Level.ERROR, o.getArguments().getLoggingLevel());
  }

  @Test(expected = ParameterException.class)
  public final void constructor3() {
    new InputAndArgs(new String[] {"-v", "OFF"}, null);
  }
}