package io.ebeaninternal.server.grammer;

import static java.lang.Character.isWhitespace;

public class EqlTokenizer {

  private static final char C_OPENBRACKET = '(';
  private static final char C_CLOSEBRACKET = ')';
  private static final char C_COMMA = ',';
  private static final char C_SINGLEQUOTE = '\'';
  private static final char C_QUOTEESCAPE = '\\';

  private final String source;
  private final int sourceLength;
  private final char[] sourceChars;
  private int pos;
  private int tokenStart;

  public EqlTokenizer(String source) {
    this.source = source;
    this.sourceLength = source.length();
    this.sourceChars = source.toCharArray();
  }

  @Override
  public String toString() {
    return source;
  }

  /**
   * Read everything until a closing bracket.
   */
  public String nextUntilCloseBracket() {
    tokenStart = pos;
    int depth = 1;
    while (pos < sourceLength) {
      final char ch = sourceChars[pos];
      switch (ch) {
        case C_OPENBRACKET: {
          depth++;
          break;
        }
        case C_CLOSEBRACKET: {
          --depth;
          if (depth == 0) {
            return source.substring(tokenStart, pos++);
          }
          break;
        }
      }
      pos++;
    }
    throw new IllegalStateException("Unable to find matching closing bracket ) " + positionInSource());
  }

  public String nextToken() {
    if (!skip()) {
      return null;
    }
    tokenStart = pos;
    switch (sourceChars[pos++]) {
      case C_OPENBRACKET:
        return EToken.OPEN_BRACKET;
      case C_CLOSEBRACKET:
        return EToken.CLOSE_BRACKET;
      case C_COMMA:
        return EToken.COMMA;
      case C_SINGLEQUOTE:
        return readQuoted();
      default:
        return readWord();
    }
  }

  private String readWord() {
    while (pos < sourceLength) {
      final char ch = sourceChars[pos];
      switch (ch) {
        case C_COMMA:
        case C_OPENBRACKET:
        case C_CLOSEBRACKET: {
          return source.substring(tokenStart, pos);
        }
        default:
          if (isWhitespace(ch)) {
            return source.substring(tokenStart, pos);
          }
      }
      pos++;
    }
    return source.substring(tokenStart, pos);
  }

  private String readQuoted() {
    while (pos < sourceLength) {
      final char ch = sourceChars[pos++];
      switch (ch) {
        case C_QUOTEESCAPE:
          pos++;
          break;
        case C_SINGLEQUOTE:
          return source.substring(tokenStart, pos);
      }
    }
    throw new IllegalStateException("Didn't find end of literal starting at " + tokenStart + " in " + source);
  }

  private boolean skip() {
    while (pos < sourceLength) {
      if (!isWhitespace(sourceChars[pos])) {
        return true;
      }
      pos++;
    }
    return false;
  }

  public String positionInSource() {
    return " at position: " + pos + " in: " + source;
  }

  public int nextTokenInt(int fieldId) {
    final String val = nextToken();
    if (val == null) {
      throw missing(fieldId);
    }
    try {
      return Integer.parseInt(val);
    } catch (NumberFormatException e) {
      throw invalid(fieldId, val);
    }
  }

  private IllegalArgumentException invalid(int fieldId, String val) {
    return new IllegalArgumentException(msgInvalid(fieldId, val) + positionInSource());
  }

  private IllegalArgumentException missing(int fieldId) {
    return new IllegalArgumentException(msgMissing(fieldId) + positionInSource());
  }

  private String msgInvalid(int fieldId, String val) {
    return "Invalid value [" + val + "] for " + field(fieldId);
  }

  private String msgMissing(int fieldId) {
    return "Missing " + field(fieldId);
  }

  private String field(int fieldId) {
    switch (fieldId) {
      case F_LIMIT:
        return "limit";
      case F_OFFSET:
        return "offset";
      case F_FETCHBATCHSIZE:
        return "fetch batch size";
    }
    return "<Unknown field " + fieldId + ">";
  }

  static final int F_LIMIT = 10;
  static final int F_OFFSET = 11;
  static final int F_FETCHBATCHSIZE = 12;

}
