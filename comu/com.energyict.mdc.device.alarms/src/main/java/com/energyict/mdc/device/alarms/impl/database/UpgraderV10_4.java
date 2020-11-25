/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.database;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmActionsFactory;
import com.energyict.mdc.device.alarms.impl.actions.WebServiceNotificationAlarmAction;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;
import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

public class UpgraderV10_4 implements Upgrader {

    private final DataModel dataModel;
    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final MessageService messageService;

    @Inject
    UpgraderV10_4(DataModel dataModel, IssueService issueService, IssueActionService issueActionService, MessageService messageService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.issueActionService = issueActionService;
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 4));
        this.createActionTypesIfNotPresent();
        this.upgradeAlarmTables();
        this.setAQSubscriber();
    }

    private void upgradeAlarmTables() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeAlarmTables(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeAlarmTables(Connection connection) {
        String[] sqlStatements = {
                "CREATE OR REPLACE VIEW DAL_ALARM_ALL AS SELECT * FROM DAL_ALARM_OPEN UNION SELECT * FROM DAL_ALARM_HISTORY",
                "UPDATE DAL_OPEN_ALM_RELATED_EVT alm SET CREATETIME = (SELECT isu.CREATETIME from ISU_ISSUE_OPEN isu where alm.ALARM = isu.ID)",
                "UPDATE DAL_HIST_ALM_RELATED_EVT alm SET CREATETIME = (SELECT isu.CREATETIME from ISU_ISSUE_HISTORY isu where alm.ALARM = isu.ISU_HIST_ISSUE_ID)"
        };
        for (String sqlStatement : sqlStatements) {
            try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }

    private void createActionTypesIfNotPresent() {
        IssueType deviceAlarmType = issueService.findIssueType(DeviceAlarmService.DEVICE_ALARM).get();
        Condition classNameCondition = buildCondition("className", Optional.of(WebServiceNotificationAlarmAction.class.getName()));
        Condition factoryCondition = buildCondition("factoryId", Optional.of(DeviceAlarmActionsFactory.ID));
        if (issueActionService.getActionTypeQuery().select(classNameCondition.and(factoryCondition)).isEmpty()) {
            issueActionService.createActionType(DeviceAlarmActionsFactory.ID, WebServiceNotificationAlarmAction.class.getName(), deviceAlarmType, CreationRuleActionPhase.CREATE);
        }
    }

    private Condition buildCondition(String field, Optional<?> value) {
        Condition condition = where(field).isNull();
        if (value.isPresent()) {
            condition = condition.or(where(field).isEqualTo(value.get()));
        }
        return condition;
    }

    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        try {
            destinationSpec.subscribe(
                    TranslationKeys.DEVICE_TYPES_CHANGES_EVENT_SUBSC,
                    DeviceAlarmService.COMPONENT_NAME,
                    Layer.DOMAIN,
                    whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/CREATED")
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/DELETED"))
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/dlc/UPDATED"))
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/lifecycle/config/dlc/update"))
                            .or(whereCorrelationId().isEqualTo("com/elster/jupiter/fsm/UPDATED")));
        } catch (DuplicateSubscriberNameException e) {
            // subscriber already exists, ignoring
        }
    }

}
