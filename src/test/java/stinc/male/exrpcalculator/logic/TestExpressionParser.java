package stinc.male.exrpcalculator.logic;

import java.math.MathContext;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static stinc.male.exrpcalculator.logic.ExpressionParser.parse;
import static stinc.male.exrpcalculator.logic.Word.Type.CLOSING_BRACKET;
import static stinc.male.exrpcalculator.logic.Word.Type.LITERAL;
import static stinc.male.exrpcalculator.logic.Word.Type.NUMERIC;

public final class TestExpressionParser {
  private static final MathContext mc = MathContext.DECIMAL32;

  public TestExpressionParser() {
  }

  @Test
  public final void testWords1() {
    final String expr = "let(a, let(b, 10, add(b, b)), let(b, 20, add(a,b)))";
    final List<Word> words = parse(expr, mc);
    assertFalse(words.stream()
        .filter(w -> w.getType().isIgnorable())
        .findAny()
        .isPresent());//assert there are no ignorable words
    assertEquals(new Word("let", LITERAL, 0, mc), words.get(0));
    assertEquals(new Word(")", CLOSING_BRACKET, expr.length() - 1, mc), words.get(words.size() - 1));
    assertEquals(new Word("10", NUMERIC, 14, mc), words.get(4));
  }

  @Test
  public final void testWords2() {
    final String expr = "let ( \n _a_d   , -5.67, let(bw, mult \t (_a_d, 10  ), add(bw, _a_d)))";
    final List<Word> words = parse(expr, mc);
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
  public final void testWords3() {
    parse("le-t(a, 10, add(a,1))", mc);
  }

  @Test(expected = CalculationException.class)
  public final void testWordsBracketsBalance1() {
    parse("(()", mc);
  }

  @Test()
  public final void testWordsBracketsBalance2() {
    parse("(()(()))", mc);
  }

  @Test(expected = CalculationException.class)
  public final void testWordsBracketsBalance3() {
    parse(")(", mc);
  }
}