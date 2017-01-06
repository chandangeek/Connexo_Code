package com.energyict.mdc.device.alarms.rest.transactions;

import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.Transaction;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.rest.i18n.MessageSeeds;

import java.util.function.Function;


public class UnassignSingleDeviceAlarmTransaction implements Transaction<ActionInfo> {
    private final Function<ActionInfo, DeviceAlarm> deviceAlarmProvider;
    private final Thesaurus thesaurus;

    public UnassignSingleDeviceAlarmTransaction(Function<ActionInfo, DeviceAlarm> deviceAlarmProvider, Thesaurus thesaurus) {
        this.deviceAlarmProvider = deviceAlarmProvider;
        this.thesaurus = thesaurus;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();

        Issue issue = deviceAlarmProvider.apply(response);
        Long workGroupId = issue.getAssignee().getWorkGroup() != null ? issue.getAssignee().getWorkGroup().getId() : -1L;
        issue.assignTo(-1L, workGroupId);
        issue.update();
        response.addSuccess(issue.getId(), thesaurus.getFormat(MessageSeeds.ACTION_ALARM_WAS_UNASSIGNED).format());

        return response;
    }
}