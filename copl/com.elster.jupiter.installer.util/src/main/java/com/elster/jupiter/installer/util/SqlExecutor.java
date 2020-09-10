/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.installer.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main class to execute sql operations
 */
public class SqlExecutor {

    public static void main(String args[]) {
        if (args.length != 4) {
            System.out.println("Incorrect syntax. The following parameters are required:");
            System.out.println("jdbc -- Connexo Flow JDBC url");
            System.out.println("user -- Connexo Flow database user");
            System.out.println("password -- password for the provided user");
            System.out.println("file -- sql file to execute");
            throw new RuntimeException();
        }

        executeSqlFromFile(args[0], args[1], args[2], args[3]);
    }

    private static void executeSqlFromFile(String jdbc, String user, String password, String pathToFile) {
        List<String> commands = getSqlFromFile(pathToFile);
        if (commands.isEmpty()) {
            throw new RuntimeException("Couldn't read SQL commands from the file!");
        } else {
            try (Connection connection = Utils.getConnection(jdbc, user, password)) {
                connection.setAutoCommit(true);

                try (Statement statement = connection.createStatement()) {
                    for (String command : commands) {
                        statement.execute(command);
                    }
                }
            } catch (SQLException ex) {
                throw new RuntimeException("Sql exception: " + ex.getMessage(), ex);
            }
        }
    }

    private static List<String> getSqlFromFile(String pathToFile) {
        try {
            return Arrays.stream(new String(Files.readAllBytes(Paths.get(pathToFile))).split(";"))
                    .map(String::trim)
                    .filter(str -> !str.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Exception reading " + pathToFile + " file: " + e.getMessage(), e);
        }
    }
}
