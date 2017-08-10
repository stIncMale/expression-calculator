package stinc.male.exrpcalculator.logic;

import javax.annotation.Nullable;
import stinc.male.exrpcalculator.Main;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static stinc.male.exrpcalculator.logic.ExpressionParser.isEmptySymbol;

public final class CalculationException extends RuntimeException {
  private static final long serialVersionUID = 0;

  private final int problemIdx;
  @Nullable
  private String expr;

  CalculationException(
      @Nullable final String expr,
      @Nullable final String message,
      @Nullable final Throwable cause) {
    this(-1, expr, message, cause);
  }

  /**
   * @param problemIdx Negative when it is impossible to specify the exact place that caused the exception.
   */
  CalculationException(
      final int problemIdx,
      @Nullable final String expr,
      @Nullable final String message,
      @Nullable final Throwable cause) {
    super(
        (message == null && expr != null)
            ? description(problemIdx, expr)
            : message,
        cause);
    this.problemIdx = problemIdx;
    this.expr = expr;
  }

  final boolean isExpressionSet() {
    return expr != null;
  }

  final void setExpression(final String expr) {
    checkNotNull(expr, "The argument %s must not be null", "expr");
    checkState(this.expr == null, "Expression has already been set");
    this.expr = expr;
  }

  public final String description() {
    checkState(this.expr != null, "Expression has not been set");
    return description(problemIdx, expr);
  }

  private static final String description(final int problemIdx, final String expr) {
    final String result;
    if (problemIdx < 0) {
      result = String.format("Can not calculate expression: %s", expr);
    } else {
      final String spaces;
      {
        final char[] spaceChars = new char[problemIdx];
        for (int i = 0; i < spaceChars.length; i++) {
          final char symbolToSubstitute = expr.charAt(i);
          if (isEmptySymbol(symbolToSubstitute)) {
            spaceChars[i] = symbolToSubstitute;
          } else {
            spaceChars[i] = ' ';
          }
        }
        spaces = new String(spaceChars);
      }
      result = String.format("Unexpected symbol '%s' at index %s:", expr.charAt(problemIdx), problemIdx) + Main.LINE_SEPARATOR
          + expr + Main.LINE_SEPARATOR
          + spaces + '^';
    }
    return result;
  }
}