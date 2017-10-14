package stinc.male.exprcalculator.arg;

import org.junit.jupiter.api.Test;
import com.beust.jcommander.ParameterException;
import java.math.MathContext;
import org.apache.logging.log4j.Level;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class TestArgs {
  public TestArgs() {
  }

  @Test
  public final void loggingLevel1() {
    assertEquals(Level.ERROR, new Args().getLoggingLevel());//default value
  }

  @Test
  public final void loggingLevel2() {
    assertEquals(Level.OFF, new Args(new String[] {"-v", "OFF"}).getLoggingLevel());
  }

  @Test
  public final void loggingLevel3() {
    assertEquals(Level.WARN, new Args(new String[] {"-log", "warn"}).getLoggingLevel());
  }

  @Test
  public final void loggingLevel4() {
    assertThrows(ParameterException.class, () -> new Args(new String[] {"-v"}));
  }

  @Test
  public final void loggingLevel5() {
    assertThrows(ParameterException.class, () -> new Args(new String[] {"-log", ""}));
  }

  @Test
  public final void loggingLevel6() {
    assertThrows(ParameterException.class, () -> new Args(new String[] {"-v", "fck"}));
  }

  @Test
  public final void mathContext1() {
    assertEquals(MathContext.DECIMAL32, new Args().getMathContext());//default value
  }

  @Test
  public final void mathContext2() {
    assertEquals(10,
        new Args(new String[] {"-p", "10"}).getMathContext()
            .getPrecision());
  }

  @Test
  public final void constructor1() {
    assertEquals(Level.OFF, new Args(new String[] {"-v", "OFF", "input"}, 0, 1).getLoggingLevel());
  }

  @Test
  public final void constructor2() {
    assertThrows(ParameterException.class, () -> new Args(new String[] {"-v", "OFF", "input"}, 0, 2));
  }
}