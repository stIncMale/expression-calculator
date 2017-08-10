package stinc.male.exrpcalculator;

import com.beust.jcommander.ParameterException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
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

/**
 * TODO
 * Notes:
 * - the last expression in the examples is missing the last closing bracket
 */
public final class Main {
  public static final int EXIT_STATUS_FAILURE = 1;
  public static final String LINE_SEPARATOR = "\n";
  public static final Charset charset = StandardCharsets.UTF_8;
  public static final Locale locale = Locale.ROOT;
  private static final String APPLICATION_ROOT_LOGGER_NAME = "stinc.male.exrpcalculator";
  private static final Logger logger;

  static {
    System.setProperty("line.separator", LINE_SEPARATOR);//set line separator; according to JDK this is a standard property
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
      logger.debug("Arguments={}", inputAndArgs.getArguments());
      logger.debug("Expression to calculate='{}'", inputAndArgs.getInput());
      final ExpressionCalculator calc = new ExpressionCalculator();
      System.out.println(calc.calculate(inputAndArgs.getInput())
          .toPlainString());
    } catch (final ParameterException e) {
      System.err.println(e.getLocalizedMessage());
      Args.printUsage();
      exitStatus = EXIT_STATUS_FAILURE;
    } catch (final CalculationException e) {
      System.err.println(e.getLocalizedMessage());
      Args.printUsage();
      logger.error(null, e);
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
    throw new UnsupportedOperationException("This class is not dedigned to be instantiated");
  }
}