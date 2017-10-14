package stinc.male.exprcalculator.logic;

import java.math.MathContext;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static stinc.male.exprcalculator.logic.Word.Type.CLOSING_BRACKET;
import static stinc.male.exprcalculator.logic.Word.Type.LITERAL;
import static stinc.male.exprcalculator.logic.Word.Type.NUMERIC;

public final class TestExpressionSpliterator {
  private static final MathContext mc = MathContext.DECIMAL32;

  public TestExpressionSpliterator() {
  }

  @Test
  public final void parse1() {
    final String expr = "let(a, let(b, 10, add(b, b)), let(b, 20, add(a,b)))";
    final List<Word> words = StreamSupport.stream(new ExpressionSpliterator(expr, mc), false).collect(Collectors.toList());
    assertFalse(words.stream()
        .filter(w -> w.getType().isIgnorable())
        .findAny()
        .isPresent());//assert there are no ignorable words
    assertEquals(new Word("let", LITERAL, 0, mc), words.get(0));
    assertEquals(new Word(")", CLOSING_BRACKET, expr.length() - 1, mc), words.get(words.size() - 1));
    assertEquals(new Word("10", NUMERIC, 14, mc), words.get(4));
  }

  @Test
  public final void parse2() {
    final String expr = "let ( \n _a_d   , -5.67, let(bw, mult \t (_a_d, 10  ), add(bw, _a_d)))";
    final List<Word> words = StreamSupport.stream(new ExpressionSpliterator(expr, mc), false).collect(Collectors.toList());
    assertFalse(words.stream()
        .filter(w -> w.getType().isIgnorable())
        .findAny()
        .isPresent());//assert there are no ignorable words
    assertEquals(new Word("let", LITERAL, 0, mc), words.get(0));
    assertEquals(new Word(")", CLOSING_BRACKET, expr.length() - 1, mc), words.get(words.size() - 1));
    assertEquals(new Word("-5.67", NUMERIC, 17, mc), words.get(2));
    assertEquals(new Word("_a_d", LITERAL, 40, mc), words.get(6));
  }

  @Test(expected = CalculationException.class)
  public final void parse3() {
    new ExpressionSpliterator("le-t(a, 10, add(a,1))", mc)
        .forEachRemaining(w -> {});
  }

  @Test(expected = CalculationException.class)
  public final void parseBracketsBalance1() {
    new ExpressionSpliterator("(()", mc)
        .forEachRemaining(w -> {});
  }

  @Test()
  public final void parseBracketsBalance2() {
    new ExpressionSpliterator("(()(()))", mc)
        .forEachRemaining(w -> {});
  }

  @Test(expected = CalculationException.class)
  public final void parseBracketsBalance3() {
    new ExpressionSpliterator(")(", mc)
        .forEachRemaining(w -> {});
  }
}