package com.energyict.mdc.device.data.impl.search.sqlbuilder;

import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.search.JoinClauseBuilder;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectPropertyRelationAttributeTypeNames;

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
        sqlBuilder.append("  on " + this.relationTableName + ".");
        sqlBuilder.append(DeviceProtocolDialectPropertyRelationAttributeTypeNames.DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME);
        sqlBuilder.append(" = " + JoinClauseBuilder.Aliases.PROTOCOL_DIALECT_PROPS  + this.deviceProtocolId + ".id");
        sqlBuilder.append(" and (" + this.relationTableName + ".todate is null or " + this.relationTableName + ".todate >");
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
