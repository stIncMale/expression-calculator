package stinc.male.exrpcalculator.logic;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static stinc.male.exrpcalculator.Main.LN;

public final class TestCalculationException {
  public TestCalculationException() {
  }

  @Test
  public final void testDescription1() {
    final String expected =
        "Problem with '5' at index 5:" + LN
      + "0123456789" + LN
      + "     ^";
    assertEquals(expected, new CalculationException(5, "0123456789", null, null).description());
  }

  @Test
  public final void testDescription2() {
    final String expected =
        "Problem with '5' at index 5:" + LN
            + "\t1\n3456789" + LN
            + "\t \n  ^";
    assertEquals(expected, new CalculationException(5, "\t1\n3456789", null, null).description());
  }
}