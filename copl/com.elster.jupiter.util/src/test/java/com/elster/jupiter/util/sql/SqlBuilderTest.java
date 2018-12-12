/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.sql;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqlBuilderTest {

    private static final String SQL_FRAGMENT_TEXT = " ? ";
    private static final String SELECT_SQL = "select * from ASSET";
    @Mock
    private SqlFragment sqlFragment;
    @Mock
    private SQLData sqlData;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;

    @Before
    public void setUp() throws SQLException {
        when(sqlFragment.getText()).thenReturn(SQL_FRAGMENT_TEXT);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @After
    public void tearDown() {

    }
    @Test
    public void testCreateEmpty() {
        SqlBuilder sqlBuilder = new SqlBuilder();

        assertThat(sqlBuilder.getText()).isEmpty();
    }

    @Test
    public void testCreateWithBase() {
        SqlBuilder sqlBuilder = new SqlBuilder(SELECT_SQL);

        assertThat(sqlBuilder.getText()).isEqualTo(SELECT_SQL);
    }

    @Test
    public void testAddFragment() {
        SqlBuilder sqlBuilder = new SqlBuilder();

        sqlBuilder.add(sqlFragment);

        assertThat(sqlBuilder.getText()).isEqualTo(SQL_FRAGMENT_TEXT);
    }

    @Test
    public void testAddObject() throws SQLException {
        SqlBuilder sqlBuilder = new SqlBuilder();

        sqlBuilder.addObject(sqlData);

        assertThat(sqlBuilder.getText()).isEqualTo(SQL_FRAGMENT_TEXT);

        sqlBuilder.prepare(connection);

        verify(preparedStatement).setObject(1, sqlData);
    }

    @Test
    public void testAddLong() throws SQLException {
        SqlBuilder sqlBuilder = new SqlBuilder();

        sqlBuilder.addLong(15);

        assertThat(sqlBuilder.getText()).isEqualTo(SQL_FRAGMENT_TEXT);

        sqlBuilder.prepare(connection);

        verify(preparedStatement).setLong(1, 15);

    }

    @Test
    public void testAddInt() throws SQLException {
        SqlBuilder sqlBuilder = new SqlBuilder();

        sqlBuilder.addInt(15);

        assertThat(sqlBuilder.getText()).isEqualTo(SQL_FRAGMENT_TEXT);

        sqlBuilder.prepare(connection);

        verify(preparedStatement).setInt(1, 15);

    }

    @Test
    public void testGetBuffer() {
        SqlBuilder builder = new SqlBuilder("select 1 from dual");
        StringBuilder buffer = builder.getBuffer();
        assertThat(buffer.toString()).isEqualTo("select 1 from dual");
    }

    @Test
    public void testSpace() {
        SqlBuilder builder = new SqlBuilder("select");
        builder.space();
        assertThat(builder.getText()).isEqualTo("select ");
    }

    @Test
    public void testPrepareClosesStatementIfFailed() throws SQLException {
        SqlBuilder builder = new SqlBuilder();
        SqlFragment brokenFragment = mock(SqlFragment.class);
        when(brokenFragment.getText()).thenReturn(SQL_FRAGMENT_TEXT);
        when(brokenFragment.bind(any(PreparedStatement.class), anyInt())).thenThrow(SQLException.class);
        builder.add(brokenFragment);

        try {
            builder.prepare(connection);
        } catch (SQLException e) {
            // intended for test
        }

        verify(preparedStatement).close();
    }

    @Test
    public void testAddString() {
        SqlBuilder sqlBuilder = new SqlBuilder();

        sqlBuilder.append(SELECT_SQL);

        assertThat(sqlBuilder.getText()).isEqualTo(SELECT_SQL);
    }

    @Test
    public void testOpenBracket() {
        SqlBuilder builder = new SqlBuilder("select");
        builder.openBracket();
        assertThat(builder.getText()).isEqualTo("select(");
    }

    @Test
    public void testSpaceOpenBracket() {
        SqlBuilder builder = new SqlBuilder("select");
        builder.spaceOpenBracket();
        assertThat(builder.getText()).isEqualTo("select (");
    }

    @Test
    public void testCloseBracket() {
        SqlBuilder builder = new SqlBuilder("select");
        builder.closeBracket();
        assertThat(builder.getText()).isEqualTo("select)");
    }

    @Test
    public void testCloseBracketSpace() {
        SqlBuilder builder = new SqlBuilder("select");
        builder.closeBracketSpace();
        assertThat(builder.getText()).isEqualTo("select) ");
    }

    @Test
    public void testToString() {
        SqlBuilder builder = new SqlBuilder(SELECT_SQL);
        assertThat(builder.toString()).isEqualTo(SELECT_SQL);

    }

    @Test
    public void testAsPageBuilder() throws SQLException {
        SqlBuilder builder = new SqlBuilder(SELECT_SQL);
        builder = builder.asPageBuilder(400, 500);

        assertThat(builder.getText()).isEqualToIgnoringCase("select * from (select x.*, ROWNUM rnum from (" + SELECT_SQL + ") x where ROWNUM <=  ? ) where rnum >=  ? ");

        builder.prepare(connection);

        verify(preparedStatement).setInt(1, 500);
        verify(preparedStatement).setInt(2, 400);
    }


}
