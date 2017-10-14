package stinc.male.exprcalculator.arg;

import com.beust.jcommander.ParameterException;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import stinc.male.exprcalculator.Main;

/**
 * Represents both input to the application and its arguments.
 */
public final class InputAndArgs {
  private static final String MESSAGE_NO_INPUT = "Input has not been provided";
  private final String input;
  private final Args arguments;

  /**
   * @param in {@link InputAndArgs} tries to read the {@linkplain #getInput() input} from {@code in} if {@code in} is provided.
   */
  public InputAndArgs(@Nullable final String[] args, @Nullable InputStream in) throws ParameterException {
    try {
      if (in != null && in.available() > 0) {//check if input is provided via std_in
        input = validateInput(IOUtils.toString(in, Main.charset));
        arguments = new Args(args);
      } else if (args == null || args.length == 0) {
        throw new ParameterException(MESSAGE_NO_INPUT);
      } else {//input is provided via command line arguments
        input = validateInput(args[args.length - 1]);
        arguments = args.length > 1 ? new Args(args, 0, args.length - 2) : new Args();//use default arguments
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public final String getInput() {
    return input;
  }

  public final Args getArguments() {
    return arguments;
  }

  @Override
  public final String toString() {
    return "{input=" + input + ", arguments=" + arguments + '}';
  }

  private static final String validateInput(final String input) throws ParameterException {
    if (StringUtils.isBlank(input)) {
      throw new ParameterException(MESSAGE_NO_INPUT);
    }
    return input;
  }
}