/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.orm.impl.KeyValue;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

abstract class AbstractChildDataMapper<T> extends JoinDataMapper<T> {
    private final ForeignKeyConstraintImpl constraint;
    private Map<Object, List<T>> targetCache;

    AbstractChildDataMapper(DataMapperImpl<T> dataMapper, ForeignKeyConstraintImpl constraint, String alias) {
        super(dataMapper, alias);
        this.constraint = constraint;
    }

    @Override
    void clearCache() {
        super.clearCache();
        targetCache = new HashMap<>();
    }

    ForeignKeyConstraintImpl getConstraint() {
        return constraint;
    }

    Map<Object, List<T>> getTargetCache() {
        return targetCache;
    }

    @Override
    final T set(Object value, ResultSet rs, int index) throws SQLException {
        if (constraint.getReverseFieldName() != null) {
            addTarget(value);
        }
        KeyValue key = getMapper().getPrimaryKey(rs, index);
        T valueType = null;
        if (key != null) {
            if (key.isEmpty() || (valueType = get(key)) == null) {
                Optional<T> optionalValue = getMapper().construct(rs, index, false);
                if (optionalValue.isPresent()) {
                    valueType = optionalValue.get();
                    put(key, valueType);
                    if (constraint.getReverseFieldName() != null) {
                        addTargetEntry(value, valueType);
                    }
                }
            }
            if (constraint.getFieldName() != null) {
                DomainMapper.FIELDSTRICT.set(valueType, constraint.getFieldName(), value);
            }
        }
        return valueType;
    }

    private void addTarget(Object target) {
        targetCache.computeIfAbsent(target, k -> new ArrayList<>());
    }

    private void addTargetEntry(Object target, T value) {
        List<T> values = targetCache.get(target);
        values.add(value);
    }

    @Override
    final boolean appendFromClause(SqlBuilder builder, String parentAlias, boolean isMarked, boolean forceOuterJoin) {
        builder.append(" LEFT JOIN ");
        appendTable(builder);
        builder.append(" ON ");
        builder.openBracket();
        List<ColumnImpl> primaryKeyColumns = constraint.getReferencedTable().getPrimaryKeyColumns();
        List<ColumnImpl> foreignKeyColumns = constraint.getColumns();
        String separator = "";
        for (int i = 0; i < primaryKeyColumns.size(); i++) {
            builder.append(separator);
            builder.append(primaryKeyColumns.get(i).getName(parentAlias));
            builder.append(" = ");
            builder.append(foreignKeyColumns.get(i).getName(getAlias()));
            separator = " AND ";
        }
        builder.closeBracketSpace();
        return true;
    }

    @Override
    final String getName() {
        return constraint.getReverseFieldName();
    }
}
