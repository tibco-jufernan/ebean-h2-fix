package io.ebeaninternal.server.grammer;

import static java.lang.Character.isWhitespace;

public class EqlTokenizer {

  static final int F_LIMIT = 10;
  static final int F_OFFSET = 11;
  static final int F_FETCHBATCHSIZE = 12;

  private static final char C_OPENBRACKET = '(';
  private static final char C_CLOSEBRACKET = ')';
  private static final char C_COMMA = ',';
  private static final char C_SINGLEQUOTE = '\'';
  private static final char C_QUOTEESCAPE = '\\';
  private static final char C_SPACE = ' ';

  private final String source;
  private final int sourceLength;
  private final char[] sourceChars;
  private int pos;
  private int tokenStart;
  private String lastKeyword;

  public EqlTokenizer(String source) {
    this.source = source;
    this.sourceLength = source.length();
    this.sourceChars = source.toCharArray();
  }

  @Override
  public String toString() {
    return source;
  }

  public int pos() {
    return pos;
  }

  public String lastKeyword() {
    return lastKeyword;
  }

  public String nextUntilKeyword(int from) {
    // move until keyword fetch|where|order|limit
    int blockStart = from;
    while(true) {
      final String next = nextKeyword();
      if (next == null) {
        return source.substring(blockStart);
      }
      switch (next) {
        case "fetch":
        case "where":
        case "order":
        case "limit": {
          int end = this.pos - next.length() - 1;
          lastKeyword = next;
          return source.substring(blockStart, end).trim();
        }
      }
    }
  }

  /**
   * Return the next keyword taking into account quotes and brackets.
   */
  private String nextKeyword() {
    boolean quoting = false;
    int bracket = 0;
    tokenStart = pos;
    while (pos < sourceLength) {
      final char ch = sourceChars[pos];
      switch (ch) {
        case C_SPACE: {
          if (bracket == 0 && !quoting) {
            if (tokenStart == -1) {
              tokenStart = pos; // potential start of keyword
            } else {
              return source.substring(tokenStart, pos++);
            }
          }
          break;
        }
        case C_SINGLEQUOTE: {
          if (bracket == 0) {
            quoting = !quoting; // swap quoting mode
            tokenStart = -1; // not a keyword
          }
          break;
        }
        case C_QUOTEESCAPE:
          if (quoting) {
            pos++;
          }
          break;
        case C_OPENBRACKET: {
          if (!quoting) {
            bracket++;
            tokenStart = -1; // not a keyword
          }
          break;
        }
        case C_CLOSEBRACKET: {
          if (!quoting) {
            --bracket;
          }
          break;
        }
      }
      pos++;
    }
    return null; // no next keyword
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

}
