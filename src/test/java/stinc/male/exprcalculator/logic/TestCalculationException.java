package stinc.male.exprcalculator.logic;

import java.math.MathContext;
import org.junit.jupiter.api.Test;
import stinc.male.exprcalculator.logic.Word.Type;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static stinc.male.exprcalculator.Main.LN;

public final class TestCalculationException {
  public TestCalculationException() {
  }

  @Test
  public final void description1() {
    final String expected = "Problem with '567' at index 5:" + LN + "0123456789" + LN + "     ^";
    assertEquals(expected,
        new CalculationException(new Word("567", Type.LITERAL, 5, MathContext.DECIMAL32, new Word.LogicalTypeValuePair()), "0123456789")
            .description());
  }

  @Test
  public final void description2() {
    final String expected = "Problem with '5' at index 5:" + LN + "\t1\n3456789" + LN + "\t \n  ^";
    assertEquals(expected, new CalculationException(5, "\t1\n3456789").description());
  }
}