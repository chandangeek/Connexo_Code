package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.upgrade.*;
import com.google.common.collect.*;

import javax.inject.*;
import java.sql.*;
import java.time.*;
import java.util.*;

public class UpgraderV10_4_19 implements Upgrader {

    private final DataModel dataModel;
    private final Clock clock;

    @Inject
    UpgraderV10_4_19(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 19));
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                ImmutableList.of(
                        "ALTER TABLE MTR_ENDDEVICEEVENTRECORD MODIFY DESCRIPTION VARCHAR2("+ Table.DESCRIPTION_LENGTH +" CHAR)"
                ).forEach(command -> execute(statement, command));
            }
        });
    }
}