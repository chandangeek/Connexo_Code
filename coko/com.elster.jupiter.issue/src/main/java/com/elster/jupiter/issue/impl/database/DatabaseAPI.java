package com.elster.jupiter.issue.impl.database;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;

import java.lang.reflect.Field;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class DatabaseAPI {
    private DataModel dataModel;
    private QueryService queryService;

    public DatabaseAPI(DataModel dataModel, QueryService queryService) {
        this.dataModel = dataModel;
        this.queryService = queryService;
    }

    public <T extends Entity> Optional<T> get(Class<T> clazz, Object... key) {
        return queryService.wrap(dataModel.query(clazz)).get(key);
    }

    public <T extends Entity> Optional<T> delete(Class<T> clazz, Object... key) {
        Optional<T> entity = get(clazz, key);
        if (entity.isPresent()){
            return delete(entity.get());
        }
        return Optional.absent();
    }

    public <T extends Entity> Optional<T> delete(T entity) {
        if (entity != null){
            Class<T> clazz = (Class<T>) entity.getClass();
            dataModel.mapper(clazz).remove(entity);
            return Optional.of(entity);
        }
        return Optional.absent();
    }

    public <T extends Entity> Optional<T> save(T entity) {
        if (entity != null){
            Class<T> clazz = (Class<T>) entity.getClass();
            dataModel.mapper(clazz).persist(entity);
            return Optional.of(entity);
        }
        return Optional.absent();
    }

    public <T extends Entity> Optional<T> update(T entity) {
        if (entity != null){
            Class<T> clazz = (Class<T>) entity.getClass();
            dataModel.mapper(clazz).update(entity);
            return Optional.of(entity);
        }
        return Optional.absent();
    }

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

    public <T extends Entity> Condition toCondition(T entity) {
        Class<T> clazz = (Class<T>) entity.getClass();
        return buildConditionForSearch(clazz, entity);
    }


    public <T extends Entity> Query<T> query(Class<T> clazz, Class<?> ... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }

    private <T extends Entity> Condition buildConditionForSearch(Class<T> clazz, T entity){
        Class<?> current = clazz;
        Condition condition = Condition.TRUE;
        do {
            for ( Field field : current.getDeclaredFields() ) {
//                if (!field.isAnnotationPresent(NonSearchable.class)) {
                    condition = getConditionForField(entity, field, condition);
//                }
            }
            current = current.getSuperclass();
        } while ( current != null );
        return condition;
    }

    //TODO try to handle default values for primitive types
    private <T extends Entity> Condition getConditionForField(T entity, Field field, Condition condition){
        field.setAccessible(true);
        try {
            Object value = field.get(entity);
            if (value != null){
                Class<?> fieldType = field.getType();
                if (fieldType.isAssignableFrom(com.elster.jupiter.orm.associations.Reference.class)){
                    condition = getConditionForReference(field.getName(), value, condition);
                } else if (fieldType.isAssignableFrom(String.class)){
                    condition = condition.and(where(field.getName()).isEqualToIgnoreCase(value));
                } else {
                    condition = condition.and(where(field.getName()).isEqualTo(value));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to build condition for field", e);
        }
        return condition;
    }

    private Condition getConditionForReference(String originalFieldName, Object value, Condition condition) throws IllegalAccessException{
        Field refField = getField(value.getClass(), "value");
        if (refField != null) {
            refField.setAccessible(true);
            value = refField.get(value);
            if (value != null) {
                condition = condition.and(where(originalFieldName).isEqualTo(value));
            }
        }
        return condition;
    }

    private Field getField(Class clazz, String name){
        for (Field field : clazz.getDeclaredFields() ) {
            if (field.getName().equals(name)){
                return field;
            }
        }
        return null;
    }
}
