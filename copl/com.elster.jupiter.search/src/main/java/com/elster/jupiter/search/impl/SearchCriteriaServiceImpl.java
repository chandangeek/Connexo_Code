package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.search.SearchCriteria;
import com.elster.jupiter.search.SearchCriteriaService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;


@Component(name = "com.elster.jupiter.search.impl",
        service = {SearchCriteriaService.class, MessageSeedProvider.class},
        property = {"name=" + SearchCriteriaService.COMPONENT_NAME,
             },
        immediate = true)
public class SearchCriteriaServiceImpl implements SearchCriteriaService, MessageSeedProvider {

    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;

    public SearchCriteriaServiceImpl(){

    }

    @Inject
    public SearchCriteriaServiceImpl(OrmService ormService, QueryService queryService, NlsService nlsService) {
        setQueryService(queryService);
        setOrmService(ormService);
        setNlsService(nlsService);
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
                bind(NlsService.class).toInstance(nlsService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(QueryService.class).toInstance(queryService);
                bind(SearchCriteriaService.class).to(SearchCriteriaServiceImpl.class).in(Scopes.SINGLETON);
                bind(MessageInterpolator.class).toInstance(thesaurus);
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
        //query.setRestriction(where("obsoleteTime").isNull());
        return query;
    }

    @Override
    public Optional<SearchCriteria> findSearchCriteriaByUser(String userName) {
        return dataModel.mapper(SearchCriteria.class).getOptional(userName);
    }

    private <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(SearchCriteriaService.COMPONENT_NAME, "search_criteria");
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(SearchCriteriaService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }
}
