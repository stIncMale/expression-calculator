package stinc.male.exprcalculator.logic;

import javax.annotation.Nullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static stinc.male.exprcalculator.Main.LN;
import static stinc.male.exprcalculator.logic.ExpressionParser.isEmptySymbol;

public final class CalculationException extends RuntimeException {
  private static final long serialVersionUID = 0;

  private final int problemIdx;
  @Nullable
  private final String word;
  @Nullable
  private String expr;

  CalculationException(@Nullable final String expr) {
    this(-1, null, expr, null, null);
  }

  CalculationException(
      @Nullable final String expr,
      @Nullable final Throwable cause) {
    this(-1, null, expr, null, cause);
  }

  CalculationException(final int problemIdx, @Nullable final String expr) {
    this(problemIdx, null, expr, null, null);
  }

  CalculationException(final int problemIdx, @Nullable final String expr, @Nullable Throwable cause) {
    this(problemIdx, null, expr, null, cause);
  }

  CalculationException(final Word word) {
    this(word.getPosition(), word.getWord(), null, null, null);
  }

  CalculationException(final Word word, @Nullable final String expr) {
    this(word.getPosition(), word.getWord(), expr, null, null);
  }

  CalculationException(final Word word, @Nullable final String expr, @Nullable Throwable cause) {
    this(word.getPosition(), word.getWord(), expr, null, cause);
  }

  /**
   * @param problemIdx Negative when it is impossible to specify the exact place that caused the exception.
   */
  CalculationException(
      final int problemIdx,
      @Nullable final String word,
      @Nullable final String expr,
      @Nullable final String message,
      @Nullable final Throwable cause) {
    super((message == null && expr != null)
            ? description(problemIdx, word, expr)
            : message,
        cause);
    this.problemIdx = problemIdx;
    this.word = word;
    this.expr = expr;
  }

  final boolean isExpressionSet() {
    return expr != null;
  }

  final void setExpression(final String expr) {
    checkNotNull(expr, "The argument %s must not be null", "expr");
    if (this.expr != null) {
      throw new Error(String.format("%s has already been set", "expr"));
    }
    this.expr = expr;
  }

  public final String description() {
    if (this.expr == null) {
      throw new Error(String.format("%s has not been set", "expr"));
    }
    return description(problemIdx, word, expr);
  }

  private static final String description(final int problemIdx, @Nullable final String word, final String expr) {
    final String result;
    if (problemIdx < 0) {
      result = String.format("Can not calculate expression:%s%s", LN, expr);
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
      result = String.format("Problem with '%s' at index %s:", word == null ? expr.charAt(problemIdx) : word, problemIdx) + LN
          + expr + LN
          + spaces + '^';
    }
    return result;
  }
}