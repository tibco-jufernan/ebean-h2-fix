package io.ebeaninternal.server.grammer;

import org.junit.Test;

import static org.junit.Assert.*;

public class EqlTokenizerTest {

  @Test
  public void nextToken_basic() {
    assertEquals("hello", new EqlTokenizer("hello").nextToken());
    assertEquals("hello", new EqlTokenizer("hello ").nextToken());
    assertEquals("hello", new EqlTokenizer("hello there").nextToken());
  }

  @Test
  public void nextToken_expect_trimLeading() {
    assertEquals("hello", new EqlTokenizer("  hello").nextToken());
    assertEquals("hello", new EqlTokenizer("\thello").nextToken());
    assertEquals("hello", new EqlTokenizer("\nhello").nextToken());
    assertEquals("hello", new EqlTokenizer(" \t \t \r \n hello").nextToken());
  }

  @Test
  public void nextToken_quoted() {
    assertEquals("'hello there'", new EqlTokenizer("'hello there'").nextToken());
    assertEquals("'hello there'", new EqlTokenizer("  'hello there'").nextToken());
    assertEquals("'hello,there'", new EqlTokenizer("'hello,there'").nextToken());
  }

  @Test
  public void nextToken_quoted_escape() {
    assertEquals("'hello\\'there'", new EqlTokenizer("'hello\\'there'").nextToken());
    assertEquals("' hello\\'there '", new EqlTokenizer(" ' hello\\'there ' ").nextToken());
    assertEquals("'hello\\there '", new EqlTokenizer("'hello\\there '").nextToken());
  }

  @Test
  public void nextToken_openBracket() {
    assertEquals("hello", new EqlTokenizer("hello(").nextToken());
    assertEquals("hello", new EqlTokenizer("hello()").nextToken());
  }

  @Test
  public void nextToken_multi() {
    final EqlTokenizer tokenizer = new EqlTokenizer("hello()there");

    assertEquals("hello", tokenizer.nextToken());
    assertEquals("(", tokenizer.nextToken());
    assertEquals(")", tokenizer.nextToken());
    assertEquals("there", tokenizer.nextToken());
    assertNull(tokenizer.nextToken());
  }

  @Test
  public void nextToken_multi_withWhitespace() {
    final EqlTokenizer tokenizer = new EqlTokenizer("  hello  (  )  there  ");
    assertEquals("hello", tokenizer.nextToken());
    assertEquals("(", tokenizer.nextToken());
    assertEquals(")", tokenizer.nextToken());
    assertEquals("there", tokenizer.nextToken());
    assertNull(tokenizer.nextToken());
  }

  @Test
  public void nextToken_multi_withWhitespace_eql() {

    final EqlTokenizer tokenizer = new EqlTokenizer("select distinct name,dob fetch billingAddress ");

    assertEquals("select", tokenizer.nextToken());
    assertEquals("distinct", tokenizer.nextToken());
    assertEquals("name", tokenizer.nextToken());
    assertEquals(",", tokenizer.nextToken());
    assertEquals("dob", tokenizer.nextToken());
    assertEquals("fetch", tokenizer.nextToken());
    assertEquals("billingAddress", tokenizer.nextToken());
    assertNull(tokenizer.nextToken());
  }

  @Test
  public void readUntilCloseBracket() {
    final EqlTokenizer tokenizer = new EqlTokenizer("fetch (any thing in here) after");

    assertEquals("fetch", tokenizer.nextToken());
    assertEquals("(", tokenizer.nextToken());
    assertEquals("any thing in here", tokenizer.nextUntilCloseBracket());
    assertEquals("after", tokenizer.nextToken());
  }

  @Test
  public void readUntilCloseBracket_empty() {
    EqlTokenizer tokenizer = new EqlTokenizer("()after");
    assertEquals("(", tokenizer.nextToken());
    assertEquals("", tokenizer.nextUntilCloseBracket());
    assertEquals("after", tokenizer.nextToken());

    tokenizer = new EqlTokenizer("() after");
    assertEquals("(", tokenizer.nextToken());
    assertEquals("", tokenizer.nextUntilCloseBracket());
    assertEquals("after", tokenizer.nextToken());
  }

  @Test
  public void readUntilCloseBracket_empty_then_null() {
    EqlTokenizer tokenizer = new EqlTokenizer("()");
    assertEquals("(", tokenizer.nextToken());
    assertEquals("", tokenizer.nextUntilCloseBracket());
    assertNull(tokenizer.nextToken());
  }

  @Test
  public void readUntilCloseBracket_some_then_null() {
    EqlTokenizer tokenizer = new EqlTokenizer("(foo in here)");
    assertEquals("(", tokenizer.nextToken());
    assertEquals("foo in here", tokenizer.nextUntilCloseBracket());
    assertNull(tokenizer.nextToken());
  }

  @Test
  public void readUntilCloseBracket_nested() {
    final EqlTokenizer tokenizer = new EqlTokenizer("(any (one(two)) thing)");

    assertEquals("(", tokenizer.nextToken());
    assertEquals("any (one(two)) thing", tokenizer.nextUntilCloseBracket());
    assertNull(tokenizer.nextToken());
  }

  @Test
  public void readUntilCloseBracket_nested2() {
    final EqlTokenizer tokenizer = new EqlTokenizer("((any (one(two)) thing))");

    assertEquals("(", tokenizer.nextToken());
    assertEquals("(any (one(two)) thing)", tokenizer.nextUntilCloseBracket());
    assertNull(tokenizer.nextToken());
  }

  @Test
  public void readUntilCloseBracket_nested3() {
    final EqlTokenizer tokenizer = new EqlTokenizer("((any (one(two)) thing) but...)");

    assertEquals("(", tokenizer.nextToken());
    assertEquals("(any (one(two)) thing) but...", tokenizer.nextUntilCloseBracket());
    assertNull(tokenizer.nextToken());
  }
}
