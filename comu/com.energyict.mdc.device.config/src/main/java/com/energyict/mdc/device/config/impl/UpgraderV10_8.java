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
import java.util.EnumSet;
import java.util.logging.Logger;

@LiteralSql
class UpgraderV10_8 implements Upgrader {
    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

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
        insertGarnetDialectToEdmiV2();
        execute(dataModel, "UPDATE MSG_SUBSCRIBERSPEC SET NLS_COMPONENT = 'DTC' WHERE NAME = 'DeviceTypesChanges'");
    }

    private void insertGarnetDialectToEdmiV2() {
        logger.fine("Inserting 'GarnetSerialDialect' to 'EDMI MK10 [Pull] CommandLine V2' device types");

        execute(dataModel, "INSERT INTO " +
                "DTC_DIALECTCONFIGPROPERTIES DCP (DEVICECONFIGURATION, ID, DEVICEPROTOCOLDIALECT, NAME, CREATETIME, MODTIME) " +
                "SELECT DCONFIG.ID, DTC_DIALECTCONFIGPROPERTIESID.nextval, 'GarnetSerialDialect', 'GarnetSerialDialect', DCONFIG.CREATETIME, DCONFIG.MODTIME " +
                "FROM DTC_DEVICECONFIG DCONFIG " +
                "INNER JOIN DTC_DEVICETYPE DTYPE " +
                "ON DCONFIG.DEVICETYPEID = DTYPE.ID " +
                "INNER JOIN CPC_PLUGGABLECLASS PC " +
                "ON DTYPE.DEVICEPROTOCOLPLUGGABLEID = PC.ID " +
                "WHERE PC.JAVACLASSNAME = 'com.energyict.protocolimplv2.edmi.mk10.MK10'");
    }

    private void installNewEventTypes() {
        EnumSet.of(
                EventType.DEVICE_TYPE_LIFE_CYCLE_CACHE_RECALCULATED
        ).forEach(eventType -> eventType.install(eventService));
    }
}
