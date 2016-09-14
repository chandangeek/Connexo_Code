package com.elster.jupiter.bpm.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Main class to perform jBPM database upgrades
 */
public class FlowUpgrader {
    public static void main(String args[]) {
        if (args.length != 4) {
            System.out.println("Incorrect syntax. The following parameters are required:");
            System.out.println("jdbc -- Connexo Flow JDBC url");
            System.out.println("user -- Connexo Flow database user");
            System.out.println("password -- password for the provided user");
            System.out.println("file -- upgrade sql file to execute");
            return;
        }

        System.exit(executeSql(args[0], args[1], args[2], args[3]));
    }

    private static int executeSql(String jdbc, String user, String password, String path_to_file) {
        int exitCode = 0;
        Connection connection = null;
        PreparedStatement statement = null;
        String sql = getSqlFromFile(path_to_file);
        if (sql == null) {
            exitCode = 1;
        } else {
            try {
                connection = getConnection(jdbc, user, password);
                connection.setAutoCommit(true);
                statement = connection.prepareStatement(sql);
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                exitCode = 1;
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        exitCode = 1;
                    }
                }
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        exitCode = 1;
                    }
                }
            }
        }
        return exitCode;
    }

    private static String getSqlFromFile(String path_to_file) {
        try {
            return Files.readAllLines(Paths.get(path_to_file)).stream().collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Connection getConnection(String jdbc, String user, String password) throws SQLException {

        Properties connectionProps = new Properties();
        connectionProps.put("user", user);
        connectionProps.put("password", password);

        return DriverManager.getConnection(jdbc, connectionProps);
    }
}
