/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.StringJoiner;

/**
 * Helper class to build an IN clause. Oracle doesn't accept IN clause with more than 1000 elements,
 * so in such a case the IN clause should be split in multiple IN clauses joined by OR.
 */
public class INClauseBuilder {

    public static final int IN_CLAUSE_MAX_ELEMENTS = 1000;

    /**
     * Adds an IN clause to the received sqlBuilder for the received column parameter. If the list of IDs has more than 1000 items,
     * then the clause is split into several IN clauses, each with at most 1000 elements.
     * Example for a split in just 3 elements:
     * (cte.device IN (1, 2, 3) OR cte.device IN (4, 5, 6) OR cte.device IN (7, 8))
     * @param ids list of IDs
     * @param column the name of the table column to apply the IN clause for
     * @param sqlBuilder the SqlBuilder object that will be updated
     */
    public static void build(List<Long> ids, String column, SqlBuilder sqlBuilder) {
        List<List<Long>> subSets = Lists.partition(ids, IN_CLAUSE_MAX_ELEMENTS);

        StringJoiner subSetJoiner = new StringJoiner(" OR " + column + " IN ", "(" + column + " IN ", ")");
        StringBuilder outerBuilder = new StringBuilder();
        subSets.forEach(subSet -> {
            StringBuilder innerBuilder = new StringBuilder();
            buildInnerList(innerBuilder, subSet);
            subSetJoiner.add(innerBuilder.toString());
        });
        outerBuilder.append(subSetJoiner.toString());
        sqlBuilder.append(outerBuilder.toString());
    }

    private static void buildInnerList(StringBuilder builder, List<Long> subSet) {
        StringJoiner idJoiner = new StringJoiner(", ", "(", ")");
        subSet.forEach(id -> idJoiner.add(id + ""));
        builder.append(idJoiner.toString());
    }

}
