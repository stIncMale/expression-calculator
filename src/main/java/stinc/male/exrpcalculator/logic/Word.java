package stinc.male.exrpcalculator.logic;

import java.util.Objects;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

final class Word {
  private final String word;
  private final Type type;
  private final int position;

  Word(
      final String word,
      final Type type,
      final int position) {
    checkArgument(StringUtils.isNotBlank(word), "The argument %s=%s must not be blank", "word", word);
    checkNotNull(type, "The argument %s must not be null", "type");
    checkArgument(position >= 0, "The argument %s=%s must not be negative", "position", position);
    this.word = word;
    this.type = type;
    this.position = position;
  }

  public final String getWord() {
    return word;
  }

  public final Type getType() {
    return type;
  }

  public final int getPosition() {
    return position;
  }

  @Override
  public final String toString() {
    return getClass().getSimpleName()
        + "{word=" + word
        + ", type=" + type
        + ", position=" + position + '}';
  }

  @Override
  public final boolean equals(@Nullable final Object o) {
    final boolean result;
    if (this == o) {
      result = true;
    } else if (o instanceof Word) {
      final Word obj = (Word)o;
      result = word.equals(obj.word)
          && type == obj.type
          && position == obj.position;
    } else {
      result = false;
    }
    return result;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(word, type, position);
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
}