package stinc.male.exprcalculator.logic;

import java.math.BigDecimal;
import java.math.MathContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class TestExpressionCalculator {
  private static final MathContext mc = MathContext.DECIMAL32;

  public TestExpressionCalculator() {
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
    assertEquals(new BigDecimal("-3.14", mc), calc.calculate("-3.14"));
    assertEquals(3,
        calc.calculate("add(let(a, 1, a), let(a, 2, a))")
            .intValueExact());
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