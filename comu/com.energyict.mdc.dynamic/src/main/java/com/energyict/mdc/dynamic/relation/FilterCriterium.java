package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.SqlBuilder;

import aQute.bnd.annotation.ProviderType;

/**
 * Not part of the API
 */
@ProviderType
public interface FilterCriterium {

    public void appendWhereClause(SqlBuilder builder);

}