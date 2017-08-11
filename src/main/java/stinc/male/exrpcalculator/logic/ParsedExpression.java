package stinc.male.exrpcalculator.logic;

import java.math.MathContext;
import java.util.stream.Stream;
import javax.annotation.concurrent.Immutable;
import static com.google.common.base.Preconditions.checkNotNull;
import static stinc.male.exrpcalculator.logic.ExpressionParser.parse;

@Immutable
final class ParsedExpression {
  private final String expr;
  private final Stream<Word> words;

  ParsedExpression(final String expr, final MathContext mc) throws CalculationException {
    checkNotNull(expr, "The argument %s must not be null", "expr");
    checkNotNull(mc, "The argument %s must not be null", "mc");
    this.expr = expr;
    this.words = parse(expr, mc);
  }

  final String getExpression() {
    return expr;
  }

  final Stream<Word> getWords() {
    return words;
  }
}