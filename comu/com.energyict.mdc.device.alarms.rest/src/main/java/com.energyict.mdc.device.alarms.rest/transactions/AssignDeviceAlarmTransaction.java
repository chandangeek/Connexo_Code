package com.energyict.mdc.device.alarms.rest.transactions;


import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.alarms.rest.request.AssignDeviceAlarmRequest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.function.Function;

public class AssignDeviceAlarmTransaction implements Transaction<ActionInfo> {
    private final AssignDeviceAlarmRequest request;
    private final User performer;
    private final Function<ActionInfo, List<? extends Issue>> alarmProvider;

    public AssignDeviceAlarmTransaction(AssignDeviceAlarmRequest request, User performer, Function<ActionInfo, List<? extends Issue>> alarmProvider) {
        this.request = request;
        this.performer = performer;
        this.alarmProvider = alarmProvider;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();
        if (request.assignee != null) {
            for (Issue issue : alarmProvider.apply(response)) {
                issue.assignTo(request.assignee.userId, request.assignee.workGroupId);
                issue.addComment(request.comment, performer);
                issue.update();
                response.addSuccess(issue.getId());
            }
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return response;
    }

}

