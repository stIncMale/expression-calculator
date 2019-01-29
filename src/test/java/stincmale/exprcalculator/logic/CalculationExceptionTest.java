package stincmale.exprcalculator.logic;

import java.math.MathContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import stincmale.exprcalculator.logic.Word.Type;
import stincmale.exprcalculator.Main;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(Lifecycle.PER_METHOD)
public final class CalculationExceptionTest {
  public CalculationExceptionTest() {
  }

  @Test
  public final void description1() {
    final String expected = "Problem with '567' at index 5:" + Main.LN + "0123456789" + Main.LN + "     ^";
    assertEquals(expected,
        new CalculationException(new Word("567", Type.LITERAL, 5, MathContext.DECIMAL32, new Word.LogicalTypeValuePair()), "0123456789")
            .description());
  }

  @Test
  public final void description2() {
    final String expected = "Problem with '5' at index 5:" + Main.LN + "\t1\n3456789" + Main.LN + "\t \n  ^";
    assertEquals(expected, new CalculationException(5, "\t1\n3456789").description());
  }
}