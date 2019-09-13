package com.elster.jupiter.search.impl;


import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.util.streams.Currying.perform;

class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    public UpgraderV10_7(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
        this.upgradeOpenIssue();
    }

    private void upgradeOpenIssue() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeOpenIssue(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeOpenIssue(Connection connection) {
        List<String> ddl = Arrays.asList(
                "CREATE TABLE DYN_SEARCHCRITERIA (" +
                        "ID NUMBER NOT NULL" +
                        "NAME VARCHAR2(80 CHAR)  NOT NULL" +
                        "USERNAME  VARCHAR2(80 CHAR)  NOT NULL"+
                        "CRITERIA  VARCHAR2(4000 CHAR)  NOT NULL"+
                        "DOMAIN  VARCHAR2(80 CHAR)  NOT NULL"+
                        ")",
                "CREATE UNIQUE INDEX DYN_PK_DYN_SEARCHCRITERIA ON DYN_SEARCHCRITERIA (NAME)",
                "ALTER TABLE DYN_SEARCHCRITERIA ADD CONSTRAINT DYN_PK_DYN_SEARCHCRITERIA PRIMARY KEY (NAME, USERNAME)",
                "CREATE SEQUENCE  \"DYN_SEARCHCRITERIAID\"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 1000 NOORDER  NOCYCLE"
        );
        try (Statement statement = connection.createStatement()) {
            ddl.forEach(perform(this::executeDdl).on(statement));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void executeDdl(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }


}