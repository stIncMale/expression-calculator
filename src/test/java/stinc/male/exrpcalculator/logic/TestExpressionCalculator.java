package stinc.male.exrpcalculator.logic;

import java.math.MathContext;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public final class TestExpressionCalculator {
  public TestExpressionCalculator() {
  }

  @Test
  public final void calculate1() {
    final ExpressionCalculator calc = new ExpressionCalculator(MathContext.DECIMAL32);
    assertEquals(3, calc.calculate("add(1, 2)").intValueExact());
    assertEquals(7, calc.calculate("add(1, mult(2, 3))").intValueExact());
    assertEquals(12, calc.calculate("mult(add(2, 2), div(9, 3))").intValueExact());
    assertEquals(10, calc.calculate("let(a, 5, add(a, a))").intValueExact());
    assertEquals(55, calc.calculate("let(a, 5, let(b, mult(a, 10), add(b, a)))").intValueExact());
    assertEquals(40, calc.calculate("let(a, let(b, 10, add(b, b)), let(b, 20, add(a, b)))").intValueExact());
  }

  @Test
  public final void calculate2() {
    final ExpressionCalculator calc = new ExpressionCalculator(MathContext.DECIMAL32);
    assertEquals(1, Math.round(calc.calculate("mult(3, div(1, 3))").doubleValue()));
  }

  @Test
  public final void calculate3() {
    final ExpressionCalculator calc = new ExpressionCalculator(MathContext.DECIMAL32);
    assertEquals(22, calc.calculate("let(a, let(b, let(d, 1, div(1, d)), add(b, b)), let(b, 20, add(a, b)))").intValueExact());
  }

  @Test
  public final void calculate4() {
    final ExpressionCalculator calc = new ExpressionCalculator(MathContext.DECIMAL32);
    assertEquals(1, calc.calculate("let(a, 1, add(let(b, 2, div(b, 1)), -1))").intValueExact());
  }

//  @Test(expected = CalculationException.class)
//  public final void calculate5() {
//    final ExpressionCalculator calc = new ExpressionCalculator(MathContext.DECIMAL32);
//    assertEquals(1, calc.calculate("let(a, 1, add(let(a, 2, div(a, 1)), -1))").intValueExact());
//  }
}