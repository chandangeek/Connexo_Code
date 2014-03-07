package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.database.NonSearchable;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.service.IssueMainService;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.issue.main", service = IssueMainService.class)
public class IssueMainServiceImpl implements IssueMainService{
    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile IssueMappingService issueMappingService;

    public IssueMainServiceImpl() {
    }

    @Inject
    public IssueMainServiceImpl(QueryService queryService, IssueMappingService issueMappingService) {
        setQueryService(queryService);
        setIssueMappingService(issueMappingService);
    }

    @Override
    public <T extends Entity> Optional<T> get(Class<T> clazz, long id) {
        return queryService.wrap(dataModel.query(clazz)).get(id);
    }

    @Override
    public <T extends Entity> Optional<T> delete(Class<T> clazz, long id) {
        Optional<T> entity = get(clazz, id);
        if (entity.isPresent()){
            return delete(entity.get());
        }
        return Optional.absent();
    }

    @Override
    public <T extends Entity> Optional<T> delete(T entity) {
        if (entity != null){
            Class<T> clazz = (Class<T>) entity.getClass();
            dataModel.mapper(clazz).remove(entity);
            return Optional.of(entity);
        }
        return Optional.absent();
    }

    @Override
    public <T extends Entity> Optional<T> save(T entity) {
        if (entity != null){
            Class<T> clazz = (Class<T>) entity.getClass();
            dataModel.mapper(clazz).persist(entity);
            return Optional.of(entity);
        }
        return Optional.absent();
    }

    @Override
    public <T extends Entity> Optional<T> update(T entity) {
        if (entity != null){
            Class<T> clazz = (Class<T>) entity.getClass();
            dataModel.mapper(clazz).update(entity);
            return Optional.of(entity);
        }
        return Optional.absent();
    }

    @Override
    public <T extends Entity> Optional<T> searchFirst(T entity) {
        Class<T> clazz = (Class<T>) entity.getClass();
        Query<T> query = query(clazz);
        Condition condition = buildConditionForSearch(clazz, entity);
        List<T> searchList = query.select(condition, 1, 1);
        if (searchList != null && searchList.size() == 1){
            return Optional.of(searchList.get(0));
        }
        return Optional.absent();
    }

    @Override
    public <T extends Entity> List<T> search(T entity) {
        Class<T> clazz = (Class<T>) entity.getClass();
        Query<T> query = query(clazz);
        Condition condition = buildConditionForSearch(clazz, entity);
        List<T> searchList = query.select(condition);
        return searchList;
    }

    private <T extends Entity> Condition buildConditionForSearch(Class<T> clazz, T entity){
        Class<?> current = clazz;
        Condition condition = Condition.TRUE;
        do {
            for ( Field field : current.getDeclaredFields() ) {
                if (!field.isAnnotationPresent(NonSearchable.class)) {
                    condition = getConditionForField(entity, field, condition);
                }
            }
            current = current.getSuperclass();
        } while ( current != null );
        return condition;
    }

    private <T extends Entity> Condition getConditionForField(T entity, Field field, Condition condition){
        field.setAccessible(true);
        try {
            Object value = field.get(entity);
            if (value != null){
                Class<?> fieldType = field.getType();
                if (fieldType.isAssignableFrom(com.elster.jupiter.orm.associations.Reference.class)){
                    // TODO some staf for references
                } else if (fieldType.isAssignableFrom(String.class)){
                    condition = condition.and(where(field.getName()).isEqualToIgnoreCase(value));
                } else {
                    condition = condition.and(where(field.getName()).isEqualTo(value));
                }
            }
        } catch (IllegalAccessException e) {
        }
        return condition;
    }

    @Override
    public <T extends Entity> Query<T> query(Class<T> clazz, Class<?> ... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }
    @Reference
    public void setIssueMappingService(IssueMappingService issueMappingService) {
        this.issueMappingService = issueMappingService;
        this.dataModel = IssueMappingServiceImpl.class.cast(issueMappingService).getDataModel();
    }
}
