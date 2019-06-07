/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.issue.IssueServiceCallService;
import com.elster.jupiter.servicecall.issue.TranslationKeys;
import com.elster.jupiter.servicecall.issue.impl.event.ServiceCallEventDescription;
import com.elster.jupiter.upgrade.FullInstaller;

import com.google.inject.Inject;

import java.util.logging.Logger;

class Installer implements FullInstaller {

    private final IssueService issueService;
    private final DataModel dataModel;

    @Inject
    Installer(DataModel dataModel, IssueService issueService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create issue view operation",
                () -> new CreateIssueViewOperation(dataModel).execute(),
                logger
        );
        run(() -> {
            issueService.createIssueType(IssueServiceCallService.ISSUE_TYPE_NAME, TranslationKeys.SERVICE_CALL_ISSUE_TYPE, IssueServiceCallService.SERVICE_CALL_ISSUE_PREFIX);
        }, "issue type", logger);
        doTry(
                "Publish events",
                this::publishEvents,
                logger
        );
    }

    private void publishEvents() {
        for (ServiceCallEventDescription eventDescription : ServiceCallEventDescription.values()) {
//            eventService.getEventType(eventDescription.getTopic()).ifPresent(eventType -> {
//                eventType.setPublish(true);
//                eventType.update();
//            });
        }
    }

    private void run(Runnable runnable, String explanation, Logger logger) {
        doTry(
                explanation,
                runnable,
                logger
        );
    }
}
