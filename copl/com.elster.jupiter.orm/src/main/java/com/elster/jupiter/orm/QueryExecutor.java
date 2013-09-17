package com.elster.jupiter.orm;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;
import com.google.common.base.Optional;

import java.util.List;

/**
 * This interface is only intended for use by com.elster.jupiter.domain.util.Query
 */
public interface QueryExecutor<T> {
	<R> void add(DataMapper<R> dataMapper);
    List<T> select(Condition condition, String[] orderBy, boolean eager , String[] exceptions);
    List<T> select(Condition condition, String[] orderBy , boolean eager , String[] exceptions , int from , int to);
	boolean hasField(String fieldName);
	Object convert(String fieldName , String value);
	SqlFragment asFragment(Condition condition, String[] fieldNames);
	Optional<T> get(Object[] key, boolean eager , String[] exceptions);
	Class<?> getType(String fieldName);
    List<String> getQueryFieldNames();
}
