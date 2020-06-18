/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.issue.datacollection.impl.install;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Where;

import com.energyict.mdc.issue.datacollection.DataCollectionEventMetadata;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;
import com.energyict.mdc.issue.datacollection.impl.records.DataCollectionEventMetadataImpl;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;

import static com.elster.jupiter.orm.Version.version;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.CONNECTION_LOST;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.DEVICE_COMMUNICATION_FAILURE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNABLE_TO_CONNECT;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNKNOWN_INBOUND_DEVICE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNKNOWN_OUTBOUND_DEVICE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNREGISTERED_FROM_GATEWAY;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionResolveEventDescription.CONNECTION_LOST_AUTO_RESOLVE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionResolveEventDescription.DEVICE_COMMUNICATION_FAILURE_AUTO_RESOLVE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionResolveEventDescription.REGISTERED_TO_GATEWAY;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionResolveEventDescription.UNABLE_TO_CONNECT_AUTO_RESOLVE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionResolveEventDescription.UNKNOWN_INBOUND_DEVICE_EVENT_AUTO_RESOLVE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionResolveEventDescription.UNKNOWN_OUTBOUND_DEVICE_EVENT_AUTO_RESOLVE;

public class UpgraderV10_8 implements Upgrader {

    private static final long PARTITIONSIZE = 86400L * 30L * 1000L;
    private final DataModel dataModel;
    private final Clock clock;
    private final IssueDataCollectionService issueDataCollectionService;
    private final TimeService timeService;

    @Inject
    UpgraderV10_8(DataModel dataModel, Clock clock, IssueDataCollectionService issueDataCollectionService, TimeService timeService) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.issueDataCollectionService = issueDataCollectionService;
        this.timeService = timeService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 8));
        //append partition for next month and enable auto increment partition interval
        if (dataModel.getSqlDialect().hasPartitioning()) {
            execute(dataModel, "LOCK TABLE IDC_DATACOLLECTION_EVENT PARTITION FOR (" + clock.instant().plusMillis(PARTITIONSIZE).toEpochMilli() + ") IN SHARE MODE",
                    "ALTER TABLE IDC_DATACOLLECTION_EVENT SET INTERVAL (" + PARTITIONSIZE + ")");
        }

        updateDataCollectionEventMetadata();
        createAndAssignIssueRelativePeriodsCategory();
    }

    private void createAndAssignIssueRelativePeriodsCategory() {
        if (!timeService.findRelativePeriodCategoryByName(ModuleConstants.ISSUE_RELATIVE_PERIOD_CATEGORY).isPresent()) {
            RelativePeriodCategory category = timeService.createRelativePeriodCategory(ModuleConstants.ISSUE_RELATIVE_PERIOD_CATEGORY);

            timeService.getRelativePeriodQuery().select(Where.where("relativePeriodCategoryUsages.relativePeriodCategory.name")
                    .isEqualTo("relativeperiod.category.deviceAlarm"))
                    .forEach(relativePeriod -> relativePeriod.addRelativePeriodCategory(category));
        }
    }

    private void updateDataCollectionEventMetadata() {
        updateEventTypeColumn();
    }

    private void updateEventTypeColumn() {
        List<DataCollectionEventMetadata> dataCollectionEvents = issueDataCollectionService.getDataCollectionEvents();

        dataCollectionEvents.forEach(event -> {
            switch (event.getEventType()) {
                case "com/energyict/mdc/connectiontask/COMPLETION":
                    updateDataCollectionEvent(event, CONNECTION_LOST_AUTO_RESOLVE);
                    createDataCollectionEvent(event, DEVICE_COMMUNICATION_FAILURE_AUTO_RESOLVE);
                    createDataCollectionEvent(event, UNABLE_TO_CONNECT_AUTO_RESOLVE);
                    break;
                case "com/energyict/mdc/device/data/device/CREATED":
                    updateDataCollectionEvent(event, UNKNOWN_INBOUND_DEVICE_EVENT_AUTO_RESOLVE);
                    createDataCollectionEvent(event, UNKNOWN_OUTBOUND_DEVICE_EVENT_AUTO_RESOLVE);
                    break;
                case "com/energyict/mdc/topology/REGISTEREDTOGATEWAY":
                    updateDataCollectionEvent(event, REGISTERED_TO_GATEWAY);
                    break;
                case "com/energyict/mdc/connectiontask/FAILURE":
                    updateDataCollectionEvent(event, CONNECTION_LOST);
                    createDataCollectionEvent(event, DEVICE_COMMUNICATION_FAILURE);
                    createDataCollectionEvent(event, UNABLE_TO_CONNECT);
                    break;
                case "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE":
                    updateDataCollectionEvent(event, UNKNOWN_INBOUND_DEVICE);
                    break;
                case "com/energyict/mdc/outboundcommunication/UNKNOWNSLAVEDEVICE":
                    updateDataCollectionEvent(event, UNKNOWN_OUTBOUND_DEVICE);
                    break;
                case "com/energyict/mdc/topology/UNREGISTEREDFROMGATEWAY":
                    updateDataCollectionEvent(event, UNREGISTERED_FROM_GATEWAY);
                    break;
                default:
                    event.delete();
            }
        });
    }

    private void createDataCollectionEvent(DataCollectionEventMetadata event, EventDescription description) {
        new DataCollectionEventMetadataImpl(dataModel)
                .init(description.getName(), event.getDevice(), null, event.getCreateDateTime())
                .save();
    }

    private void updateDataCollectionEvent(DataCollectionEventMetadata event, EventDescription description) {
        event.setEventType(description.getName());
        event.update();
    }
}
