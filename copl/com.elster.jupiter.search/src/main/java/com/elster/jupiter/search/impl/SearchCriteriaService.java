package com.elster.jupiter.search.impl;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Query;

@ProviderType
public interface SearchCriteriaService {
    String COMPONENT_NAME = "DYN";
    SearchCriteriaBuilder newSearchCriteria();
    Query<SearchCriteria> getCreationRuleQuery(Class<?>... eagers);

    @ProviderType
    interface SearchCriteriaBuilder {

        SearchCriteriaBuilder setName(String name);
        SearchCriteriaBuilder setUserName(String userName);
        SearchCriteriaBuilder setCriteria(String criteria);
        SearchCriteriaBuilder setDomain(String domain);
        SearchCriteria complete();

    }
}
