/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.EnumSet;

public class UpgraderV10_7 implements Upgrader {
    public static final Version VERSION = Version.version(10, 7);
    private final DataModel dataModel;
    private final UserService userService;
    private final PrivilegesProviderV10_7 privilegesProviderV10_7;
    private final EventService eventService;

    @Inject
    public UpgraderV10_7(DataModel dataModel,
                         UserService userService,
                         PrivilegesProviderV10_7 privilegesProviderV10_7,
                         EventService eventService) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.privilegesProviderV10_7 = privilegesProviderV10_7;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        userService.addModulePrivileges(privilegesProviderV10_7);
        EnumSet.of(
                EventType.INBOUND_AUTH_FAILURE,
                EventType.OUTBOUND_ENDPOINT_NOT_AVAILABLE,
                EventType.OUTBOUND_NO_ACKNOWLEDGEMENT,
                EventType.OUTBOUND_BAD_ACKNOWLEDGEMENT,
                EventType.OUTBOUND_AUTH_FAILURE
        ).forEach(et -> et.installIfNotPresent(eventService));
    }
}
