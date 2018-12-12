/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.h2.impl;

import com.elster.jupiter.util.Pair;

import com.google.common.base.Strings;
import org.h2.jdbcx.JdbcDataSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;

public enum InMemoryPersistence {
    ;

    private static final String url = "jdbc:h2:mem:DB;MVCC=TRUE;lock_timeout=5000;MODE=Oracle";

    public static String query(String sql) {
        JdbcDataSource dataSource = getDataSource();

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

    public static String execute(String sql) {
        JdbcDataSource dataSource = getDataSource();

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                return String.valueOf(statement.executeUpdate(sql));
            }
        } catch (SQLException e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            return stringWriter.toString();
        }
    }

    public static JdbcDataSource getDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(url);
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
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

    public static String currentRow(ResultSet resultSet) {
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            Pair<String, String> headerAndData = IntStream.rangeClosed(1, columnCount)
                    .mapToObj(i -> {
                        try {
                            return Pair.of(metaData.getColumnName(i), Objects.toString(resultSet.getObject(i)));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(pair -> {
                        int max = Math.max(pair.getFirst().length(), pair.getLast().length());
                        return pair
                                .withFirst(s -> Strings.padEnd(s, max, ' '))
                                .withLast(s -> Strings.padEnd(s, max, ' '));
                    })
                    .collect(new Collector<Pair<String, String>, Pair<StringBuilder, StringBuilder>, Pair<String, String>>() {
                        @Override
                        public Supplier<Pair<StringBuilder, StringBuilder>> supplier() {
                            return () -> Pair.of(new StringBuilder(), new StringBuilder());
                        }

                        @Override
                        public BiConsumer<Pair<StringBuilder, StringBuilder>, Pair<String, String>> accumulator() {
                            return (builderPair, stringPair) -> builderPair
                                    .withFirst(builder -> builder.append(" | ").append(stringPair.getFirst()))
                                    .withLast(builder -> builder.append(" | ").append(stringPair.getLast()));
                        }

                        @Override
                        public BinaryOperator<Pair<StringBuilder, StringBuilder>> combiner() {
                            return (b1, b2) -> b1
                                    .withFirst(builder -> builder.append(" | ").append(b2.getFirst()))
                                    .withLast(builder -> builder.append(" | ").append(b2.getLast()));
                        }

                        @Override
                        public Function<Pair<StringBuilder, StringBuilder>, Pair<String, String>> finisher() {
                            return pair -> pair
                                    .withFirst(StringBuilder::toString)
                                    .withLast(StringBuilder::toString);
                        }

                        @Override
                        public Set<Characteristics> characteristics() {
                            return Collections.emptySet();
                        }
                    });
            return headerAndData.getFirst() + '\n' + headerAndData.getLast();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
