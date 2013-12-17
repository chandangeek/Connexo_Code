package com.elster.jupiter.bootstrap.h2.impl;

import org.h2.jdbcx.JdbcDataSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public enum InMemoryPersistence {
    ;

    private static final String url = "jdbc:h2:mem:DB;MVCC=TRUE;lock_timeout=5000";

    public static String query(String sql) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(url);
        dataSource.setUser("sa");
        dataSource.setPassword("");

        try (Connection connection = dataSource.getConnection()) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new ResultSetPrinter(new PrintStream(out)).print(resultSet);
            return new String(out.toByteArray());
        }
        } catch (SQLException e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            return stringWriter.toString();
        }
    }


}
