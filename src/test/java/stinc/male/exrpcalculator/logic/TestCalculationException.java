package stinc.male.exrpcalculator.logic;

import org.junit.Test;
import stinc.male.exrpcalculator.Main;
import static org.junit.Assert.assertEquals;

public final class TestCalculationException {
  public TestCalculationException() {
  }

  @Test
  public final void testDescription1() {
    final String expected =
        "Unexpected symbol '5' at index 5:" + Main.LINE_SEPARATOR
      + "0123456789" + Main.LINE_SEPARATOR
      + "     ^";
    assertEquals(expected, new CalculationException(5, "0123456789", null, null).description());
  }

  @Test
  public final void testDescription2() {
    final String expected =
        "Unexpected symbol '5' at index 5:" + Main.LINE_SEPARATOR
            + "\t1\n3456789" + Main.LINE_SEPARATOR
            + "\t \n  ^";
    assertEquals(expected, new CalculationException(5, "\t1\n3456789", null, null).description());
  }
}