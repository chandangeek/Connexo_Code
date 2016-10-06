package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.groups.spi.EndDeviceQueryProvider;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.sql.SqlFragment;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface QueryEndDeviceGroup extends EndDeviceGroup {

    String TYPE_IDENTIFIER = "QEG";

    SearchDomain getSearchDomain();

    EndDeviceQueryProvider getEndDeviceQueryProvider();

    SqlFragment toFragment();

    List<SearchablePropertyValue> getSearchablePropertyValues();

    void setConditions(List<SearchablePropertyValue> conditions);

    List<SearchablePropertyCondition> getSearchablePropertyConditions();

}