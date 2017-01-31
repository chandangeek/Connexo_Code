/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.groups.spi.QueryProvider;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.sql.SqlFragment;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface QueryGroup<T extends HasId & IdentifiedObject> extends Group<T> {

    SearchDomain getSearchDomain();

    QueryProvider<T> getQueryProvider();

    SqlFragment toFragment();

    List<SearchablePropertyValue> getSearchablePropertyValues();

    void setConditions(List<SearchablePropertyValue> conditions);

    List<SearchablePropertyCondition> getSearchablePropertyConditions();

}
