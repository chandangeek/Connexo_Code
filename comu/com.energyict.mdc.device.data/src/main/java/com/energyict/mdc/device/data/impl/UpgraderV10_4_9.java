package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class UpgraderV10_4_9 implements Upgrader {

    private static final Logger logger = Logger.getLogger(UpgraderV10_4_9.class.getName());

    private final DataModel dataModel;

    @Inject
    public UpgraderV10_4_9(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModel.useConnectionRequiringTransaction(this::updateCRLTable);
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 9));
    }

    private void updateCRLTable(Connection connection) {
        int definedCRLTasks = dataModel.mapper(CrlRequestTaskProperty.class).find().size();
        if (definedCRLTasks != 0) {
            throw new RuntimeException("Cannot upgrade while CRL tasks defined and cannot be automatically upgraded. Please delete your CRL tasks and retry");
        }
        final String dropConstraint = "ALTER TABLE" + TableSpecs.DDC_CRL_REQUEST_TASK_PROPS.name() + " DROP CONSTRAINT FK_DDC_CRL_SECURITYACCESSOR";
        final String dropColumn = "ALTER TABLE" + TableSpecs.DDC_CRL_REQUEST_TASK_PROPS.name() + " DROP COLUMN SECURITY_ACCESSOR";

        List<String> updateSql = Arrays.asList(dropConstraint, dropColumn);
        updateSql.forEach(sqlStatement -> {
            try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                logger.info("Executing: " + sqlStatement);
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.warning("Couldn't perform upgrade for \n" + sqlStatement);
                throw new UnderlyingSQLFailedException(e);
            }
        });
    }

}
