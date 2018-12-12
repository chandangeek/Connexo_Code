/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search.sqlbuilder;

import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.search.JoinClauseBuilder;

public class ProtocolDialectPropertyJoinType implements JoinType {

    private final long deviceProtocolId;

    public ProtocolDialectPropertyJoinType(long deviceProtocolId) {
        this.deviceProtocolId = deviceProtocolId;
    }

    private String alias(){
        return JoinClauseBuilder.Aliases.PROTOCOL_DIALECT_PROPS + deviceProtocolId;
    }

    @Override
    public void appendTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" left join "); // left join to support a search by device configuration properties
        sqlBuilder.append(TableSpecs.DDC_PROTOCOLDIALECTPROPS.name());
        sqlBuilder.append(" " + alias() + " on " + alias()
                + ".DEVICEID = "+ JoinClauseBuilder.Aliases.DEVICE + ".id and " + alias() + ".DEVICEPROTOCOLID =");
        sqlBuilder.addLong(this.deviceProtocolId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProtocolDialectPropertyJoinType that = (ProtocolDialectPropertyJoinType) o;

        return deviceProtocolId == that.deviceProtocolId;

    }

    @Override
    public int hashCode() {
        return Long.hashCode(deviceProtocolId);
    }
}
