package stinc.male.exrpcalculator;

import com.beust.jcommander.ParameterException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.annotation.Nullable;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stinc.male.exrpcalculator.arg.Args;
import stinc.male.exrpcalculator.arg.InputAndArgs;
import stinc.male.exrpcalculator.logic.CalculationException;
import stinc.male.exrpcalculator.logic.ExpressionCalculator;

public final class Main {
  public static final int EXIT_STATUS_FAILURE = 1;
  /**
   * Line separator.
   */
  public static final String LN = "\n";
  public static final Charset charset = StandardCharsets.UTF_8;
  public static final Locale locale = Locale.ROOT;
  private static final String APPLICATION_ROOT_LOGGER_NAME = "stinc.male.exrpcalculator";
  private static final Logger logger;

  static {
    System.setProperty("line.separator", LN);//set line separator; according to JDK this is a standard property
    try {//set charset for std streams
      System.setOut(new PrintStream(System.out, true, charset.name()));
      System.setErr(new PrintStream(System.err, true, charset.name()));
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    logger = LoggerFactory.getLogger(Main.class);
  }

  public static final void main(@Nullable final String[] args) {
    @Nullable
    Integer exitStatus = null;
    try {
      final InputAndArgs inputAndArgs = new InputAndArgs(args, System.in);
      Configurator.setAllLevels(APPLICATION_ROOT_LOGGER_NAME, inputAndArgs.getArguments().getLoggingLevel());
      logger.debug("Application start");
      logger.debug("Arguments {}", inputAndArgs.getArguments());
      logger.info("Expression to calculate '{}'", inputAndArgs.getInput());
      final MathContext mc = inputAndArgs.getArguments().getMathContext();
      final ExpressionCalculator calculator = new ExpressionCalculator(new MathContext(
          mc.getPrecision() == 0 ? 0 : inputAndArgs.getArguments().getMathContext().getPrecision() + 1,//by using increased precision and then rounding we can achieve mult(div(1, 3), 3) == 1 instead of 0.999...
          inputAndArgs.getArguments().getMathContext().getRoundingMode()));
      final BigDecimal result = calculator.calculate(inputAndArgs.getInput())
          .round(inputAndArgs.getArguments().getMathContext());
      final String strResult = result.toPlainString();
      logger.info("Calculation result {}", strResult);
      System.out.println(strResult);
    } catch (final ParameterException e) {
      System.err.println(e.getLocalizedMessage());
      Args.printUsage();
      exitStatus = EXIT_STATUS_FAILURE;
    } catch (final CalculationException e) {
      System.err.println(e.description());
      Args.printUsage();
      logger.warn(e.description());
      logger.warn(null, e);
      exitStatus = EXIT_STATUS_FAILURE;
    } catch (final RuntimeException e) {
      logger.error(null, e);
      exitStatus = EXIT_STATUS_FAILURE;
    } finally {
      logger.debug("Application stop");
      if (exitStatus == null) {
        //exit normally
      } else {
        System.exit(exitStatus);
      }
    }
  }

  private Main() {
    throw new UnsupportedOperationException("This class is not designed to be instantiated");
  }
}