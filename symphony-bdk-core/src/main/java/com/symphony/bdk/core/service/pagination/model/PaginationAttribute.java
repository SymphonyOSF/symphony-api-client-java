package com.symphony.bdk.core.service.pagination.model;

import lombok.Getter;

/**
 * Pagination Attribute model to be used in default pagination methods provided by bdk services class.
 */
@Getter
public class PaginationAttribute {

  private final Integer skip;
  private final Integer limit;

  public PaginationAttribute(Integer skip, Integer limit) {
    if (skip == null || limit == null) {
      throw new IllegalArgumentException("Skip and limit for pagination have to be not null.");
    }
    this.skip = skip;
    this.limit = limit;
  }
}
