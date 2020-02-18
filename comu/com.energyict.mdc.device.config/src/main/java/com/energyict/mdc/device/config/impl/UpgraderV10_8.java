/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.common.device.config.EventType;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;

@LiteralSql
class UpgraderV10_8 implements Upgrader {
    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;

    @Inject
    UpgraderV10_8(DataModel dataModel, EventService eventService, MessageService messageService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8));
        installNewEventTypes();
        try (Connection connection = dataModel.getConnection(true);
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE MSG_SUBSCRIBERSPEC SET NLS_COMPONENT = 'DTC' WHERE NAME = 'DeviceTypesChanges'");
        ) {
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void installNewEventTypes() {
        EnumSet.of(
                EventType.DEVICE_TYPE_LIFE_CYCLE_CACHE_RECALCULATED
        ).forEach(eventType -> eventType.install(eventService));
    }
}
