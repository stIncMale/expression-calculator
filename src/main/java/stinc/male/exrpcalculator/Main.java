package stinc.male.exrpcalculator;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {
  private static final String APPLICATION_ROOT_LOGGER_NAME = "stinc.male.exrpcalculator";
  private static final Logger logger;

  static {
    System.setProperty("line.separator", "\n");//set line separator; according to JDK this is a standard property
    try {//set charset for out and err streams
      System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
      System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8.name()));
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    logger = LoggerFactory.getLogger(Main.class);
  }

  public static final void main(final String[] args) {
    logger.debug("Application start");
    try {
      Configurator.setAllLevels(APPLICATION_ROOT_LOGGER_NAME, Level.INFO);
    } finally {
      logger.debug("Application stop");
    }
  }

  private Main() {
    throw new UnsupportedOperationException("This class is not dedigned to be instantiated");
  }
}