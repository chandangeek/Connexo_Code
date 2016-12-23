package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import aQute.bnd.annotation.ConsumerType;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

@ConsumerType
public interface Upgrader {

    void migrate(DataModelUpgrader dataModelUpgrader);

    default void execute(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            String name = this.getClass().getName();
            Logger.getLogger(name)
                    .severe("Exception during upgrade by " + name + System.lineSeparator() +
                            "Failed statement: " + sql);
            throw new UnderlyingSQLFailedException(e);
        }
    }
}
