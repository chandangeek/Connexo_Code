package com.energyict.mdc.device.alarms.impl.database;

import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.alarms.impl.templates.BasicDeviceAlarmRuleTemplate;

import javax.inject.Inject;

public class UpgraderV10_8_7 implements Upgrader {

    private final DataModel dataModel;
    private final IssueService issueService;

    @Inject
    public UpgraderV10_8_7(DataModel dataModel, IssueService issueService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        updateDeviceTypesChangesSubscriber();
        updateAlarmRulesContent();
    }

    private void updateDeviceTypesChangesSubscriber() {
        execute(dataModel, "update MSG_SUBSCRIBERSPEC set" +
                " FILTER = '(corrid = ''com/energyict/mdc/device/config/devicetype/CREATED''" +
                " OR corrid = ''com/energyict/mdc/device/config/devicetype/DELETED''" +
                " OR corrid = ''com/energyict/mdc/device/config/devicetype/dlc/UPDATED''" +
                " OR corrid = ''com/energyict/mdc/device/lifecycle/config/dlc/update''" +
                " OR corrid = ''com/elster/jupiter/fsm/UPDATED'')'" +
                " where name = 'DeviceTypesChanges'");
    }

    private void updateAlarmRulesContent() {
        issueService.getIssueCreationService().getCreationRuleQuery()
                .select(Where.where("template").isEqualTo(BasicDeviceAlarmRuleTemplate.NAME))
                .forEach(CreationRule::update);
    }
}
