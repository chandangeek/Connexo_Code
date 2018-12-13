/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search.sqlbuilder;

import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.impl.search.JoinClauseBuilder;

import java.time.Instant;

public class ProtocolDialectDynamicPropertyJoinType implements JoinType {

    private final String relationTableName;
    private final long deviceProtocolId;

    public ProtocolDialectDynamicPropertyJoinType(long  deviceProtocolId, String relationTableName) {
        this.deviceProtocolId = deviceProtocolId;
        this.relationTableName = relationTableName;
    }

    @Override
    public void appendTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" left join "); // left join to support a search by device configuration properties
        sqlBuilder.append(this.relationTableName);
        sqlBuilder.append("  on " + this.relationTableName + ".DIALECT_PROPS_PROVIDER");
        sqlBuilder.append(" = " + JoinClauseBuilder.Aliases.PROTOCOL_DIALECT_PROPS  + this.deviceProtocolId + ".id");
        sqlBuilder.append(" and (" + this.relationTableName + ".ENDTIME is null or " + this.relationTableName + ".ENDTIME >");
        sqlBuilder.addLong(Instant.now().getEpochSecond());
        sqlBuilder.closeBracketSpace();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProtocolDialectDynamicPropertyJoinType that = (ProtocolDialectDynamicPropertyJoinType) o;

        return !(relationTableName != null ? !relationTableName.equals(that.relationTableName) : that.relationTableName != null);

    }

    @Override
    public int hashCode() {
        return relationTableName != null ? relationTableName.hashCode() : 0;
    }
}
