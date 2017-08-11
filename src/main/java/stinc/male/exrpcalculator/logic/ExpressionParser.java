package stinc.male.exrpcalculator.logic;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stinc.male.exrpcalculator.logic.Word.Type;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static stinc.male.exrpcalculator.Main.LN;
import static stinc.male.exrpcalculator.logic.Word.Type.CLOSING_BRACKET;
import static stinc.male.exrpcalculator.logic.Word.Type.COMMA;
import static stinc.male.exrpcalculator.logic.Word.Type.EMPTY;
import static stinc.male.exrpcalculator.logic.Word.Type.LITERAL;
import static stinc.male.exrpcalculator.logic.Word.Type.NUMERIC;
import static stinc.male.exrpcalculator.logic.Word.Type.OPENING_BRACKET;

public final class ExpressionParser {
  private static final Logger logger = LoggerFactory.getLogger(ExpressionParser.class);

  /**
   * This can be {@linkplain Stream streamified} to reduce memory footprint.
   *
   * @throws CalculationException
   */
  static final List<Word> parse(@Nullable final String expr, final MathContext mc) throws CalculationException {//TODO return stream
    checkNotNull(mc, "The argument %s must not be null", "mc");
    logger.debug("Parsing '{}'", expr);
    final List<Word> result;
    if (StringUtils.isBlank(expr)) {
      result = Collections.emptyList();
    } else {
      result = new ArrayList<>();
      @Nullable
      WordInfo wordInfo = null;
      final BracketsValidator bracketsValidator = new BracketsValidator();
      for (int idx = 0; idx <= expr.length(); idx++) {
        final boolean endOfExpression = idx == expr.length();
        final int problemIdx = endOfExpression ? expr.length() - 1 : idx;
        try {
          final char symbol = endOfExpression
              ? ' '
              : expr.charAt(idx);
          if (idx == 0) {
            wordInfo = new WordInfo(idx, symbol, mc);
          } else {
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
                result.add(wordInfo.buildWordAndStartNew(idx, symbol, expr));
                break;
              }
              case LITERAL: {
                if (symbol == '_' || Character.isAlphabetic(symbol) || Character.isDigit(symbol)) {
                  //continue reading the current word
                } else if (isTrailerSymbol(symbol)) {
                  result.add(wordInfo.buildWordAndStartNew(idx, symbol, expr));
                } else {//invalid symbol
                  throw new CalculationException(problemIdx, expr, null, null);
                }
                break;
              }
              case NUMERIC: {
                if (symbol == '.' || Character.isDigit(symbol)) {
                  //continue reading the current word
                } else if (isTrailerSymbol(symbol)) {
                  result.add(wordInfo.buildWordAndStartNew(idx, symbol, expr));
                } else {//invalid symbol
                  throw new CalculationException(problemIdx, expr, null, null);
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
          throw new CalculationException(problemIdx, expr, null, e);
        }
      }
    }
    logger.debug("Parsing result for '{}' is{}[{}]", expr, LN,
        result.stream()
            .map(Word::toString)
            .collect(Collectors.joining(LN)));
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

  private ExpressionParser() {
    throw new UnsupportedOperationException("This class is not designed to be instantiated");
  }

  private static final class WordInfo {
    private int startIdx;
    private Word.Type type;
    private final MathContext mc;

    private WordInfo(final int startIdx, final char startingSymbol, final MathContext mc) {
      this.startIdx = startIdx;
      this.type = wordTypeFor(startingSymbol);
      this.mc = mc;
    }

    private final Word buildWordAndStartNew(final int startIdx, final char startingSymbol, final String expr) {
      final Word result = new Word(expr.substring(this.startIdx, startIdx), type, this.startIdx, mc);
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

    final void accountOpening() {
      count++;
    }

    final void accountClosing() {
      count--;
      checkState(count >= 0, "%s must not be negative", "count");
    }

    final void validate() {
      checkState(count == 0, "%s=%s must be 0", "count", count);
    }
  }
}