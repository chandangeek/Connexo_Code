package com.energyict.mdc.device.alarms.impl.install;

import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmActionsFactory;
import com.energyict.mdc.device.alarms.impl.actions.AssignDeviceAlarmAction;
import com.energyict.mdc.device.alarms.impl.actions.CloseDeviceAlarmAction;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_3 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    public UpgraderV10_3(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 3));
        this.upgradeSubscriberSpecs();
        this.updateActiontypes();
    }

    private void updateActiontypes() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.updateActiontypes(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void updateActiontypes(Connection connection) {
        IssueActionService issueActionService = this.dataModel.getInstance(IssueActionService.class);
        IssueService issueService = this.dataModel.getInstance(IssueService.class);

        IssueType deviceAlarmType = issueService.findIssueType(DeviceAlarmService.DEVICE_ALARM).get();
        Condition conditionAction = Operator.EQUALIGNORECASE.compare("className", AssignDeviceAlarmAction.class.getName());
        if (issueActionService.getActionTypeQuery()
                .select(conditionAction).isEmpty()) {
            issueActionService.createActionType(DeviceAlarmActionsFactory.ID, AssignDeviceAlarmAction.class.getName(), deviceAlarmType);
        }

        conditionAction = Operator.EQUALIGNORECASE.compare("className", CloseDeviceAlarmAction.class.getName());
        if (issueActionService.getActionTypeQuery()
                .select(conditionAction).isEmpty()) {
            issueActionService.createActionType(DeviceAlarmActionsFactory.ID, CloseDeviceAlarmAction.class.getName(), deviceAlarmType);
        }
    }

    private void upgradeSubscriberSpecs() {
        try (Connection connection = this.dataModel.getConnection(true)) {
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
        statement.setString(1, DeviceAlarmService.COMPONENT_NAME);
        statement.setString(2, Layer.DOMAIN.name());
        statement.setString(3, TranslationKeys.AQ_DEVICE_ALARM_EVENT_SUBSC.getKey());
        return statement;
    }

}
