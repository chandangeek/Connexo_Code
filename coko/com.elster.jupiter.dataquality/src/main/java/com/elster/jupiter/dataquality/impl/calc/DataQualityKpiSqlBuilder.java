/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

interface DataQualityKpiSqlBuilder {

    enum ResultSetColumn {
        CHANNELID(1),
        READINGTIMESTAMP(2),
        READINGQUALITYTYPE(3),
        NOTSUSPECTFLAG(4),
        COUNTER(5);

        private int index;

        ResultSetColumn(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }

    DataQualityKpiSqlBuilder init(Instant start, Instant end);

    PreparedStatement prepare(Connection connection) throws SQLException;

}
