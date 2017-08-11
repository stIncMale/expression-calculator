package stinc.male.exrpcalculator.logic;

import java.math.MathContext;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import static com.google.common.base.Preconditions.checkNotNull;
import static stinc.male.exrpcalculator.logic.ExpressionParser.parse;

@Immutable
final class ParsedExpression {
  private final String expr;
  private final List<Word> words;

  ParsedExpression(final String expr, final MathContext mc) throws CalculationException {
    checkNotNull(expr, "The argument %s must not be null", "expr");
    checkNotNull(mc, "The argument %s must not be null", "mc");
    this.expr = expr;
    this.words = Collections.unmodifiableList(parse(expr, mc));
  }

  final String getExpression() {
    return expr;
  }

  final List<Word> getWords() {
    return words;
  }
}