/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Range;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryStringifierTest {

    @Test
    public void testTypes() {
        SqlBuilder sqlBuilder = new SqlBuilder("select ID from ABC where TS1 = ");
        sqlBuilder.addTimestamp(Instant.EPOCH.plusSeconds(5));
        sqlBuilder.append(" and TS2 = ");
        sqlBuilder.addTimestamp(Date.from(Instant.EPOCH.plusSeconds(123)));
        sqlBuilder.append(" and LL = ");
        sqlBuilder.addLong(345L);
        sqlBuilder.append(" and II = ");
        sqlBuilder.addInt(543);
        sqlBuilder.append(" and DD1 = ");
        sqlBuilder.addDate(Instant.EPOCH.plusSeconds(8));
        sqlBuilder.append(" and DD2 = ");
        sqlBuilder.addDate(Date.from(Instant.EPOCH.plusSeconds(321)));
        sqlBuilder.append(" and NN is ");
        sqlBuilder.addNull(Types.VARCHAR);
        sqlBuilder.append(" and SS = ");
        sqlBuilder.addObject("some'String");
        sqlBuilder.add("RR", Range.closedOpen(Instant.EPOCH.plusSeconds(2), Instant.EPOCH.plusSeconds(3)), "and");
        sqlBuilder.append(" and ID ");
        sqlBuilder.addInClauseForIdList(Arrays.asList(Id.values()));
        SqlBuilder paginatedQueryBuilder = sqlBuilder.asPageBuilder(1, 2, "ID");

        try (QueryStringifier queryStringifier = new QueryStringifier(paginatedQueryBuilder)) {
            assertThat(queryStringifier.getQuery()).isEqualToIgnoringCase(
                    "select ID from (select x.*, ROWNUM rnum from (select ID from ABC " +
                            "where TS1 =  '1970-01-01 04:00:05.0'  " +
                            "and TS2 =  '1970-01-01 04:02:03.0'  " +
                            "and LL =  345  " +
                            "and II =  543  " +
                            "and DD1 =  '1970-01-01'  " +
                            "and DD2 =  '1970-01-01'  " +
                            "and NN is  null  " +
                            "and SS =  'some''String'  " +
                            "and RR >= 2000  " +
                            "and RR < 3000  " +
                            "and ID  in ( 1 , 13 )" +
                            ") x where ROWNUM <=  2 ) where rnum >=  1 ");
        }
    }

    @Test
    public void testObject() {
        SqlBuilder sqlBuilder = new SqlBuilder("select ID from ABC where TS1 = ");
        sqlBuilder.addObject(new Timestamp(Instant.EPOCH.plusSeconds(5).toEpochMilli()));
        sqlBuilder.append(" and TS2 = ");
        sqlBuilder.addObject(Date.from(Instant.EPOCH.plusSeconds(123)));
        sqlBuilder.append(" and LL = ");
        sqlBuilder.addObject(345L);
        sqlBuilder.append(" and II = ");
        sqlBuilder.addObject(543);
        sqlBuilder.append(" and SH = ");
        sqlBuilder.addObject((short) 434);
        sqlBuilder.append(" and BB = ");
        sqlBuilder.addObject((byte) 2);
        sqlBuilder.append(" and DO = ");
        sqlBuilder.addObject(2.2D);
        sqlBuilder.append(" and FL = ");
        sqlBuilder.addObject(1.1F);
        sqlBuilder.append(" and BD = ");
        sqlBuilder.addObject(new BigDecimal("7654321.1"));
        sqlBuilder.append(" and BI = ");
        sqlBuilder.addObject(new BigInteger("1234567"));
        sqlBuilder.append(" and BY = ");
        sqlBuilder.addObject("abc23".getBytes(StandardCharsets.UTF_8));
        sqlBuilder.append(" and DD1 = ");
        sqlBuilder.addObject(Instant.EPOCH.plusSeconds(8));
        sqlBuilder.append(" and DD2 = ");
        sqlBuilder.addObject(new java.sql.Date(Instant.EPOCH.plusSeconds(321).toEpochMilli()));
        sqlBuilder.append(" and NN is ");
        sqlBuilder.addObject(null);
        sqlBuilder.append(" and SS = ");
        sqlBuilder.addObject("some'Stri'ng");
        sqlBuilder.append(" and BL = ");
        sqlBuilder.addObject(SimpleBlob.fromString("blob'string"));
        sqlBuilder.append(" and IS = ");
        sqlBuilder.addObject(new ByteArrayInputStream("streamed'string".getBytes()));
        SqlBuilder paginatedQueryBuilder = sqlBuilder.asPageBuilder(1, 2, "ID");

        try (QueryStringifier queryStringifier = new QueryStringifier(paginatedQueryBuilder)) {
            assertThat(queryStringifier.getQuery()).isEqualToIgnoringCase(
                    "select ID from (select x.*, ROWNUM rnum from (select ID from ABC " +
                            "where TS1 =  '1970-01-01 04:00:05.0'  " +
                            "and TS2 =  '1970-01-01 04:02:03.0'  " +
                            "and LL =  345  " +
                            "and II =  543  " +
                            "and SH =  434  " +
                            "and BB =  2  " +
                            "and DO =  2.2  " +
                            "and FL =  1.1  " +
                            "and BD =  7654321.1  " +
                            "and BI =  1234567  " +
                            "and BY =  'abc23'  " +
                            "and DD1 =  '1970-01-01 04:00:08.0'  " +
                            "and DD2 =  '1970-01-01'  " +
                            "and NN is  null  " +
                            "and SS =  'some''Stri''ng'  " +
                            "and BL =  'blob''string'  " +
                            "and IS =  'streamed''string' " +
                            ") x where ROWNUM <=  2 ) where rnum >=  1 ");
        }
    }

    private enum Id implements HasId {
        ID1(1),
        ID13(13);

        private final long id;

        Id(long id) {
            this.id = id;
        }

        @Override
        public long getId() {
            return id;
        }
    }
}
