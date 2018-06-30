package stinc.male.exprcalculator.logic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.google.common.base.Preconditions.checkNotNull;
import static stinc.male.exprcalculator.Main.LN;
import static stinc.male.exprcalculator.logic.Word.LogicalType.OPERAND;
import static stinc.male.exprcalculator.logic.Word.LogicalType.OPERAND_VAR;
import static stinc.male.exprcalculator.logic.Word.LogicalType.OPERATOR_LET;

public final class ExpressionCalculator {
  private static final Logger logger = LoggerFactory.getLogger(ExpressionCalculator.class);

  private final MathContext mc;
  private final Deque<Word> stack;
  private final Deque<LetOperatorScope> letOperatorScopesStack;
  private final Map<String, BigDecimal> context;
  private final List<Word> reversedOperands;
  private final Mutable<Word> lastSeenOperator;

  public ExpressionCalculator(final MathContext mc) {
    checkNotNull(mc, "The argument %s must not be null", "mc");
    this.mc = mc;
    stack = new ArrayDeque<>();
    letOperatorScopesStack = new ArrayDeque<>();
    context = new HashMap<>();
    reversedOperands = new ArrayList<>();
    lastSeenOperator = new MutableObject<>(null);
  }

  public final BigDecimal calculate(final String expr) throws CalculationException {
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
      throw new CalculationException(expr, e);
    } finally {
      stack.clear();
      letOperatorScopesStack.clear();
      context.clear();
      reversedOperands.clear();
      lastSeenOperator.setValue(null);
    }
    logger.debug("Calculation result for '{}' is {}", expr, result);
    return result;
  }

  private final BigDecimal calculate(final ParsedExpression parsedExpr) throws CalculationException {
    parsedExpr.stream()
        .forEach(word -> {
          try {
            switch (word.getLogicalType()) {
              case OPERATOR_LET: {
                lastSeenOperator.setValue(word);
                stack.push(word);
                letOperatorScopesStack.push(new LetOperatorScope(word));
                break;
              }
              case OPERATOR_ADD:
              case OPERATOR_SUB:
              case OPERATOR_MULT:
              case OPERATOR_DIV: {
                lastSeenOperator.setValue(word);
                stack.push(word);
                break;
              }
              case OPERAND:
              case OPERAND_VAR: {
                stack.push(word);
                if (lastSeenOperator.getValue() != null && lastSeenOperator.getValue()
                    .getLogicalType() == OPERATOR_LET && !letOperatorScopesStack.isEmpty()) {
                  letOperatorScopesStack.peek()
                      .register(word, context);
                }
                break;
              }
              case CALCULATION: {
                final Word intermediateResult = calculateIntermediateResult(word, stack, letOperatorScopesStack, context, reversedOperands, mc);
                stack.push(intermediateResult);
                if (!letOperatorScopesStack.isEmpty()) {
                  letOperatorScopesStack.peek()
                      .register(intermediateResult, context);
                }
                break;
              }
              default: {
                throw new Error(String.format("%s is not accounted", word.getLogicalType()));
              }
            }
          } catch (final CalculationException e) {
            throw e;
          } catch (final RuntimeException e) {
            throw new CalculationException(word, parsedExpr.getExpression(), e);
          }
        });
    final BigDecimal result;
    if (stack.isEmpty()) {
      throw new CalculationException(parsedExpr.getExpression());
    } else if (stack.size() > 1) {
      throw new CalculationException(stack.peek(), parsedExpr.getExpression());
    } else {//exactly one element in the stack
      final Word lastWord = stack.peek();
      if (lastWord.getLogicalType() == OPERAND) {
        result = lastWord.getValue();
      } else {
        throw new CalculationException(lastWord, parsedExpr.getExpression());
      }
    }
    return result;
  }

  private static final Word calculateIntermediateResult(
      final Word calculationWord,
      final Deque<Word> stack,
      final Deque<LetOperatorScope> letOperatorScopesStack,
      final Map<String, BigDecimal> context,
      final List<Word> reversedOperands,
      final MathContext mc) throws CalculationException {
    if (logger.isDebugEnabled()) {
      logger.debug(
          "Calculating intermediate result for {}, stack{}[{}],{}letOperatorScopesStack{}[{}]{}context {}",
          calculationWord,
          LN,
          stack.stream()
              .map(Word::toString)
              .collect(Collectors.joining(LN)),
          LN,
          LN,
          letOperatorScopesStack.stream()
              .map(scope -> scope.operator.toString())
              .collect(Collectors.joining(LN)),
          LN,
          context);
    }
    reversedOperands.clear();
    final Word result;
    @Nullable Word wordFromStack;
    for (wordFromStack = stack.peek();
        wordFromStack != null && !wordFromStack.getLogicalType()
            .isOperator();
        wordFromStack = stack.peek()) {//read top operands and operator
      if (wordFromStack.getLogicalType() == OPERAND || wordFromStack.getLogicalType() == OPERAND_VAR) {
        reversedOperands.add(wordFromStack);
        stack.pop();//remove wordFromStack from stack
      } else {
        throw new CalculationException(wordFromStack);
      }
    }
    if (wordFromStack == null) {
      throw new CalculationException(calculationWord);
    } else {
      final Word operator = wordFromStack;
      stack.pop();//remove operator from stack
      logger.debug("Calculating intermediate result for operator {}, reversed operands {}", operator, reversedOperands);
      if (operator.getLogicalType()
          .isCalculationSupported()) {//OPERATOR_ADD, SUB, MULT, DIV
        result = calculateIntermediateResultForCalculableOperator(calculationWord, operator, reversedOperands, context, mc);
      } else {//OPERATOR_LET
        result = calculateIntermediateResultForLetOperator(calculationWord, operator, reversedOperands, context, letOperatorScopesStack);
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug(
          "Intermediate result {}, stack{}[{}],{}letOperatorScopesStack{}[{}]{}context {}",
          result,
          LN,
          stack.stream()
              .map(Word::toString)
              .collect(Collectors.joining(LN)),
          LN,
          LN,
          letOperatorScopesStack.stream()
              .map(scope -> scope.operator.toString())
              .collect(Collectors.joining(LN)),
          LN,
          context);
    }
    return result;
  }

  private static final Word calculateIntermediateResultForCalculableOperator(
      final Word calculationWord,
      final Word operator,
      final List<Word> reversedOperands,
      final Map<String, BigDecimal> context,
      final MathContext mc) throws CalculationException {
    final BigDecimal intermediateResult;
    if (reversedOperands.size() < 2) {
      throw new CalculationException(calculationWord);
    } else if (reversedOperands.size() > 2) {
      throw new CalculationException(reversedOperands.get(reversedOperands.size() - 3));
    } else {//exactly 2 operands
      final Word operand1 = reversedOperands.get(reversedOperands.size() - 1);
      final Word operand2 = reversedOperands.get(reversedOperands.size() - 2);
      try {
        intermediateResult = operator.getLogicalType()
            .calculate(operandValue(operand1, context), operandValue(operand2, context), mc);
      } catch (final RuntimeException e) {
        throw new CalculationException(operator);
      }
    }
    return new Word(operator, intermediateResult);
  }

  private static final Word calculateIntermediateResultForLetOperator(
      final Word calculationWord,
      final Word operator,
      final List<Word> reversedOperands,
      final Map<String, BigDecimal> context,
      final Deque<LetOperatorScope> letOperatorScopesStack) throws CalculationException {
    final BigDecimal intermediateResult;
    if (reversedOperands.size() < 3) {
      throw new CalculationException(calculationWord);
    } else if (reversedOperands.size() > 3) {
      throw new CalculationException(reversedOperands.get(reversedOperands.size() - 4));
    } else {//exactly 3 operands
      final Word operand1 = reversedOperands.get(reversedOperands.size() - 1);
      if (operand1.getLogicalType() != OPERAND_VAR) {
        throw new CalculationException(operand1);
      }
      final Word operand2 = reversedOperands.get(reversedOperands.size() - 2);
      if (operand2.getLogicalType() != OPERAND) {
        throw new CalculationException(operand2);
      }
      final Word operand3 = reversedOperands.get(reversedOperands.size() - 3);
      intermediateResult = operandValue(operand3, context);
      if (context.remove(operand1.getWord()) == null) {
        throw new Error(String.format("context=%s does not contain variable %s", context, operand1.getWord()));
      } else {
        logger.debug("New context ({} was removed) {}", operand1.getWord(), context);
      }
      letOperatorScopesStack.pop();
    }
    return new Word(operator, intermediateResult);
  }

  private static final BigDecimal operandValue(final Word operand, final Map<String, BigDecimal> context) throws CalculationException {
    @Nullable final BigDecimal result;
    switch (operand.getLogicalType()) {
      case OPERAND: {
        result = operand.getValue();
        break;
      }
      case OPERAND_VAR: {
        result = context.get(operand.getWord());
        if (result == null) {
          throw new CalculationException(operand);
        }
        break;
      }
      default: {
        throw new Error(String.format("%s is not an operand", operand));
      }
    }
    return result;
  }

  private static final class LetOperatorScope {
    private final Word operator;
    @Nullable private Word operandVar;
    @Nullable private Word operand;

    private LetOperatorScope(final Word operator) {
      this.operator = operator;
      operandVar = null;
    }

    private void register(final Word word, final Map<String, BigDecimal> context) throws CalculationException {
      if (operandVar == null) {//expecting OPERAND_VAR
        if (word.getLogicalType() == OPERAND_VAR) {
          operandVar = word;
        } else {
          throw new CalculationException(word);
        }
      } else if (operand == null) {//expecting OPERAND
        if (word.getLogicalType() == OPERAND) {//expecting OPERAND and getting OPERAND
          final String varName = operandVar.getWord();
          if (context.containsKey(varName)) {//variable with the same name has already been defined
            throw new CalculationException(operandVar);
          } else {
            operand = word;
            final BigDecimal varValue = word.getValue();
            context.put(varName, varValue);
            logger.debug("New context ({} was added) {}", varName, context);
          }
        } else {//expecting OPERAND but getting something invalid
          throw new CalculationException(word);
        }
      } else {//expecting nothing
      }
    }
  }
}