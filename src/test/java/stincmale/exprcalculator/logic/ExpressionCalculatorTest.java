package stincmale.exprcalculator.logic;

import java.math.BigDecimal;
import java.math.MathContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*
 * Set level="debug" in log4j2.xml to enable logging.
 */
@TestInstance(Lifecycle.PER_METHOD)
public final class ExpressionCalculatorTest {
  private static final MathContext mc = MathContext.DECIMAL32;

  public ExpressionCalculatorTest() {
  }

  @Test
  public final void calculate1() {
    final ExpressionCalculator calc = new ExpressionCalculator(mc);
    assertEquals(3,
        calc.calculate("add(1, 2)")
            .intValueExact());
    assertEquals(7,
        calc.calculate("add(1, mult(2, 3))")
            .intValueExact());
    assertEquals(12,
        calc.calculate("mult(add(2, 2), div(9, 3))")
            .intValueExact());
    assertEquals(10,
        calc.calculate("let(a, 5, add(a, a))")
            .intValueExact());
    assertEquals(55,
        calc.calculate("let(a, 5, let(b, mult(a, 10), add(b, a)))")
            .intValueExact());
    assertEquals(40,
        calc.calculate("let(a, let(b, 10, add(b, b)), let(b, 20, add(a, b)))")
            .intValueExact());
    assertEquals(1,
        Math.round(calc.calculate("mult  (3,div(1, 3) )")
            .doubleValue()));
    assertEquals(22,
        calc.calculate("let(a, let(b, let(d, 1, div(1, d)), add(b, b)), let(b, 20, add(a, b)))")
            .intValueExact());
    assertEquals(1,
        calc.calculate("let(a, 1, add(let(b, 2, div(b, 1)), -1))")
            .intValueExact());
    assertEquals(3,
        calc.calculate("add(let(a, 1, a), let(a, 2, a))")
            .intValueExact());
    assertEquals(-1,
            calc.calculate("let(d, sub(mult(1, 2), div(9, 3)), add(d, 0))")
                    .intValueExact());
    assertEquals(-38,
            calc.calculate("let(d, sub(mult(add(8, 13), 1), let(a, let(b, 10, add(b, b)), let(c, 20, add(a, c)))), add(div(d, 1), d))")
                    .intValueExact());
    assertEquals(new BigDecimal("-16.69230", mc),
            calc.calculate("let(d, sub(mult(add(8, 13), div(28, 24)), let(a, let(b, 10, add(b, b)), let(c, 20, add(a, c)))), add(div(d, 13), d))"));
    assertEquals(new BigDecimal("-3.14", mc), calc.calculate("-3.14"));
  }

  @Test
  public final void calculate2() {
    final ExpressionCalculator calc = new ExpressionCalculator(mc);
    assertThrows(CalculationException.class, () -> calc.calculate("(123)"));
    assertThrows(CalculationException.class, () -> calc.calculate("let(a, 1, add(let(a, 2, div(a, 1)), 1))"));
    assertThrows(CalculationException.class, () -> calc.calculate("let(add, 1, div(a, 1))"));
    assertThrows(CalculationException.class, () -> calc.calculate("a"));
    assertThrows(CalculationException.class, () -> calc.calculate("let"));
    assertThrows(CalculationException.class, () -> calc.calculate("+"));
    assertThrows(CalculationException.class, () -> calc.calculate("let(a)"));
    assertThrows(CalculationException.class, () -> calc.calculate("let(a, a)"));
    assertThrows(CalculationException.class, () -> calc.calculate("let(let(a, 1, 2), 1, 2)"));
    assertThrows(CalculationException.class, () -> calc.calculate("add(1,,2)"));
    assertThrows(CalculationException.class, () -> calc.calculate("add(let(a, 1, a), a)"));
    assertThrows(CalculationException.class, () -> calc.calculate("div(0,0)"));
  }

  @Test
  public final void calculate3() {
    final ExpressionCalculator calc = new ExpressionCalculator(mc);
    assertEquals(1,
        Math.round(calc.calculate("mult(3, div(1, 3))")
            .doubleValue()));
  }
}