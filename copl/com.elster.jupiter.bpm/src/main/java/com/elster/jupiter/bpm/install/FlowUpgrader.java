package com.elster.jupiter.bpm.install;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Main class to perform jBPM database upgrades
 */
public class FlowUpgrader {
    private static class SQLCommand {
        String sqlStatement;
        String sqlRollback;
    }

    public static void main(String args[]) {
        if (args.length != 6) {
            System.out.println("Incorrect syntax. The following parameters are required:");
            System.out.println("version -- Connexo version to upgrade to");
            System.out.println("jdbc -- Connexo Flow JDBC url");
            System.out.println("user -- Connexo Flow database user");
            System.out.println("password -- password for the provided user");
            System.out.println("file -- upgrade sql file to execute");
            System.out.println("action -- upgrade or rollback");
            return;
        }

        executeSql(args[0], args[1], args[2], args[3], args[4], args[5]);
    }

    private static void executeSql(String versionUpgradeTo, String jdbc, String user, String password, String path_to_file, String action) {
        Connection connection = null;
        Statement statement = null;

        int marker = 0;
        List<SQLCommand> commands = getSqlFromFile(path_to_file);
        if (commands == null || commands.isEmpty()) {
            throw new RuntimeException("Could not read SQL commands from the file!");
        } else {
            try {
                connection = getConnection(jdbc, user, password);
                connection.setAutoCommit(true);
                statement = connection.createStatement();

                /*String versionUpgradeFrom = "10.1";
                ResultSet version = null;
                try {
                    version = statement.executeQuery("select version from VERSION");
                    versionUpgradeFrom = version.getString(0);
                } catch (SQLException e) {

                }*/

                for (SQLCommand command : commands) {
                    String sql = action.equals("upgrade") ? command.sqlStatement : command.sqlRollback;
                    if (!sql.isEmpty()) {
                        statement.execute(sql);
                    }
                    marker++;
                }
            } catch (SQLException exCommit) {
                try {
                    if (connection != null) {
                        if (action.equals("upgrade")) {
                            int rollbackMarker = 0;
                            for (SQLCommand command : commands) {
                                if (rollbackMarker < marker) {
                                    if (!command.sqlRollback.isEmpty()) {
                                        statement.execute(command.sqlRollback);
                                    }
                                    rollbackMarker++;
                                }
                            }
                        }
                    }
                } catch (SQLException exRollback) {
                    throw new RuntimeException(exRollback.getMessage());
                }

                throw new RuntimeException(exCommit.getMessage());
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }
        }
    }

    private static List<SQLCommand> getSqlFromFile(String path_to_file) {
        try {
            //List<String> lines = Files.readAllLines(Paths.get(path_to_file));
            //return lines.stream().filter(line -> !(line.startsWith("--") || line.isEmpty())).collect(Collectors.joining());

            List<SQLCommand> sqlCommands = new ArrayList<>();
            File fXmlFile = new File(path_to_file);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("block");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    SQLCommand command = new FlowUpgrader.SQLCommand();
                    command.sqlStatement = eElement.getElementsByTagName("statement").item(0).getTextContent();
                    command.sqlRollback = eElement.getElementsByTagName("rollback").item(0).getTextContent();
                    sqlCommands.add(command);
                }
            }

            return sqlCommands;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static Connection getConnection(String jdbc, String user, String password) throws SQLException {

        Properties connectionProps = new Properties();
        connectionProps.put("user", user);
        connectionProps.put("password", password);

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getStackTrace().toString());
        }
        return DriverManager.getConnection(jdbc, connectionProps);
    }
}
