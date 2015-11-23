package com.energyict.mdc.device.data.impl.search.sqlbuilder;

import com.elster.jupiter.util.sql.SqlBuilder;

interface JoinType {
    void appendTo(SqlBuilder sqlBuilder);
}
