package com.elster.jupiter.metering.groups;

import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.sql.SqlFragment;

import java.util.List;

public interface QueryEndDeviceGroup extends EndDeviceGroup {

    String TYPE_IDENTIFIER = "QEG";

    SearchDomain getSearchDomain();

    EndDeviceQueryProvider getEndDeviceQueryProvider();

    SqlFragment toFragment(String... fieldNames);

    List<SearchablePropertyValue> getSearchablePropertyValues();

    void setConditions(List<SearchablePropertyValue> conditions);

    List<SearchablePropertyCondition> getSearchablePropertyConditions();

}