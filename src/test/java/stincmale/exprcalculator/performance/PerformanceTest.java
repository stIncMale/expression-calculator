package stincmale.exprcalculator.performance;

import java.io.IOException;
import java.math.MathContext;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
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
import stincmale.exprcalculator.logic.ExpressionCalculator;
import static org.openjdk.jmh.runner.options.TimeValue.milliseconds;

/*
 * Set level="debug" in log4j2.xml to enable logging.
 */
@TestInstance(Lifecycle.PER_CLASS)
public class PerformanceTest {
  public PerformanceTest() {
  }

  @Test
  void run() throws RunnerException {
    final ChainedOptionsBuilder jmhOptions = new OptionsBuilder()
        .jvmArgs("-Xms1536m", "-Xmx1536m")
        .jvmArgsAppend("-server", "-disableassertions")
        .shouldDoGC(true)
        .syncIterations(true)
        .shouldFailOnError(true)
        .threads(1)
        .timeout(milliseconds(5_000))
        .forks(3)
        .warmupTime(milliseconds(200))
        .warmupIterations(10)
        .measurementTime(milliseconds(200))
        .measurementIterations(5);
    new Runner(jmhOptions.include(PerformanceTest.class.getName() + ".*")
        .mode(Mode.AverageTime)
        .timeUnit(TimeUnit.MICROSECONDS)
        .build())
        .run();
  }

  @Benchmark
  public void calculate(final BenchmarkState state, final Blackhole bh) {
    final List<String> expressions = state.expressions;
    final int size = state.expressions.size();
    final int idx = size == 1
        ? 0
        : ThreadLocalRandom.current().nextInt(0, state.expressions.size() - 1);
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
      final URL expressionsUrl = PerformanceTest.class.getClassLoader()
          .getResource("expressions");
      if (expressionsUrl == null) {
        throw new RuntimeException("Can't locate expressions");
      }
      try (final Stream<String> lines = Files.lines(Paths.get(expressionsUrl.toURI()))) {
        expressions = lines.filter(line -> !(line.isEmpty() || line.startsWith("#")))
                .collect(Collectors.toUnmodifiableList());
      } catch (final URISyntaxException | IOException e) {
        throw new RuntimeException(e);
      }
      if (expressions.isEmpty()) {
        throw new RuntimeException("There are no expressions to measure against");
      }
      calculator = new ExpressionCalculator(MathContext.DECIMAL32);
    }
  }
}
