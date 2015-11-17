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
import java.util.Arrays;

public enum InMemoryPersistence {
    ;

    private static final String url = "jdbc:h2:mem:DB;MVCC=TRUE;lock_timeout=5000;MODE=Oracle";

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
            return tablerized(new String(out.toByteArray()));
        }
        } catch (SQLException e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            return stringWriter.toString();
        }
    }

    static String tablerized(String tabled) {
        String[] rows = tabled.split("\n");
        int maxCols = 0;

        String[][] cells = new String[rows.length][];

        for (int i = 0; i < rows.length; i++) {
            String row = rows[i];
            cells[i] = row.split("\t");
            maxCols = Math.max(maxCols, cells[i].length);
        }

        for (int colIndex = 0; colIndex < maxCols; colIndex++) {
            int maxWidth = 0;
            for (int rowIndex = 0; rowIndex < cells.length; rowIndex++) {
                if (cells[rowIndex].length > colIndex) {
                    maxWidth = Math.max(maxWidth, cells[rowIndex][colIndex].length());
                }
            }
            maxWidth++;
            char[] filler = new char[maxWidth];
            Arrays.fill(filler, ' ');
            for (int rowIndex = 0; rowIndex < cells.length; rowIndex++) {
                if (cells[rowIndex].length > colIndex && cells[rowIndex][colIndex].length() < maxWidth) {
                    cells[rowIndex][colIndex] = new StringBuilder(cells[rowIndex][colIndex])
                            .append(filler, 0, maxWidth - cells[rowIndex][colIndex].length())
                            .toString();
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        for (int rowIndex = 0; rowIndex < cells.length; rowIndex++) {
            for (int colIndex = 0; colIndex < cells[rowIndex].length; colIndex++) {
                builder.append(cells[rowIndex][colIndex]);
            }
            builder.append('\n');
        }
        return builder.toString();
    }

}
