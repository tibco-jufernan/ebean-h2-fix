package io.ebeaninternal.server.grammer;

import java.util.ArrayList;
import java.util.List;

import static io.ebeaninternal.server.grammer.EqlTokenizer.F_FETCHBATCHSIZE;
import static io.ebeaninternal.server.grammer.EqlTokenizer.F_LIMIT;
import static io.ebeaninternal.server.grammer.EqlTokenizer.F_OFFSET;

public class EqlP {

  private static final String KEYWORD_OFFSET = "offset";
  private static final String KEYWORD_LIMIT = "limit";
  private static final String KEYWORD_SELECT = "select";
  private static final String KEYWORD_FETCH = "fetch";
  private static final String KEYWORD_WHERE = "where";
  private static final String KEYWORD_ORDER = "order";
  private static final String KEYWORD_BY = "by";
  private static final String KEYWORD_QUERY = "query";
  private static final String KEYWORD_LAZY = "lazy";
  private static final String KEYWORD_DISTINCT = "distinct";

  private static final String NULLS = "nulls";
  private static final String ASC = "asc";

  private final EqlPAdapter<?> adapter;
  private final EqlTokenizer tokenizer;

  private boolean selectDistinct;
  private String selectProperties;
  private String fetchOption;
  private String fetchPath;
  private String fetchProperties;
  private int fetchBatchSize;

  public EqlP(EqlPAdapter<?> adapter, String eql) {
    this.adapter = adapter;
    this.tokenizer = new EqlTokenizer(eql);
  }

  private void endSelectClause() {
    adapter.visitSelect(selectDistinct, selectProperties);
  }

  private void beginFetchClause() {
    fetchOption = null;
    fetchBatchSize = 0;
    fetchPath = null;
    fetchProperties = null;
  }

  private void endFetchClause() {
    adapter.visitFetch(fetchPath, fetchProperties, fetchOption, fetchBatchSize);
  }

  public void parse() {
    final String token = next();
    switch (token) {
      case KEYWORD_SELECT:
        parseSelect();
        break;
      case KEYWORD_FETCH:
        parseFetch();
        break;
      case KEYWORD_WHERE:
        parseWhere();
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

  private void parseWhere() {
    // where expr|( |and|or|not|()
    // where e0 or e1 or e2 or
    // where (e0 or e1) and (e3 and e4)
    // where (e0 or not e1) and (e3 and not e4)
    // where e0 and not (e1)
    final String next = next();
    switch (next) {
      case EToken.OPEN_BRACKET: {

      }
    }
    // conditional_expression : conditional_term ('or' conditional_term)*
    // conditional_term : conditional_factor ('and' conditional_factor)*
    // conditional_factor : 'not'? conditional_primary
    // conditional_primary : any_expression  | '(' conditional_expression ')'

    // any_expression
//    : comparison_expression
//      | like_expression
//      | inrange_expression
//      | between_expression
//      | propertyBetween_expression
//      | inOrEmpty_expression
//      | in_expression
//      | isNull_expression
//      | isNotNull_expression
//      | isEmpty_expression
//      | isNotEmpty_expression
//      | '(' any_expression ')'
  }

  private void parseLimit() {
    int limit = nextInt(F_LIMIT);
    final String next = next();
    if (next == null) {
      adapter.visitLimit(limit);
    } else {
      if (!next.equals(KEYWORD_OFFSET)) {
        throw iae("Expecting offset keyword");
      }
      adapter.visitLimitOffset(limit, nextInt(F_OFFSET));
    }
  }

  private void parseOrderBy() {
    final String by = next();
    if (!KEYWORD_BY.equals(by)) {
      throw iae("Expecting by keyword for order by clause");
    }
    while(true) {
      if (!parseOrderByClause()) break;
    }
  }

  private boolean parseOrderByClause() {
    // read tokens until comma or limit
    List<String> tokens = new ArrayList<>(4);
    do {
      String next = next();
      if (next == null) {
        pushOrderByClause(tokens);
        return false;
      } else if (next.equals(KEYWORD_LIMIT)) {
        pushOrderByClause(tokens);
        parseLimit();
        return false;
        // push tokens, read limit
      } else if (next.equals(EToken.COMMA)) {
        pushOrderByClause(tokens);
        return true;
      } else {
        tokens.add(next);
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
    // 'fetch' fetch_option? fetch_path_path fetch_property_set?
    beginFetchClause();
    final String next = next();
    if (next == null) {
      throw iae("Empty fetch clause? Expecting a fetch path");
    }
    switch (next) {
      case KEYWORD_QUERY:
        parseFetchOption(KEYWORD_QUERY);
        break;
      case KEYWORD_LAZY:
        parseFetchOption(KEYWORD_LAZY);
        break;
      default:
        parseFetchPath(next);
    }
  }

  private void parseFetchPath(String token) {
    fetchPath = token;
    final String next = next();
    if (EToken.OPEN_BRACKET.equals(next)) {
      fetchProperties = tokenizer.nextUntilCloseBracket();
      parseNextFetch(next());
    } else {
      parseNextFetch(next);
    }
  }

  private void parseNextFetch(String next) {
    endFetchClause();
    parseKeyword(next);
  }

  private void parseKeyword(String next) {
    if (next == null) {
      return;
    }
    switch (next) {
      case KEYWORD_FETCH:
        parseFetch();
        break;
      case KEYWORD_WHERE:
        parseWhere();
        break;
      case KEYWORD_ORDER:
        parseOrderBy();
        break;
      case KEYWORD_LIMIT:
        parseLimit();
        break;
      default:
        throw iae("Expecting keyword fetch|order|limit but got " + next);
    }
  }

  private void parseFetchOption(String fetchOption) {
    this.fetchOption = fetchOption;
    final String next = next();
    if (next == null) {
      throw iae("Expecting fetch batch size");
    }
    if (EToken.OPEN_BRACKET.equals(next)) {
      parseFetchOptionBatchSize();
    } else {
      parseFetchPath(next);
    }
  }

  private void parseFetchOptionBatchSize() {
    fetchBatchSize = nextInt(F_FETCHBATCHSIZE);
    if (!EToken.CLOSE_BRACKET.equals(next())) {
      throw iae("Expecting ) close bracket for fetch batch size");
    }
    parseFetchPath(next());
  }

  private void parseSelect() {
    // 'select' distinct? select_properties
    // select_properties =  : '(' fetch_property_group ')' | fetch_property_group
    int from = tokenizer.pos();
    String next = next();
    if (KEYWORD_DISTINCT.equals(next)) {
      from = tokenizer.pos();
      selectDistinct = true;
      next = next();
    }
    if (EToken.OPEN_BRACKET.equals(next)) {
      selectProperties = tokenizer.nextUntilCloseBracket().trim();
      endSelectClause();
      parseKeyword(next());
    } else {
      selectProperties = tokenizer.nextUntilKeyword(from).trim();
      endSelectClause();
      parseKeyword(tokenizer.lastKeyword());
    }
  }

  private int nextInt(int field) {
    return tokenizer.nextTokenInt(field);
  }

  private String next() {
    return tokenizer.nextToken();
  }

  private IllegalArgumentException iae(String prefix) {
    return new IllegalArgumentException(prefix + tokenizer.positionInSource());
  }

}
