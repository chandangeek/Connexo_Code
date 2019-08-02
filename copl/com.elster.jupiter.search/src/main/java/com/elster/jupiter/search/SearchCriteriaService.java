package com.elster.jupiter.search;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Query;

import java.util.Optional;

@ProviderType
public interface SearchCriteriaService {
    String COMPONENT_NAME = "DYN";
    SearchCriteriaBuilder newSearchCriteria();
    Query<SearchCriteria> getCreationRuleQuery(Class<?>... eagers);
    Optional<SearchCriteria> findSearchCriteriaByUser(String userName);

    @ProviderType
    interface SearchCriteriaBuilder {

        SearchCriteriaBuilder setId(long id);
        SearchCriteriaBuilder setName(String name);
        SearchCriteriaBuilder setUserName(String userName);
        SearchCriteriaBuilder setCriteria(String criteria);
        SearchCriteriaBuilder setDomain(String domain);
        SearchCriteria complete();
        SearchCriteria update();
        SearchCriteria delete();

    }
}
