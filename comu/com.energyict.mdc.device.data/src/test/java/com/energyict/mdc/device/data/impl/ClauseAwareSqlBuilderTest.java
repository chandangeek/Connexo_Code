package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.sql.SqlBuilder;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link ClauseAwareSqlBuilder} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-01 (09:13)
 */
public class ClauseAwareSqlBuilderTest {

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

    private ClauseAwareSqlBuilder newSqlBuilder() {
    	ClauseAwareSqlBuilder result = ClauseAwareSqlBuilder.select("*");
    	result.append(" from dual");
    	return result;
    }

}