package stinc.male.exprcalculator.performance;

import java.io.IOException;
import java.math.MathContext;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import stinc.male.exprcalculator.logic.ExpressionCalculator;
import static java.lang.Boolean.parseBoolean;
import static org.openjdk.jmh.runner.options.TimeValue.milliseconds;

@Tag("performance")
@TestInstance(Lifecycle.PER_CLASS)
public class TestPerformance {
  private static final boolean DRY_RUN = parseBoolean(System.getProperty("stinc.male.exprcalculator.dryRun", "false"));
  private static final boolean JAVA_SERVER = true;
  private static final boolean JAVA_ASSERTIONS = DRY_RUN;

  static {
    Configurator.setAllLevels("", org.apache.logging.log4j.Level.OFF);//disable logging
  }

  public TestPerformance() {
  }

  @Test
  public void run() throws RunnerException {
    final ChainedOptionsBuilder jmhOptions = new OptionsBuilder()
        .jvmArgs("-Xms1536m", "-Xmx1536m")
        .jvmArgsAppend(
            JAVA_SERVER ? "-server" : "-client",
            JAVA_ASSERTIONS ? "-enableassertions" : "-disableassertions")
        .shouldDoGC(true)
        .syncIterations(true)
        .shouldFailOnError(true)
        .threads(1)
        .timeout(milliseconds(5_000));
    if (DRY_RUN) {
      jmhOptions.forks(1)
          .warmupTime(milliseconds(50))
          .warmupIterations(1)
          .measurementTime(milliseconds(50))
          .measurementIterations(1);
    } else {
      jmhOptions.forks(3)
          .warmupTime(milliseconds(200))
          .warmupIterations(10)
          .measurementTime(milliseconds(200))
          .measurementIterations(5);
    }
    new Runner(jmhOptions.include(getClass().getName() + ".*")
        .mode(Mode.AverageTime)
        .timeUnit(TimeUnit.MICROSECONDS)
        .build())
        .run();
  }

  @Benchmark
  public void calculate(final BenchmarkState state, final Blackhole bh) {
    final List<String> expressions = state.expressions;
    final int idx = ThreadLocalRandom.current().nextInt(0, state.expressions.size() - 1);
    bh.consume(state.calculator.calculate(expressions.get(idx)));
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private List<String> expressions;
    private ExpressionCalculator calculator;

    public BenchmarkState() {
    }

    @Setup(Level.Trial)
    public final void setupTrial() {
      @Nullable
      final URL expressionsUrl = TestPerformance.class.getClassLoader()
          .getResource("expressions");
      if (expressionsUrl == null) {
        throw new RuntimeException("Can't locate expressions");
      }
      try (final Stream<String> lines = Files.lines(Paths.get(expressionsUrl.toURI()))) {
        expressions = Collections.unmodifiableList(lines.filter(line -> !(line.isEmpty() || line.startsWith("//")))
            .collect(Collectors.toList()));
      } catch (final URISyntaxException | IOException e) {
        throw new RuntimeException(e);
      }
      calculator = new ExpressionCalculator(MathContext.DECIMAL32);
    }
  }
}