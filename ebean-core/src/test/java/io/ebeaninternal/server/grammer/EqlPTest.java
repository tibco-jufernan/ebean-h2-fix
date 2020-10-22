package io.ebeaninternal.server.grammer;

import io.ebean.DB;
import io.ebean.OrderBy;
import io.ebeaninternal.api.SpiQuery;
import org.junit.Test;
import org.tests.model.basic.Customer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class EqlPTest {

  @Test
  public void parse_limit_offset() {
    verify(spyAdapter("limit 10 offset 11")).visitLimitOffset(10, 11);
    verify(spyAdapter("limit 42 offset 99")).visitLimitOffset(42, 99);
  }

  @Test
  public void parse_limit() {
    verify(spyAdapter("limit 12")).visitLimit(12);
    verify(spyAdapter("limit 42")).visitLimit(42);
  }

  @Test
  public void parse_orderBy() {
    verify(spyAdapter("order by name")).visitOrderByClause("name", true, null, null);
    verify(spyAdapter("order by name asc")).visitOrderByClause("name", true, null, null);
    verify(spyAdapter("order by name desc")).visitOrderByClause("name", false, null, null);
    verify(spyAdapter("order by name desc nulls first")).visitOrderByClause("name", false, "nulls", "first");
    verify(spyAdapter("order by name desc nulls last")).visitOrderByClause("name", false, "nulls", "last");
    verify(spyAdapter("order by name nulls last")).visitOrderByClause("name", true, "nulls", "last");
    verify(spyAdapter("order by name nulls first")).visitOrderByClause("name", true, "nulls", "first");
  }

  @Test
  public void parse_orderBy_2() {
    final SpiQuery<Customer> query = query();
    new EqlP(new EqlPAdapter(query), "order by name desc, whenCreated asc").parse();

    final OrderBy<Customer> orderBy = query.getOrderBy();
    final List<OrderBy.Property> properties = orderBy.getProperties();
    assertThat(properties).hasSize(2);
    assertThat(orderBy.toStringFormat()).isEqualTo("name desc, whenCreated");
  }

  private EqlPAdapter spyAdapter(String s) {
    final EqlPAdapter adapter = spy(new EqlPAdapter(query()));
    EqlP parser = new EqlP(adapter, s);
    parser.parse();
    return adapter;
  }

  @Test(expected = IllegalArgumentException.class)
  public void parse_limitoffset_offsetInvalid() {
    parseInvalid("limit 12 offset nan");
  }

  @Test(expected = IllegalArgumentException.class) //Missing offset at position: 15 in: limit 12 offset
  public void parse_limitoffset_offsetMissing() {
    parseInvalid("limit 12 offset");
  }

  @Test(expected = IllegalArgumentException.class) //Invalid value [nan] for limit at position: 9 in: limit nan
  public void parse_limitoffset_limitInvalid() {
    parseInvalid("limit nan");
  }

  @Test(expected = IllegalArgumentException.class) //Missing limit at position: 6 in: limit
  public void parse_limitoffset_limitMissing() {
    parseInvalid("limit ");
  }

  private void parseInvalid(String s) {
    new EqlP(new EqlPAdapter(query()), s).parse();
  }

  private SpiQuery<Customer> query() {
    return (SpiQuery<Customer>)DB.find(Customer.class);
  }

}
