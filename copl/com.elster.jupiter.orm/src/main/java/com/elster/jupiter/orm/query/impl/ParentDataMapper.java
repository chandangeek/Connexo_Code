/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.orm.impl.KeyValue;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

class ParentDataMapper<T> extends JoinDataMapper<T> {
    private ForeignKeyConstraintImpl constraint;

    ParentDataMapper(DataMapperImpl<T> dataMapper, ForeignKeyConstraintImpl constraint, String alias) {
        super(dataMapper, alias);
        this.constraint = constraint;
    }

    T set(Object target, ResultSet rs, int index) throws SQLException {
        T value = null;
        if (target != null) {
            KeyValue key = getMapper().getPrimaryKey(rs, index);
            if (key != null) {
                if (key.isEmpty() || (value = get(key)) == null) {
                    Optional<T> optionalValue = getMapper().construct(rs, index, false);
                    if (optionalValue.isPresent()) {
                        value = optionalValue.get();
                        put(key, value);
                    }
                }
                if (constraint.getFieldName() != null && !getTable().isCached()) {
                    DomainMapper.FIELDSTRICT.set(target, constraint.getFieldName(), value);
                }
            }
        }
        return value;
    }

    @Override
    boolean appendFromClause(SqlBuilder builder, String parentAlias, boolean marked, boolean forceOuterJoin) {
        boolean outerJoin = forceOuterJoin || !constraint.isNotNull();
        if (outerJoin) {
            builder.append(" LEFT");
        }
        builder.append(" JOIN ");
        appendTable(builder);
        builder.append(" ON ");
        builder.openBracket();
        List<ColumnImpl> primaryKeyColumns = constraint.getReferencedTable().getPrimaryKeyColumns();
        List<ColumnImpl> foreignKeyColumns = constraint.getColumns();
        String separator = "";
        for (int i = 0; i < primaryKeyColumns.size(); i++) {
            builder.append(separator);
            builder.append(foreignKeyColumns.get(i).getName(parentAlias));
            builder.append(" = ");
            builder.append(primaryKeyColumns.get(i).getName(getAlias()));
            separator = " AND ";
        }
        builder.closeBracketSpace();
        return outerJoin;
    }


    @Override
    String getName() {
        return constraint.getFieldName();
    }

    @Override
    public boolean isReachable() {
        return getTable().getCacheType() == Table.CacheType.NO_CACHE;
    }

    boolean skipFetch(boolean marked, boolean anyChildMarked) {
        return getTable().getCacheType() != Table.CacheType.NO_CACHE;
    }

    @Override
    boolean needsDistinct(boolean marked, boolean anyChildMarked) {
        return false;
    }

}
