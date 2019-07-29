package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.search.impl",
        service = {SearchCriteriaService.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        property = {"name=" + SearchCriteriaService.COMPONENT_NAME,
             },
        immediate = true)
public class SearchCriteriaServiceImpl implements SearchCriteriaService {

    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile Thesaurus thesaurus;
    @Inject
    public SearchCriteriaServiceImpl(DataModel dataModel, QueryService queryService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.queryService = queryService;
        this.thesaurus = thesaurus;
        activate();
    }

    @Activate
    public void activate() {
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {

                bind(Thesaurus.class).toInstance(thesaurus);
                bind(QueryService.class).toInstance(queryService);
                bind(SearchCriteriaService.class).to(SearchCriteriaServiceImpl.class).in(Scopes.SINGLETON);

            }
        });
        // issueCreationService = dataModel.getInstance(IssueCreationService.class);
    }

    @Override
    public SearchCriteriaBuilder newSearchCriteria() {
        // return new CreationRuleBuilderImpl(dataModel, dataModel.getInstance(CreationRuleImpl.class));
        return new SearchCriteriaBuilderImpl(dataModel, dataModel.getInstance(SearchCriteriaImpl.class));
    }

    @Override
    public Query<SearchCriteria> getCreationRuleQuery(Class<?>... eagers) {
        Query<SearchCriteria> query = query(SearchCriteria.class, eagers);
        query.setRestriction(where("obsoleteTime").isNull());
        return query;
    }

    private <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }
}
