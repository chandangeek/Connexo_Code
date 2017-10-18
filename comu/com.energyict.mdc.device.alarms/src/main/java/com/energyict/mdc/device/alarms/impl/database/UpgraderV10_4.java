/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.database;

import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmActionsFactory;
import com.energyict.mdc.device.alarms.impl.actions.WebServiceNotificationAlarmAction;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

public class UpgraderV10_4 implements Upgrader {

    private final DataModel dataModel;
    private final IssueService issueService;
    private final IssueActionService issueActionService;

    @Inject
    UpgraderV10_4(DataModel dataModel, IssueService issueService, IssueActionService issueActionService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.issueActionService = issueActionService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 4));
        this.createActionTypesIfNotPresent();
        this.upgradeOpenAlarm();
    }

    private void upgradeOpenAlarm() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeOpenAlarm(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeOpenAlarm(Connection connection) {
        String[] sqlStatements = {
                "CREATE OR REPLACE VIEW DAL_ALARM_ALL AS SELECT * FROM DAL_ALARM_OPEN UNION SELECT * FROM DAL_ALARM_HISTORY"};
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

}
