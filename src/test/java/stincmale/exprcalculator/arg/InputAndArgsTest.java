package stincmale.exprcalculator.arg;

import com.beust.jcommander.ParameterException;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(Lifecycle.PER_METHOD)
public final class InputAndArgsTest {
  public InputAndArgsTest() {
  }

  @Test
  public final void constructor1() {
    final InputAndArgs o = new InputAndArgs(new String[] {"-v", "OFF", "input"}, null);
    assertEquals("input", o.getInput());
    assertEquals(
        Level.OFF,
        o.getArguments()
            .getLoggingLevel());
  }

  @Test
  public final void constructor2() {
    final InputAndArgs o = new InputAndArgs(new String[] {"-v"}, null);
    assertEquals("-v", o.getInput());
    assertEquals(
        Level.ERROR,
        o.getArguments()
            .getLoggingLevel());
  }

  @Test
  public final void constructor3() {
    assertThrows(ParameterException.class, () -> new InputAndArgs(new String[] {"-v", "OFF"}, null));
  }
}