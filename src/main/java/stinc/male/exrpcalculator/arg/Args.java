package stinc.male.exrpcalculator.arg;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.logging.log4j.Level;
import stinc.male.exrpcalculator.Main;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents application arguments.
 */
public final class Args {
  private static final String NAME_OF_EXECUTABLE = "java -jar exrpcalculator.jar";

  @Parameter(names = {"-v", "-log"}, description = "Logging level. Possible values: OFF, ERROR, WARN, DEBUG", converter = LoggingLevelConverter.class)
  private Level loggingLevel = Level.ERROR;

  public Args(@Nullable final String[] args) throws ParameterException {
    this(args, 0, args.length - 1);
  }

  public Args(@Nullable final String[] args, int fromIdxInclusive, int toIdxInclusive) throws ParameterException {
    checkArgument(fromIdxInclusive >= 0, "The argument %s=%s must not be negative",
        "fromIdxInclusive", fromIdxInclusive);
    checkArgument(toIdxInclusive >= fromIdxInclusive, "The argument %s=%s must not be less than %s=%s",
        "toIdxInclusive", toIdxInclusive, "fromIdxInclusive", fromIdxInclusive);
    @Nullable final String[] actualArgs;
    if (args == null || args.length == 0) {
      actualArgs = null;
    } else if (fromIdxInclusive == 0 && toIdxInclusive == args.length - 1) {
      actualArgs = args;
    } else {
      actualArgs = new String[toIdxInclusive - fromIdxInclusive + 1];
      System.arraycopy(
          args, fromIdxInclusive,
          actualArgs, 0,
          actualArgs.length);
    }
    if (actualArgs == null) {
      //use the default values
    } else {
      newJCommanderBuilder().addObject(this)
          .build()
          .parse(actualArgs);
    }
  }

  public Args() {
  }

  public final Level getLoggingLevel() {
    return loggingLevel;
  }

  @Override
  public final String toString() {
    return "{loggingLevel=" + loggingLevel + '}';
  }

  /**
   * Prints usage to {@link System#out}.
   */
  public static final RuntimeException printUsage() {
    newJCommanderBuilder().addObject(new Args())
        .build()
        .usage();
    return new RuntimeException();
  }

  private static final JCommander.Builder newJCommanderBuilder() {
    return JCommander.newBuilder()
        .atFileCharset(Main.charset)
        .programName(NAME_OF_EXECUTABLE);
  }

  @ThreadSafe
  private static final class LoggingLevelConverter implements IStringConverter<Level> {
    private static final Set<Level> validValues = ImmutableSet.of(Level.OFF, Level.ERROR, Level.WARN, Level.DEBUG);

    public LoggingLevelConverter() {
    }

    @Override
    public final Level convert(final String v) throws ParameterException {
      final Level result;
      try {
        result = Level.valueOf(v.toUpperCase(Main.locale));
        if (!validValues.contains(result)) {
          throw new RuntimeException();
        }
      } catch (final RuntimeException e) {
        throw new ParameterException(String.format("Invalid value %s", v), e);
      }
      return result;
    }
  }
}