package com.energyict.mdc.device.alarms.impl.database;

import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmActionsFactory;
import com.energyict.mdc.device.alarms.impl.actions.MailNotificationAlarmAction;

import javax.inject.Inject;
import java.util.Optional;
import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;


public class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final IssueService issueService;
    private final IssueActionService issueActionService;


    @Inject
    UpgraderV10_7(DataModel dataModel, IssueService issueService, IssueActionService issueActionService, MessageService messageService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.issueActionService = issueActionService;

    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 7));
        this.createActionTypesIfNotPresent();
    }
    private void createActionTypesIfNotPresent() {
        IssueType deviceAlarmType = issueService.findIssueType(DeviceAlarmService.DEVICE_ALARM).get();
        Condition classNameCondition = buildCondition("className", Optional.of(MailNotificationAlarmAction.class.getName()));
        Condition factoryCondition = buildCondition("factoryId", Optional.of(DeviceAlarmActionsFactory.ID));
        if (issueActionService.getActionTypeQuery().select(classNameCondition.and(factoryCondition)).isEmpty()) {
            issueActionService.createActionType(DeviceAlarmActionsFactory.ID, MailNotificationAlarmAction.class.getName(), deviceAlarmType, CreationRuleActionPhase.CREATE);
        }
    }
    private Condition buildCondition(String field, Optional<?> value) {
        Condition condition = where(field).isNull();
        if (value.isPresent()) {
            condition = condition.or(where(field).isEqualTo(value.get()));
        }
        return condition;
    }
}
