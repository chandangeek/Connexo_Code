/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpgraderV10_4_HSM implements Upgrader {
    private final OrmService ormService;

    @Inject
    public UpgraderV10_4_HSM(OrmService ormService) {
        this.ormService = ormService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        this.upgradeSubscriberSpecs(ormService.getDataModel(MessageService.COMPONENTNAME).get());
    }

    private void upgradeSubscriberSpecs(DataModel dataModel) {
        try (Connection connection = dataModel.getConnection(true)) {
            this.upgradeSubscriberSpecs(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeSubscriberSpecs(Connection connection) {
        try (PreparedStatement statement = this.upgradeSubscriberSpecsStatement(connection)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private PreparedStatement upgradeSubscriberSpecsStatement(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE MSG_SUBSCRIBERSPEC SET nls_component = ?, nls_layer = ? WHERE name = ?");
        statement.setString(1, SecureHSMDeviceShipmentImporterMessageHandler.COMPONENT_NAME);
        statement.setString(2, Layer.DOMAIN.name());
        statement.setString(3, SecureHSMDeviceShipmentImporterMessageHandler.SUBSCRIBER_NAME);
        return statement;
    }

}
