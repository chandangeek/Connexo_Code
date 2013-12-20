package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.SqlBuilder;

/**
 * Not part of the API
 */
public class RelationSearchFilter {

    private FilterCriterium criterium;

    public RelationSearchFilter(FilterCriterium criterium) {
        this.criterium = criterium;
    }

    public void appendWhereClause(SqlBuilder builder) {
        builder.appendWhereOrAnd();
        criterium.appendWhereClause(builder);
    }

}

