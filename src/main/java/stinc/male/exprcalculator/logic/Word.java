package stinc.male.exprcalculator.logic;

import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.apache.commons.lang3.StringUtils;
import stinc.male.exprcalculator.Main;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static stinc.male.exprcalculator.logic.Word.LogicalType.CALCULATION;
import static stinc.male.exprcalculator.logic.Word.LogicalType.OPERAND;
import static stinc.male.exprcalculator.logic.Word.LogicalType.OPERAND_VAR;
import static stinc.male.exprcalculator.logic.Word.Type.NUMERIC;

@Immutable final class Word {
  private final String word;
  private final Type type;
  private final int position;
  private final LogicalType ltype;
  @Nullable private final BigDecimal value;

  Word(final Word operator, final BigDecimal result) {
    checkNotNull(operator, "The argument %s must not be null", "operator");
    checkArgument(operator.getLogicalType()
        .isOperator(), "The argument %s=%s is invalid", "operator", operator);
    checkNotNull(result, "The argument %s must not be null", "result");
    word = operator.getWord();
    type = NUMERIC;
    position = operator.getPosition();
    ltype = OPERAND;
    value = result;
  }

  Word(final String word, final Type type, final int position, final MathContext mc) {
    checkArgument(StringUtils.isNotBlank(word), "The argument %s=%s must not be blank", "word", word);
    checkNotNull(type, "The argument %s must not be null", "type");
    checkArgument(!type.isIgnorable(), "The argument %s=%s is invalid", "type", type);
    checkArgument(position >= 0, "The argument %s=%s must not be negative", "position", position);
    checkNotNull(mc, "The argument %s must not be null", "mc");
    this.word = word;
    this.type = type;
    this.position = position;
    final Map.Entry<LogicalType, BigDecimal> ltypeAndValue = determineLogicalType(word, type, mc);
    this.ltype = ltypeAndValue.getKey();
    this.value = ltypeAndValue.getValue();
  }

  final String getWord() {
    return word;
  }

  final Type getType() {
    return type;
  }

  final int getPosition() {
    return position;
  }

  final LogicalType getLogicalType() {
    return ltype;
  }

  final BigDecimal getValue() {
    if (value == null) {
      throw new Error(String.format("%s does not have value", this));
    }
    return value;
  }

  @Override
  public final String toString() {
    return getClass().getSimpleName() + "{word='" + word + "'" + ", type=" + type + ", position=" + position + ", ltype=" + ltype + ", value=" +
        value + '}';
  }

  @Override
  public final boolean equals(@Nullable final Object o) {
    final boolean result;
    if (this == o) {
      result = true;
    } else if (o instanceof Word) {
      final Word obj = (Word)o;
      result = word.equals(obj.word) && type == obj.type && position == obj.position;
    } else {
      result = false;
    }
    return result;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(word, type, position);
  }

  private static final Map.Entry<LogicalType, BigDecimal> determineLogicalType(final String word, final Type type, final MathContext mc) {
    final Map.Entry<LogicalType, BigDecimal> result;
    switch (type) {
      case LITERAL: {
        @Nullable final LogicalType ltype = LogicalType.operationsIndex.get(word.toLowerCase(Main.locale));
        if (ltype == null) {
          result = new SimpleImmutableEntry<>(OPERAND_VAR, null);
        } else {
          result = new SimpleImmutableEntry<>(ltype, null);
        }
        break;
      }
      case NUMERIC: {
        result = new SimpleImmutableEntry<>(OPERAND, new BigDecimal(word, mc));
        break;
      }
      case CLOSING_BRACKET: {
        result = new SimpleImmutableEntry<>(CALCULATION, null);
        break;
      }
      default: {
        throw new Error(String.format("%s is not allowed here", type));
      }
    }
    return result;
  }

  enum Type {
    EMPTY(true),
    LITERAL(false),
    NUMERIC(false),
    OPENING_BRACKET(true),
    CLOSING_BRACKET(false),
    COMMA(true);

    private final boolean ignorable;

    Type(final boolean ignorable) {
      this.ignorable = ignorable;
    }

    final boolean isIgnorable() {
      return ignorable;
    }
  }

  enum LogicalType {
    OPERATOR_LET(true, null),
    OPERATOR_ADD(true, BigDecimal::add),
    OPERATOR_SUB(true, BigDecimal::subtract),
    OPERATOR_MULT(true, BigDecimal::multiply),
    OPERATOR_DIV(true, BigDecimal::divide),
    OPERAND(false, null),
    OPERAND_VAR(false, null),
    CALCULATION(false, null);

    private final boolean operator;
    @Nullable private final Operation operation;

    LogicalType(
        final boolean operator, @Nullable Operation operation) {
      this.operator = operator;
      this.operation = operation;
    }

    final boolean isOperator() {
      return operator;
    }

    final boolean isCalculationSupported() {
      return operation != null;
    }

    final BigDecimal calculate(final BigDecimal v1, final BigDecimal v2, final MathContext mc) {
      checkNotNull(v1, "The argument %s must not be null", "v1");
      checkNotNull(v2, "The argument %s must not be null", "v2");
      checkNotNull(mc, "The argument %s must not be null", "mc");
      if (!isCalculationSupported()) {
        throw new Error(String.format("%s does not support calculation", this));
      }
      assert operation != null;
      return operation.apply(v1, v2, mc);
    }

    private static final Map<String, LogicalType> operationsIndex = ImmutableMap.of("let",
        OPERATOR_LET,
        "add",
        OPERATOR_ADD,
        "sub",
        OPERATOR_SUB,
        "mult",
        OPERATOR_MULT,
        "div",
        OPERATOR_DIV);

    @FunctionalInterface private interface Operation {
      BigDecimal apply(BigDecimal v1, BigDecimal v2, MathContext mc);
    }
  }
}