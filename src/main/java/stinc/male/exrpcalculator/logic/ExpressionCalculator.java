package stinc.male.exrpcalculator.logic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stinc.male.exrpcalculator.Main;
import static com.google.common.base.Preconditions.checkNotNull;
import static stinc.male.exrpcalculator.logic.Word.LogicalType.OPERAND;
import static stinc.male.exrpcalculator.logic.Word.LogicalType.OPERAND_VAR;
import static stinc.male.exrpcalculator.logic.Word.LogicalType.OPERATOR_LET;

public final class ExpressionCalculator {
  private static final Logger logger = LoggerFactory.getLogger(ExpressionCalculator.class);

  private final MathContext mc;
  private final Deque<Word> stack;
  private final LetOperatorStack letOperatorStack;
  private final LinkedHashMap<String, BigDecimal> context;
  final List<Word> reversedOperands;

  public ExpressionCalculator(final MathContext mc) {
    checkNotNull(mc, "The argument %s must not be null", "mc");
    this.mc = mc;
    stack = new ArrayDeque<>();
    letOperatorStack = new LetOperatorStack();
    context = new LinkedHashMap<>();
    reversedOperands = new ArrayList<>();
  }

  public final BigDecimal calculate(final String expr) throws CalculationException {//TODO test multiple calls
    checkNotNull(expr, "The argument %s must not be null", "expr");
    logger.debug("Calculating '{}'", expr);
    final BigDecimal result;
    try {
      result = calculate(new ParsedExpression(expr, mc));
    } catch (final CalculationException e) {
      if (!e.isExpressionSet()) {
        e.setExpression(expr);
      }
      throw e;
    } catch (final RuntimeException e) {
      throw new CalculationException(expr, null, e);
    } finally {
      stack.clear();
      letOperatorStack.clear();
      context.clear();
      reversedOperands.clear();
    }
    logger.debug("Calculation result for '{}' is {}", expr, result);
    return result;
  }

  private final BigDecimal calculate(final ParsedExpression parsedExpr) throws CalculationException {
    final List<Word> words = parsedExpr.getWords();
    for (int wordIdx = 0; wordIdx < words.size(); wordIdx++) {
      final Word word = words.get(wordIdx);
      if (wordIdx == 0 && !word.getLogicalType().isOperator()) {
        throw new CalculationException(word.getPosition(), parsedExpr.getExpression(), null, null);
      }
      try {
        switch (word.getLogicalType()) {//TODO put all cases as one?
          case OPERATOR_LET:
          case OPERATOR_ADD:
          case OPERATOR_SUB:
          case OPERATOR_MULT:
          case OPERATOR_DIV:{
            stack.push(word);
            letOperatorStack.pushToStackOrPutToContext(word, context);
            break;
          }
          case OPERAND: {
            stack.push(word);
            letOperatorStack.pushToStackOrPutToContext(word, context);
            break;
          }
          case OPERAND_VAR: {
            stack.push(word);
            letOperatorStack.pushToStackOrPutToContext(word, context);
            break;
          }
          case CALCULATION: {
            final Word intermediateResult = calculate(word, stack, context, reversedOperands, mc);
            stack.push(intermediateResult);
            letOperatorStack.pushToStackOrPutToContext(intermediateResult, context);
            break;
          }
          default: {
            throw new Error(String.format("%s is not accounted", word.getLogicalType()));
          }
        }
      } catch (final CalculationException e) {
        throw e;
      } catch (final RuntimeException e) {
        throw new CalculationException(word.getPosition(), parsedExpr.getExpression(), null, e);
      }
    }
    final BigDecimal result;
    if (stack.isEmpty()) {
      throw new CalculationException(parsedExpr.getExpression(), null, null);
    } else if (stack.size() > 1) {
      throw new CalculationException(stack.pop().getPosition(), parsedExpr.getExpression(), null, null);
    } else {//exactly one element in the stack
      final Word lastWord = stack.pop();
      if (lastWord.getLogicalType() == OPERAND) {
        result = lastWord.getValue();
      } else {
        throw new CalculationException(lastWord.getPosition(), parsedExpr.getExpression(), null, null);
      }
    }
    return result;
  }

  @Nullable
  private final Word calculate(
      final Word word,
      final Deque<Word> stack,
      final LinkedHashMap<String, BigDecimal> context,
      final List<Word> reversedOperands,
      final MathContext mc) {
    checkNotNull(word, "The argument %s must not be null", "word");
    checkNotNull(stack, "The argument %s must not be null", "stack");
    checkNotNull(context, "The argument %s must not be null", "context");
    checkNotNull(reversedOperands, "The argument %s must not be null", "reversedOperands");
    checkNotNull(mc, "The argument %s must not be null", "mc");
    if (logger.isDebugEnabled()) {
      logger.debug("Calculating intermediate result{}word={},{}stack={},{}context={}", Main.LINE_SEPARATOR,
          word, Main.LINE_SEPARATOR,
          stack.stream()
              .map(Word::toString)
              .collect(Collectors.joining(Main.LINE_SEPARATOR)), Main.LINE_SEPARATOR,
          context);
    }
    reversedOperands.clear();
    final Word result;
    @Nullable
    Word wordFromStack;
    for (wordFromStack = stack.peek();
        wordFromStack != null && !wordFromStack.getLogicalType().isOperator();) {
      if (wordFromStack.getLogicalType() == OPERAND
          || wordFromStack.getLogicalType() == OPERAND_VAR) {
        reversedOperands.add(wordFromStack);
        stack.pop();//remove wordFromStack from stack
        wordFromStack = stack.peek();
      } else {
        throw new CalculationException(wordFromStack.getPosition(), null, null, null);
      }
    }
    if (wordFromStack == null) {
      throw new CalculationException(word.getPosition(), null, null, null);
    } else {
      final Word operator = wordFromStack;
      stack.pop();//remove operator from stack
      logger.debug("operator={}, reversedOperands={}", operator, reversedOperands);
      if (operator.getLogicalType().isCalculationSupported()) {//OPERATOR_ADD, SUB, MULT, DIV
        final BigDecimal intermediateResult;
        if (reversedOperands.size() < 2) {
          throw new CalculationException(word.getPosition(), null, null, null);
        } else if (reversedOperands.size() > 2) {
          throw new CalculationException(reversedOperands.get(reversedOperands.size() - 3).getPosition(), null, null, null);
        } else {//exactly 2 operands
          final Word operand1 = reversedOperands.get(reversedOperands.size() - 1);
          final Word operand2 = reversedOperands.get(reversedOperands.size() - 2);
          intermediateResult = operator.getLogicalType().calculate(
              operandValue(operand1, context),
              operandValue(operand2, context),
              mc);
        }
        result = new Word(operator, intermediateResult);
      } else {//OPERATOR_LET
        final BigDecimal intermediateResult;
        if (reversedOperands.size() < 3) {
          throw new CalculationException(word.getPosition(), null, null, null);
        } else if (reversedOperands.size() > 3) {
          throw new CalculationException(reversedOperands.get(reversedOperands.size() - 4).getPosition(), null, null, null);
        } else {//exactly 3 operands
          final Word operand1 = reversedOperands.get(reversedOperands.size() - 1);
          if (operand1.getLogicalType() != OPERAND_VAR) {
            throw new CalculationException(operand1.getPosition(), null, null, null);
          }
          final Word operand2 = reversedOperands.get(reversedOperands.size() - 2);
          if (operand2.getLogicalType() != OPERAND) {
            throw new CalculationException(operand2.getPosition(), null, null, null);
          }
          final Word operand3 = reversedOperands.get(reversedOperands.size() - 3);
          if (operand3.getLogicalType() != OPERAND) {
            throw new CalculationException(operand3.getPosition(), null, null, null);
          }
          if (context.remove(operand1.getWord()) == null) {
            throw new Error(String.format("context=%s does not contain variable %s", context, operand1.getWord()));
          }
          intermediateResult = operand3.getValue();
        }
        result = new Word(operator, intermediateResult);
      }
    }
    logger.debug("intermediate result={}", result);
    return result;
  }

  private static final BigDecimal operandValue(final Word operand, final Map<String, BigDecimal> context) throws CalculationException {
    @Nullable
    final BigDecimal result;
    switch (operand.getLogicalType()) {
      case OPERAND: {
        result = operand.getValue();
        break;
      }
      case OPERAND_VAR: {
        result = context.get(operand.getWord());
        if (result == null) {
          throw new CalculationException(operand.getPosition(), null, null, null);
        }
        break;
      }
      default: {
        throw new Error(String.format("%s is not an operand", operand));
      }
    }
    return result;
  }

  private final class LetOperatorStack {
    private final Deque<Word> stack;

    private LetOperatorStack() {
      stack = new ArrayDeque<>();
    }

    private boolean pushToStackOrPutToContext(final Word word, final Map<String, BigDecimal> context) throws CalculationException {
      final boolean result;
      @Nullable
      final Word lastWord = stack.peek();
      if (lastWord == null) {
        if (word.getLogicalType() == OPERATOR_LET) {
          stack.push(word);
          result = false;
        } else {
          stack.clear();
          result = false;
        }
      } else if (lastWord.getLogicalType() == OPERATOR_LET && word.getLogicalType() == OPERAND_VAR) {
        stack.push(word);
        result = false;
      } else if (lastWord.getLogicalType() == OPERAND_VAR) {
        if (word.getLogicalType() == OPERAND) {//put in context
          final String varName = lastWord.getWord();
          if (context.containsKey(varName)) {//variable with the ame name has already been defined
            throw new CalculationException(lastWord.getPosition(), null, null, null);
          } else {
            final BigDecimal varValue = word.getValue();
            context.put(varName, varValue);
            stack.clear();
            result = true;
          }
        } else if (word.getLogicalType().isCalculationSupported()) {//OPERATOR_ADD, SUB, MULT, DIV
          result = false;
        } else {
          result = false;
        }
      } else {
        stack.clear();
        result = false;
      }
      if (logger.isDebugEnabled()) {
        logger.debug("letOperatorStack{}word={},{}stack={},{}context={}", Main.LINE_SEPARATOR,
            word, Main.LINE_SEPARATOR,
            stack.stream()
                .map(Word::toString)
                .collect(Collectors.joining(Main.LINE_SEPARATOR)), Main.LINE_SEPARATOR,
            context);
      }
      return result;
    }

    private void clear() {
      stack.clear();
    }
  }
}