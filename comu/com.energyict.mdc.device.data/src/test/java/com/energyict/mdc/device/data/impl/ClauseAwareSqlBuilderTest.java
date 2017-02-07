/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.time.Instant;
import java.util.EnumSet;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link ClauseAwareSqlBuilder} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-01 (09:13)
 */
public class ClauseAwareSqlBuilderTest {

    @Test
    public void testOnlyOneWith() {
        ClauseAwareSqlBuilder sqlBuilder = ClauseAwareSqlBuilder.with("select sysdate now from dual", "time");
        sqlBuilder.append("select * from dual where time.now = sysdate");

        // Asserts
        assertThat(sqlBuilder.getText()).startsWith("with");
        assertThat(sqlBuilder.getText()).doesNotContain(",");
        assertThat(sqlBuilder.getText()).endsWith("where time.now = sysdate");
    }

    @Test
    public void testTwoWiths() {
        ClauseAwareSqlBuilder sqlBuilder = ClauseAwareSqlBuilder.with("select sysdate now from dual", "time");
        sqlBuilder.appendWith("select sequence.nextval id", "next");
        sqlBuilder.append("select next.id + now from time, next");

        // Asserts
        assertThat(sqlBuilder.getText()).startsWith("with");
        assertThat(sqlBuilder.getText()).contains(",");
        assertThat(sqlBuilder.getText()).endsWith("from time, next");
    }

    @Test
    public void testMultipleWiths() {
        ClauseAwareSqlBuilder sqlBuilder = ClauseAwareSqlBuilder.with("select sysdate now from dual", "time");
        sqlBuilder.appendWith("select sequence.nextval id", "next");
        sqlBuilder.appendWith("select 'something' text from dual", "something");
        sqlBuilder.append("select next.id + now + something.text from time, next, something");

        // Asserts
        assertThat(sqlBuilder.getText()).startsWith("with");
        assertThat(sqlBuilder.getText()).contains(",");
        assertThat(sqlBuilder.getText()).endsWith("from time, next, something");
    }

    @Test
    public void testMultipleWithsAndMultipleWhere() {
        ClauseAwareSqlBuilder sqlBuilder = ClauseAwareSqlBuilder.with("select sysdate now from dual", "time");
        sqlBuilder.appendWith("select sequence.nextval id", "next");
        sqlBuilder.append("select next.id + now from time, next");
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append("next.id > 1");
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append("time = sysdate");

        // Asserts
        assertThat(sqlBuilder.getText()).startsWith("with");
        assertThat(sqlBuilder.getText()).contains(",");
        assertThat(sqlBuilder.getText()).endsWith("and time = sysdate");
    }

    @Test(expected = IllegalStateException.class)
    public void testWithAfterWhere() {
        ClauseAwareSqlBuilder sqlBuilder = this.newSqlBuilder();
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append("1 = 1");
        sqlBuilder.appendWith("select sysdate now from dual", "time");

        // Asserts: see expected exception rule
    }

    @Test
    public void testWhere () {
        ClauseAwareSqlBuilder sqlBuilder = this.newSqlBuilder();

        // Business method
        sqlBuilder.appendWhereOrAnd();

        // Asserts
        assertThat(sqlBuilder.getText()).endsWith("where ");
    }

    @Test
    public void testAnd () {
        ClauseAwareSqlBuilder sqlBuilder = this.newSqlBuilder();
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append("1 = 1");

        // Business method
        sqlBuilder.appendWhereOrAnd();

        // Asserts
        assertThat(sqlBuilder.getText()).endsWith("and ");
    }

    @Test
    public void testOr () {
        ClauseAwareSqlBuilder sqlBuilder = this.newSqlBuilder();
        sqlBuilder.appendWhereOrOr();
        sqlBuilder.append("1 = 1");

        // Business method
        sqlBuilder.appendWhereOrOr();

        // Asserts
        assertThat(sqlBuilder.getText()).endsWith("or ");
    }

    @Test
    public void testUnionAllWithAnd () {
        ClauseAwareSqlBuilder sqlBuilder = this.newSqlBuilder();
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append("1 = 1");
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append("2 = 2");

        // Business method
        sqlBuilder.unionAll();
        sqlBuilder.append("select * from dual");
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append("3 = 3");

        // Asserts
        assertThat(sqlBuilder.getText()).endsWith("where 3 = 3");
    }

    @Test
    public void testUnionAllWithOr () {
        ClauseAwareSqlBuilder sqlBuilder = this.newSqlBuilder();
        sqlBuilder.appendWhereOrOr();
        sqlBuilder.append("1 = 1");
        sqlBuilder.appendWhereOrOr();
        sqlBuilder.append("2 = 2");

        // Business method
        sqlBuilder.unionAll();
        sqlBuilder.append("select * from dual");
        sqlBuilder.appendWhereOrOr();
        sqlBuilder.append("3 = 3");

        // Asserts
        assertThat(sqlBuilder.getText()).endsWith("where 3 = 3");
    }

    @Test
    public void testWithoutExcludesStates() {
        ClauseAwareSqlBuilder sqlBuilder = ClauseAwareSqlBuilder.withExcludedStates("states", EnumSet.noneOf(DefaultState.class), Instant.EPOCH);

        // Asserts
        String sqlText = sqlBuilder.getText().toLowerCase();
        assertThat(sqlText).startsWith("with");
        assertThat(sqlText).matches(".*fs\\.name\\s*is\\s*not\\s*null.*");
    }

    @Test
    public void testWithOneExcludedStates() {
        ClauseAwareSqlBuilder sqlBuilder = ClauseAwareSqlBuilder.withExcludedStates("states", EnumSet.of(DefaultState.ACTIVE), Instant.EPOCH);

        // Asserts
        String sqlText = sqlBuilder.getText().toLowerCase();
        assertThat(sqlText).startsWith("with");
        assertThat(sqlText).contains("fs.name <> '" + DefaultState.ACTIVE.getKey().toLowerCase() + "'");
    }

    @Test
    public void testWithMultipleExcludedStates() {
        ClauseAwareSqlBuilder sqlBuilder = ClauseAwareSqlBuilder.withExcludedStates("states", EnumSet.of(DefaultState.DECOMMISSIONED, DefaultState.IN_STOCK), Instant.EPOCH);

        // Asserts
        String sqlText = sqlBuilder.getText().toLowerCase();
        assertThat(sqlText).startsWith("with");
        assertThat(sqlText).contains("'" + DefaultState.DECOMMISSIONED.getKey().toLowerCase() + "'");
        assertThat(sqlText).contains("'" + DefaultState.IN_STOCK.getKey().toLowerCase() + "'");
        assertThat(sqlText).matches(".*fs\\.name\\s*not\\s*in\\s*\\(.*\\).*");
    }

    private ClauseAwareSqlBuilder newSqlBuilder() {
    	ClauseAwareSqlBuilder result = ClauseAwareSqlBuilder.select("*");
    	result.append(" from dual");
    	return result;
    }

}