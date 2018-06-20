package stinc.male.exprcalculator.logic;

import java.math.MathContext;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import stinc.male.exprcalculator.logic.Word.Type;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static stinc.male.exprcalculator.logic.Word.Type.CLOSING_BRACKET;
import static stinc.male.exprcalculator.logic.Word.Type.COMMA;
import static stinc.male.exprcalculator.logic.Word.Type.EMPTY;
import static stinc.male.exprcalculator.logic.Word.Type.LITERAL;
import static stinc.male.exprcalculator.logic.Word.Type.NUMERIC;
import static stinc.male.exprcalculator.logic.Word.Type.OPENING_BRACKET;

public final class ExpressionSpliterator extends AbstractSpliterator<Word> {
  private final String expr;
  private final MathContext mc;
  private int idx;
  private final WordInfo wordInfo;
  private final BracketsValidator bracketsValidator;
  private final Word.LogicalTypeValuePair ltypeAndValueHolder;

  ExpressionSpliterator(final String expr, final MathContext mc) {
    super(Long.MAX_VALUE, DISTINCT | IMMUTABLE | NONNULL | ORDERED);
    checkNotNull(expr, "The argument %s must not be null", "expr");
    checkNotNull(mc, "The argument %s must not be null", "mc");
    this.expr = expr;
    this.mc = mc;
    idx = 0;
    wordInfo = new WordInfo();
    bracketsValidator = new BracketsValidator();
    ltypeAndValueHolder = new Word.LogicalTypeValuePair();
  }

  @Override
  public final boolean tryAdvance(final Consumer<? super Word> action) throws CalculationException {
    checkNotNull(action, "The argument %s must not be null", "action");
    final boolean result;
    @Nullable Word word = null;
    for (; word == null && idx <= expr.length(); idx++) {
      final boolean endOfExpression = idx == expr.length();
      final int problemIdx = endOfExpression ? expr.length() - 1 : idx;
      try {
        final char symbol = endOfExpression ? ' ' : expr.charAt(idx);
        if (idx == 0) {
          wordInfo.reset(idx, symbol);
          bracketsValidator.reset();
        } else {
          assert bracketsValidator != null;
          assert wordInfo != null;
          switch (wordInfo.type) {
            case EMPTY:
            case COMMA: {
              wordInfo.startNew(idx, symbol);//ignore the word
              break;
            }
            case OPENING_BRACKET: {
              bracketsValidator.accountOpening();
              wordInfo.startNew(idx, symbol);//ignore the word
              break;
            }
            case CLOSING_BRACKET: {
              bracketsValidator.accountClosing();
              word = wordInfo.buildWordAndStartNew(idx, symbol, expr, mc, ltypeAndValueHolder);
              break;
            }
            case LITERAL: {
              if (symbol == '_' || Character.isAlphabetic(symbol) || Character.isDigit(symbol)) {
                //continue reading the current word
              } else if (isTrailerSymbol(symbol)) {
                word = wordInfo.buildWordAndStartNew(idx, symbol, expr, mc, ltypeAndValueHolder);
              } else {//invalid symbol
                throw new CalculationException(problemIdx, expr);
              }
              break;
            }
            case NUMERIC: {
              if (symbol == '.' || Character.isDigit(symbol)) {
                //continue reading the current word
              } else if (isTrailerSymbol(symbol)) {
                word = wordInfo.buildWordAndStartNew(idx, symbol, expr, mc, ltypeAndValueHolder);
              } else {//invalid symbol
                throw new CalculationException(problemIdx, expr);
              }
              break;
            }
            default: {
              throw new Error(String.format("%s is not considered", wordInfo.type));
            }
          }
          if (endOfExpression) {
            bracketsValidator.validate();
          }
        }
      } catch (final CalculationException e) {
        throw e;
      } catch (final RuntimeException e) {
        throw new CalculationException(problemIdx, expr, e);
      }
    }
    if (word == null) {
      result = false;
    } else {
      action.accept(word);
      result = true;
    }
    return result;
  }

  private static final Type wordTypeFor(final char startingSymbol) throws IllegalArgumentException {
    final Type result;
    if (startingSymbol == '(') {
      result = OPENING_BRACKET;
    } else if (startingSymbol == ')') {
      result = CLOSING_BRACKET;
    } else if (startingSymbol == ',') {
      result = COMMA;
    } else if (startingSymbol == '-' || Character.isDigit(startingSymbol)) {
      result = NUMERIC;
    } else if (startingSymbol == '_' || Character.isAlphabetic(startingSymbol)) {
      result = LITERAL;
    } else if (isEmptySymbol(startingSymbol)) {
      return EMPTY;
    } else {
      throw new IllegalArgumentException(String.format("Unknown symbol %s", startingSymbol));
    }
    return result;
  }

  static final boolean isEmptySymbol(final char symbol) {
    return Character.isWhitespace(symbol) || Character.isSpaceChar(symbol) || Character.isISOControl(symbol);
  }

  private static final boolean isTrailerSymbol(final char symbol) {
    return symbol == '(' || symbol == ')' || symbol == ',' || isEmptySymbol(symbol);
  }

  private static final class WordInfo {
    private int startIdx;
    private Word.Type type;

    private WordInfo() {
      this.startIdx = 0;
      this.type = Type.EMPTY;
    }

    private final void reset(final int startIdx, final char startingSymbol) {
      this.startIdx = startIdx;
      this.type = wordTypeFor(startingSymbol);
    }

    private final Word buildWordAndStartNew(final int startIdx, final char startingSymbol, final String expr, final MathContext mc,
        final Word.LogicalTypeValuePair ltypeAndValueHolder) {
      final Word result = new Word(expr.substring(this.startIdx, startIdx), type, this.startIdx, mc, ltypeAndValueHolder);
      startNew(startIdx, startingSymbol);
      return result;
    }

    private final void startNew(final int startIdx, final char startingSymbol) {
      this.startIdx = startIdx;
      type = wordTypeFor(startingSymbol);
    }
  }

  private static final class BracketsValidator {
    private int count;

    private BracketsValidator() {
      count = 0;
    }

    private final void reset() {
      count = 0;
    }

    private final void accountOpening() {
      count++;
    }

    private final void accountClosing() {
      count--;
      checkState(count >= 0, "%s must not be negative", "count");
    }

    private final void validate() {
      checkState(count == 0, "%s=%s must be 0", "count", count);
    }
  }
}