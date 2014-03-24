package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.database.DatabaseAPI;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.service.IssueMainService;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.issue.main", service = IssueMainService.class)
public class IssueMainServiceImpl implements IssueMainService{
    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile DatabaseAPI dbAPI;

    public IssueMainServiceImpl() {
    }

    @Inject
    public IssueMainServiceImpl(QueryService queryService, IssueMappingService issueMappingService) {
        setQueryService(queryService);
        setIssueMappingService(issueMappingService);
        activate();
    }

    @Activate
    public void activate(){
        dbAPI = new DatabaseAPI(dataModel, queryService);
    }

    @Override
    public <T extends Entity> Optional<T> get(Class<T> clazz, Object... key) {
        return dbAPI.get(clazz, key);
    }

    @Override
    public <T extends Entity> Optional<T> delete(Class<T> clazz, Object... key) {
        return dbAPI.delete(clazz, key);
    }

    @Override
    public <T extends Entity> Optional<T> delete(T entity) {
        return dbAPI.delete(entity);
    }

    @Override
    public <T extends Entity> Optional<T> save(T entity) {
        return dbAPI.save(entity);
    }

    @Override
    public <T extends Entity> Optional<T> update(T entity) {
        return dbAPI.update(entity);
    }

    @Override
    public <T extends Entity> Optional<T> searchFirst(T entity) {
        return dbAPI.searchFirst(entity);
    }

    @Override
    public <T extends Entity> Condition toCondition(T entity) {
        return dbAPI.toCondition(entity);
    }

    @Override
    public <T extends Entity> Query<T> query(Class<T> clazz, Class<?> ... eagers) {
          return dbAPI.query(clazz, eagers);
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setIssueMappingService(IssueMappingService issueMappingService) {
        this.dataModel = IssueMappingServiceImpl.class.cast(issueMappingService).getDataModel();
    }
}
