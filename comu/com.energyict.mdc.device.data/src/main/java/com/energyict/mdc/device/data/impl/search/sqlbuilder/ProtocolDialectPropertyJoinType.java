package com.energyict.mdc.device.data.impl.search.sqlbuilder;

import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.search.JoinClauseBuilder;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectPropertyRelationAttributeTypeNames;

import java.time.Instant;

public class ProtocolDialectPropertyJoinType implements JoinType {

    private final long deviceProtocolId;
    private final String relationTableName;

    public ProtocolDialectPropertyJoinType(long deviceProtocolId, String relationTableName) {
        this.deviceProtocolId = deviceProtocolId;
        this.relationTableName = relationTableName;
    }

    @Override
    public void appendTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" left join "); // left join to support a search by device configuration properties
        sqlBuilder.append(TableSpecs.DDC_PROTOCOLDIALECTPROPS.name());
        sqlBuilder.append(" " + JoinClauseBuilder.Aliases.PROTOCOL_DIALECT_PROPS + " on " + JoinClauseBuilder.Aliases.PROTOCOL_DIALECT_PROPS
                + ".DEVICEID = "+ JoinClauseBuilder.Aliases.DEVICE + ".id and " + JoinClauseBuilder.Aliases.PROTOCOL_DIALECT_PROPS + ".DEVICEPROTOCOLID =");
        sqlBuilder.addLong(this.deviceProtocolId);
        sqlBuilder.append(" left join "); // left join to support a search by device configuration properties
        sqlBuilder.append(this.relationTableName);
        sqlBuilder.append(" druprops on druprops.");
        sqlBuilder.append(DeviceProtocolDialectPropertyRelationAttributeTypeNames.DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME);
        sqlBuilder.append(" = " + JoinClauseBuilder.Aliases.PROTOCOL_DIALECT_PROPS + ".id");
        sqlBuilder.append(" and (druprops.todate is null or druprops.todate >");
        sqlBuilder.addLong(Instant.now().getEpochSecond());
        sqlBuilder.closeBracketSpace();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProtocolDialectPropertyJoinType that = (ProtocolDialectPropertyJoinType) o;
        return this.deviceProtocolId == that.deviceProtocolId;

    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.deviceProtocolId);
    }

}
