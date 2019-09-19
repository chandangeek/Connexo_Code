/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.install;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_7 implements Upgrader {

    private final EventService eventService;
    private final MessageService messageService;
    private final DataModel dataModel;
    private final IssueService issueService;

    @Inject
    public UpgraderV10_7(EventService eventService, MessageService messageService, DataModel dataModel, IssueService issueService) {
        this.eventService = eventService;
        this.messageService = messageService;
        this.dataModel = dataModel;
        this.issueService = issueService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
    }
}
