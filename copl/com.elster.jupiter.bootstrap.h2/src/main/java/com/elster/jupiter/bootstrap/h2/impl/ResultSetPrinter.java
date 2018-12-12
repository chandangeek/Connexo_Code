/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.h2.impl;

import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Prints a ResultSet for debugging purposes.
 * The current output goes to System.out and is formatted as following:
 * <ul>
 * <li>Tab separated list of selected column names</li>
 * <li>Sequence of rows returned where every element is separated by a tab</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-01 (08:55)
 */
public class ResultSetPrinter {

    private final PrintStream out;

    public ResultSetPrinter() {
        super();
        out = System.out;
    }

    public ResultSetPrinter(PrintStream out) {
        super();
        this.out = out;
    }

    public void print (ResultSet resultSet) {
        try {
            this.print(resultSet, resultSet.getMetaData());
        }
        catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    private void print (ResultSet resultSet, ResultSetMetaData metaData) throws SQLException {
        this.printHeader(metaData);
        this.printRows(resultSet, metaData);
    }

    private void printHeader (ResultSetMetaData metaData) throws SQLException {
        int numberOfColumns = metaData.getColumnCount();
        for (int i = 0; i < numberOfColumns; i++) {
            out.print(metaData.getColumnLabel(i + 1));
            if (i < numberOfColumns - 1) {
                out.print("\t");
            }
        }
        out.println();
    }

    private void printRows (ResultSet resultSet, ResultSetMetaData metaData) throws SQLException {
        int numberOfRows = 0;
        while (resultSet.next()) {
            this.printRow(resultSet, metaData);
            numberOfRows++;
            out.println();
        }
        out.println("numberOfRows = " + numberOfRows);
    }

    private void printRow (ResultSet resultSet, ResultSetMetaData metaData) throws SQLException {
        int numberOfColumns = metaData.getColumnCount();
        for (int i = 0; i < numberOfColumns; i++) {
            out.print(resultSet.getObject(i + 1));
            if (i < numberOfColumns - 1) {
                out.print("\t");
            }
        }
    }

}