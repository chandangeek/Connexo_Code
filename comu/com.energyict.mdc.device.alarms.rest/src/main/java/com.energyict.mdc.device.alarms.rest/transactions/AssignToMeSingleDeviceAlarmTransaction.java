/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.transactions;

import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.rest.i18n.DeviceAlarmTranslationKeys;

import java.util.function.Function;


public class AssignToMeSingleDeviceAlarmTransaction implements Transaction<ActionInfo> {
    private final User performer;
    private final Function<ActionInfo, DeviceAlarm> deviceAlarmProvider;
    private final Thesaurus thesaurus;


    public AssignToMeSingleDeviceAlarmTransaction(User performer, Function<ActionInfo, DeviceAlarm> deviceAlarmProvider, Thesaurus thesaurus) {

        this.performer = performer;
        this.deviceAlarmProvider = deviceAlarmProvider;
        this.thesaurus = thesaurus;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();

        Issue issue = deviceAlarmProvider.apply(response);
        Long userId = performer.getId();
        Long workGroupId = issue.getAssignee().getWorkGroup() != null ? issue.getAssignee().getWorkGroup().getId() : -1L;
        issue.assignTo(userId, workGroupId);
        issue.update();
        response.addSuccess(issue.getId(), thesaurus.getFormat(DeviceAlarmTranslationKeys.ACTION_ALARM_ASSIGNED_USER).format(issue.getAssignee().getUser().getName()));

        return response;
    }
}