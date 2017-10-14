package stinc.male.exprcalculator.logic;

import java.math.MathContext;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.ThreadSafe;
import static com.google.common.base.Preconditions.checkNotNull;

@ThreadSafe final class ParsedExpression {
  private final String expr;
  private final MathContext mc;

  ParsedExpression(final String expr, final MathContext mc) {
    checkNotNull(expr, "The argument %s must not be null", "expr");
    checkNotNull(mc, "The argument %s must not be null", "mc");
    this.expr = expr;
    this.mc = mc;
  }

  final String getExpression() {
    return expr;
  }

  /**
   * @return A new {@link Stream}{@code <}{@link Word}{@code >} which represents the string expression
   * supplied in the {@linkplain #ParsedExpression(String, MathContext) constructor}.
   */
  final Stream<Word> stream() throws CalculationException {
    return StreamSupport.stream(new ExpressionSpliterator(expr, mc), false);
  }

  @Override
  public final String toString() {
    return getClass().getSimpleName() + "{expr='" + expr + "'" + ", mc=" + mc + '}';
  }
}