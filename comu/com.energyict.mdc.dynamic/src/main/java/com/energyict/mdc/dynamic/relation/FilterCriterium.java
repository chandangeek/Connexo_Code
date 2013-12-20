package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.SqlBuilder;

/**
 * Not part of the API
 */
public interface FilterCriterium {

    public void appendWhereClause(SqlBuilder builder);

}