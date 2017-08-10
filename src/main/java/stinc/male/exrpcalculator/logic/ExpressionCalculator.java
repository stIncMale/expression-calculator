package stinc.male.exrpcalculator.logic;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.google.common.base.Preconditions.checkNotNull;
import static stinc.male.exrpcalculator.logic.ExpressionParser.parse;

public final class ExpressionCalculator {
  private static final Logger logger = LoggerFactory.getLogger(ExpressionCalculator.class);

  public ExpressionCalculator() {
  }

  public final BigDecimal calculate(final String expr) throws CalculationException {
    checkNotNull(expr, "The argument %s must not be null", "expr");
    logger.debug("Calculating '{}'", expr);
    final BigDecimal result;
    try {
      parse(expr);
      result = BigDecimal.ZERO;
    } catch (final CalculationException e) {
      if (!e.isExpressionSet()) {
        e.setExpression(expr);
      }
      throw e;
    } catch (final RuntimeException e) {
      throw new CalculationException(expr, null, e);
    }
    logger.debug("Calculation result for '{}' is {}", expr, result);
    return result;
  }
}