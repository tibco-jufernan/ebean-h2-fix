package io.ebeaninternal.server.grammer;

import io.ebean.FetchConfig;
import io.ebean.OrderBy;
import io.ebeaninternal.api.SpiQuery;

public class EqlPAdapter<T> {

  private final SpiQuery<T> query;

  public EqlPAdapter(SpiQuery<T> query) {
    this.query = query;
  }

  public void visitSelect(boolean distinct, String properties) {
    if (distinct) {
      query.setDistinct(true);
    }
    if (properties != null) {
      query.select(properties);
    }
  }

  public void visitFetch(String path, String properties, String option, int batchSize) {
    query.fetch(path, properties, FetchConfig.of(option, batchSize));
  }

  public void visitOrderByClause(String path, boolean asc, String nulls, String highLow) {
    query.order().add(new OrderBy.Property(path, asc, nulls, highLow));
  }

  public void visitLimit(int limit) {
    query.setMaxRows(limit);
  }

  public void visitLimitOffset(int limit, int offset) {
    query.setMaxRows(limit);
    query.setFirstRow(offset);
  }

}
