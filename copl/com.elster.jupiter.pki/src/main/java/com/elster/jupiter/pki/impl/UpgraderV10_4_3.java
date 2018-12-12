/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Statement;

public class UpgraderV10_4_3 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    public UpgraderV10_4_3(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        this.dataModel.useConnectionRequiringTransaction((connection) -> {
            Statement statement = connection.createStatement();
            Throwable var3 = null;

            try {
                ImmutableList.of("alter table SSM_PLAINTEXTSK add DISCRIMINATOR char(1) default 'P' CONSTRAINT NOT_NULL_DISCRIMANTOR NOT NULL").forEach((command) -> {
                    this.execute(statement, command);
                });
            } catch (Throwable var12) {
                var3 = var12;
                throw var12;
            } finally {
                if (statement != null) {
                    if (var3 != null) {
                        try {
                            statement.close();
                        } catch (Throwable var11) {
                            var3.addSuppressed(var11);
                        }
                    } else {
                        statement.close();
                    }
                }
            }
        });
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 3));

    }


}
