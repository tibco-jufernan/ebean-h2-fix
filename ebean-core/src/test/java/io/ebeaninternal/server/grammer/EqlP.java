package io.ebeaninternal.server.grammer;

import java.util.ArrayList;
import java.util.List;

import static io.ebeaninternal.server.grammer.EqlTokenizer.F_LIMIT;
import static io.ebeaninternal.server.grammer.EqlTokenizer.F_OFFSET;

public class EqlP {

  private static final String KEYWORD_OFFSET = "offset";
  private static final String KEYWORD_LIMIT = "limit";
  private static final String KEYWORD_SELECT = "select";
  private static final String KEYWORD_FETCH = "fetch";
  private static final String KEYWORD_ORDER = "order";
  private static final String KEYWORD_BY = "by";

  private static final String NULLS = "nulls";
  private static final String ASC = "asc";

  private final EqlPAdapter<?> adapter;
  private final EqlTokenizer tokenizer;

  public EqlP(EqlPAdapter<?> adapter, String eql) {
    this.adapter = adapter;
    this.tokenizer = new EqlTokenizer(eql);
  }

  public void parse() {
    final String token = tokenizer.nextToken();
    switch (token) {
      case KEYWORD_SELECT:
        parseSelect();
        break;
      case KEYWORD_FETCH:
        parseFetch();
        break;
      case KEYWORD_ORDER:
        parseOrderBy();
        break;
      case KEYWORD_LIMIT:
        parseLimit();
        break;
      default:
        throw iae("Expecting keyword select|fetch|order|limit but got " + token);
    }
  }

  private void parseLimit() {
    int limit = tokenizer.nextTokenInt(F_LIMIT);
    final String maybeOffset = tokenizer.nextToken();
    if (maybeOffset == null) {
      adapter.visitLimit(limit);
    } else {
      if (!maybeOffset.equals(KEYWORD_OFFSET)) {
        throw iae("Expecting offset keyword");
      }
      adapter.visitLimitOffset(limit, tokenizer.nextTokenInt(F_OFFSET));
    }
  }


  private void parseOrderBy() {
    final String by = tokenizer.nextToken();
    if (!by.equals(KEYWORD_BY)) {
      throw iae("Expecting by keyword");
    }
    while(true) {
      if (!parseOrderByClause()) break;
    }
  }

  private boolean parseOrderByClause() {
    // read tokens until comma or limit
    List<String> tokens = new ArrayList<>(4);
    do {
      String tk = tokenizer.nextToken();
      if (tk == null) {
        pushOrderByClause(tokens);
        return false;
      } else if (tk.equals(KEYWORD_LIMIT)) {
        pushOrderByClause(tokens);
        parseLimit();
        return false;
        // push tokens, read limit
      } else if (tk.equals(EToken.COMMA)) {
        pushOrderByClause(tokens);
        return true;
        //parseOrderByClause();
      } else {
        tokens.add(tk);
      }
    } while (true);
  }

  private void pushOrderByClause(List<String> tokens) {
    String path = tokens.get(0);
    boolean asc = true;
    String nulls = null;
    String nullsFirstLast = null;

    final int size = tokens.size();
    if (size == 4) {
      asc = tokens.get(1).startsWith(ASC);
      nullsFirstLast = tokens.get(3);
      nulls = NULLS;
    } else if (size == 3) {
      String firstChild = tokens.get(1);
      if (firstChild.startsWith(NULLS)) {
        nullsFirstLast = tokens.get(2);
        nulls = NULLS;
      } else {
        asc = firstChild.startsWith(ASC);
      }
    } else if (size == 2) {
      asc = tokens.get(1).startsWith(ASC);
    }
    adapter.visitOrderByClause(path, asc, nulls, nullsFirstLast);
  }

  private void parseFetch() {

  }

  private void parseSelect() {

  }

  private IllegalArgumentException iae(String prefix) {
    return new IllegalArgumentException(prefix + tokenizer.positionInSource());
  }

}
