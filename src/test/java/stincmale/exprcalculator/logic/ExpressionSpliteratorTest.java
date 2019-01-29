package stincmale.exprcalculator.logic;

import java.math.MathContext;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import stincmale.exprcalculator.logic.Word.Type;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(Lifecycle.PER_METHOD)
public final class ExpressionSpliteratorTest {
  private static final MathContext mc = MathContext.DECIMAL32;

  public ExpressionSpliteratorTest() {
  }

  @Test
  public final void parse1() {
    final String expr = "let(a, let(b, 10, add(b, b)), let(b, 20, add(a,b)))";
    final List<Word> words = StreamSupport.stream(new ExpressionSpliterator(expr, mc), false)
        .collect(Collectors.toList());
    assertFalse(words.stream()
        .anyMatch(w -> w.getType()
            .isIgnorable()));//assert there are no ignorable words
    assertEquals(new Word("let", Type.LITERAL, 0, mc, new Word.LogicalTypeValuePair()), words.get(0));
    assertEquals(new Word(")", Type.CLOSING_BRACKET, expr.length() - 1, mc, new Word.LogicalTypeValuePair()), words.get(words.size() - 1));
    assertEquals(new Word("10", Type.NUMERIC, 14, mc, new Word.LogicalTypeValuePair()), words.get(4));
  }

  @Test
  public final void parse2() {
    final String expr = "let ( \n _a_d   , -5.67, let(bw, mult \t (_a_d, 10  ), add(bw, _a_d)))";
    final List<Word> words = StreamSupport.stream(new ExpressionSpliterator(expr, mc), false)
        .collect(Collectors.toList());
    assertFalse(words.stream()
        .anyMatch(w -> w.getType()
            .isIgnorable()));//assert there are no ignorable words
    assertEquals(new Word("let", Type.LITERAL, 0, mc, new Word.LogicalTypeValuePair()), words.get(0));
    assertEquals(new Word(")", Type.CLOSING_BRACKET, expr.length() - 1, mc, new Word.LogicalTypeValuePair()), words.get(words.size() - 1));
    assertEquals(new Word("-5.67", Type.NUMERIC, 17, mc, new Word.LogicalTypeValuePair()), words.get(2));
    assertEquals(new Word("_a_d", Type.LITERAL, 40, mc, new Word.LogicalTypeValuePair()), words.get(6));
  }

  @Test
  public final void parse3() {
    assertThrows(CalculationException.class, () -> new ExpressionSpliterator("le-t(a, 10, add(a,1))", mc).forEachRemaining(w -> {}));
  }

  @Test
  public final void parseBracketsBalance1() {
    assertThrows(CalculationException.class, () -> new ExpressionSpliterator("(()", mc).forEachRemaining(w -> {}));
  }

  @Test
  public final void parseBracketsBalance2() {
    new ExpressionSpliterator("(()(()))", mc).forEachRemaining(w -> {});
  }

  @Test
  public final void parseBracketsBalance3() {
    assertThrows(CalculationException.class, () -> new ExpressionSpliterator(")(", mc).forEachRemaining(w -> {}));
  }
}