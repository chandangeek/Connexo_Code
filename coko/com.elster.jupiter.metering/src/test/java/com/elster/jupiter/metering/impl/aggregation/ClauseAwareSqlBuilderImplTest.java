/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link ClauseAwareSqlBuilderImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (15:33)
 */
public class ClauseAwareSqlBuilderImplTest {

    @Test
    public void testSelectOnly() {
        ClauseAwareSqlBuilderImpl sqlBuilder = testInstance();

        // Business methods
        sqlBuilder.select().append("sysdate from dual");
        String sql = sqlBuilder.finish().getText();

        // Asserts: equals ignoring whitespace does not treat \n as whitespace so replace it with space
        sql = sql.replace("\n", " ");
        assertThat(sql).isEqualToIgnoringWhitespace("SELECT sysdate from dual");
    }

    @Test
    public void testOneWithClauseWithoutColumnsAndComment() {
        ClauseAwareSqlBuilderImpl sqlBuilder = testInstance();

        // Business methods
        sqlBuilder.with("w1", Optional.empty()).append("SELECT 1, 2 from dual");
        sqlBuilder.select().append("w1.value from w1");
        String sql = sqlBuilder.finish().getText();

        // Asserts: equals ignoring whitespace does not treat \n as whitespace so replace it with space
        sql = sql.replace("\n", " ");
        assertThat(sql).isEqualToIgnoringWhitespace("WITH w1 AS ( SELECT 1, 2 from dual) SELECT w1.value from w1");
    }

    @Test
    public void testOneWithClauseWithColumnsAndComment() {
        ClauseAwareSqlBuilderImpl sqlBuilder = testInstance();

        // Business methods
        sqlBuilder.with("w1", Optional.of("silly comment"), "id", "value").append("SELECT 1, 2 from dual");
        sqlBuilder.select().append("w1.value from w1");
        String sql = sqlBuilder.finish().getText();

        // Asserts: equals ignoring whitespace does not treat \n as whitespace so replace it with space
        sql = sql.replace("\n", " ");
        assertThat(sql).isEqualToIgnoringWhitespace("WITH w1(id, value) AS (/* silly comment */ SELECT 1, 2 from dual) SELECT w1.value from w1");
    }

    @Test
    public void testOrderlyCalls() {
        ClauseAwareSqlBuilderImpl sqlBuilder = testInstance();

        // Business methods
        sqlBuilder.with("w1", Optional.of("dataset1"), "id", "value").append("SELECT 1, 2 from dual");
        sqlBuilder.with("w2", Optional.of("dataset2"), "id", "value").append("SELECT 1, 20 from dual");
        sqlBuilder.select().append("w1.value - w2.value from w1 join w2 on w2.id = w1.id");
        sqlBuilder.select().append("w1.value * w2.value from w1 join w2 on w2.id = w1.id");
        String sql = sqlBuilder.finish().getText();

        // Asserts: equals ignoring whitespace does not treat \n as whitespace so replace it with space
        sql = sql.replace("\n", " ");
        assertThat(sql).isEqualToIgnoringWhitespace("WITH w1(id, value) AS (/* dataset1 */ SELECT 1, 2 from dual), w2(id, value) AS (/* dataset2 */ SELECT 1, 20 from dual) SELECT w1.value - w2.value from w1 join w2 on w2.id = w1.id UNION ALL SELECT w1.value * w2.value from w1 join w2 on w2.id = w1.id");
    }

    @Test
    public void testDisorderlyCalls() {
        ClauseAwareSqlBuilderImpl sqlBuilder = testInstance();

        // Business methods
        sqlBuilder.select().append("w1.value - w2.value from w1 join w2 on w2.id = w1.id");
        sqlBuilder.with("w1", Optional.of("dataset1"), "id", "value").append("SELECT 1, 2 from dual");
        sqlBuilder.with("w2", Optional.of("dataset2"), "id", "value").append("SELECT 1, 20 from dual");
        sqlBuilder.select().append("w1.value * w2.value from w1 join w2 on w2.id = w1.id");
        String sql = sqlBuilder.finish().getText();

        // Asserts: equals ignoring whitespace does not treat \n as whitespace so replace it with space
        sql = sql.replace("\n", " ");
        assertThat(sql).isEqualToIgnoringWhitespace("WITH w1(id, value) AS (/* dataset1 */ SELECT 1, 2 from dual), w2(id, value) AS (/* dataset2 */ SELECT 1, 20 from dual) SELECT w1.value - w2.value from w1 join w2 on w2.id = w1.id UNION ALL SELECT w1.value * w2.value from w1 join w2 on w2.id = w1.id");
    }

    @Test
    public void withClauseDoesNotExist() {
        ClauseAwareSqlBuilderImpl sqlBuilder = testInstance();

        // Business method && asserts
        assertThat(sqlBuilder.withExists("no")).isFalse();
    }

    @Test
    public void withClauseExists() {
        ClauseAwareSqlBuilderImpl sqlBuilder = testInstance();
        sqlBuilder.with("yes", Optional.empty());

        // Business method && asserts
        assertThat(sqlBuilder.withExists("yes")).isTrue();
    }

    private ClauseAwareSqlBuilderImpl testInstance() {
        return new ClauseAwareSqlBuilderImpl();
    }

}