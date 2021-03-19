/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public enum SqlDialect {
    /*
     * H2 Database
     */
    H2 {
        @Override
        public String rowId() {
            return "_ROWID_";
        }

        @Override
        public boolean hasPartitioning() {
            return false;
        }

        @Override
        public boolean hasIndexOrganizedTables() {
            return false;
        }

        @Override
        public boolean hasIndexCompression() {
            return false;
        }

        @Override
        public String renameColumnSyntax() {
            return "ALTER TABLE {0} ALTER COLUMN {1} RENAME TO {2}";
        }

        @Override
        public boolean allowsConstraintRename() {
            return false;
        }

        @Override
        public String leftPad(String field, int zerofillSize, String padCharacter) {
            return "lpad(" + field + "," + zerofillSize + ",'" + padCharacter + "'";
        }

        @Override
        public boolean allowHints() {
            return false;
        }

        @Override
        public List<Long> getMultipleNextVals(Statement statement, String sequenceName, int number) {
            List<Long> nextVals = new ArrayList<>();
            for (int i = 0; i < number; i++) {
                try (ResultSet resultSet = statement.executeQuery("SELECT " + sequenceName + ".nextval FROM dual")) {
                    resultSet.next();
                    nextVals.add(resultSet.getLong(1));
                } catch (SQLException e) {
                    throw new UnderlyingSQLFailedException(e);
                }
            }
            return nextVals;
        }

        @Override
        public Optional<String> renameSyntax() {
            return Optional.empty();
        }
    },
    /*
     * Oracle Enterprise Edition with partitioning option
     */
    ORACLE_EE {
        @Override
        public String rowId() {
            return "ROWID";
        }

        @Override
        public boolean hasPartitioning() {
            return true;
        }

        @Override
        public boolean hasIndexOrganizedTables() {
            return true;
        }

        @Override
        public boolean hasIndexCompression() {
            return true;
        }

        @Override
        public boolean allowHints() {
            return true;
        }

        @Override
        public String leftPad(String field, int zerofillSize, String padCharacter) {
            return "lpad(to_char(" + field + ")," + zerofillSize + ",'" + padCharacter + "'";
        }

        @Override
        public boolean allowHints() {
            return true;
        }

        @Override
        public List<Long> getMultipleNextVals(Statement statement, String sequenceName, int number) {
            List<Long> nextVals = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery("SELECT " + sequenceName + ".nextval FROM dual connect by level <= " + number)) {
                while (resultSet.next()) {
                    nextVals.add(resultSet.getLong(1));
                }
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
            return nextVals;
        }
    },
    /*
     * Oracle Standard Edition
     */
    ORACLE_SE {
        @Override
        public String rowId() {
            return "ROWID";
        }

        @Override
        public boolean hasPartitioning() {
            return false;
        }

        @Override
        public boolean hasIndexOrganizedTables() {
            return true;
        }

        @Override
        public boolean hasIndexCompression() {
            return true;
        }

        @Override
        public String leftPad(String field, int zerofillSize, String padCharacter) {
            return "lpad(to_char(" + field + ")," + zerofillSize + ",'" + padCharacter + "'";
        }

        @Override
        public boolean allowHints() {
            return true;
        }

        @Override
        public List<Long> getMultipleNextVals(Statement statement, String sequenceName, int number) {
            List<Long> nextVals = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery("SELECT " + sequenceName + ".nextval FROM dual connect by level <= " + number)) {
                while (resultSet.next()) {
                    nextVals.add(resultSet.getLong(1));
                }
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
            return nextVals;
        }

        @Override
        public boolean allowHints() {
            return true;
        }
    };

    abstract public String rowId();

    abstract public boolean hasPartitioning();

    abstract public boolean hasIndexOrganizedTables();

    abstract public boolean hasIndexCompression();

    public String renameColumnSyntax() {
        return "ALTER TABLE {0} RENAME COLUMN {1} TO {2}";
    }

    public String renameConstraintSyntax() {
        return "ALTER TABLE {0} RENAME CONSTRAINT {1} TO {2}";
    }

    public Optional<String> renameSyntax() {
        return Optional.of("RENAME {0} TO {1}");
    }

    public boolean allowsConstraintRename() {
        return true;
    }

    public abstract boolean allowHints();

    public abstract String leftPad(String id, int zerofillSize, String s);

    public abstract boolean allowHints();

    public abstract List<Long> getMultipleNextVals(Statement statement, String sequenceName, int number);
}
