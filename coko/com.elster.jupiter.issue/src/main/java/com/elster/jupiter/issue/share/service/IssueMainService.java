package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;

public interface IssueMainService {
    public <T extends Entity> Optional<T> get(Class<T> clazz, Object... key);
    public <T extends Entity> Optional<T> delete(Class<T> clazz, Object... key);
    public <T extends Entity> Optional<T> delete(T entity);
    public <T extends Entity> Optional<T> save(T entity);
    public <T extends Entity> Optional<T> update(T entity);
    public <T extends Entity> Optional<T> searchFirst(T entity);
    public <T extends Entity> Condition toCondition(T entity);
    public <T extends Entity> Query<T> query(Class<T> clazz, Class<?> ... eagers);
}
