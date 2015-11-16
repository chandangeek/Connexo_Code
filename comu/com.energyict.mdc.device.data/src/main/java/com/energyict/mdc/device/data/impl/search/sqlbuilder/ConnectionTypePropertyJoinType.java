package com.energyict.mdc.device.data.impl.search.sqlbuilder;

import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePropertyRelationAttributeTypeNames;

public class ConnectionTypePropertyJoinType implements JoinType {
    private final ConnectionTypePluggableClass pluggableClass;

    public ConnectionTypePropertyJoinType(ConnectionTypePluggableClass pluggableClass) {
        super();
        this.pluggableClass = pluggableClass;
    }

    @Override
    public void appendTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" join ");
        sqlBuilder.append(this.pluggableClass.findRelationType().getDynamicAttributeTableName());
        sqlBuilder.append(" props on props.");
        sqlBuilder.append(ConnectionTypePropertyRelationAttributeTypeNames.CONNECTION_TASK_ATTRIBUTE_NAME);
        sqlBuilder.append(" = ct.id AND props.TODATE is NULL");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConnectionTypePropertyJoinType that = (ConnectionTypePropertyJoinType) o;
        return pluggableClass.getId() == that.pluggableClass.getId();

    }

    @Override
    public int hashCode() {
        return Long.hashCode(pluggableClass.getId());
    }

}
