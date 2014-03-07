package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/issue")
public class HelpResource extends BaseResource {
    public HelpResource() {
        super();
    }

    //TODO delete when events will be produced by MDC
    @GET
    @Path("/event")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getEvent() {
        getIssueHelpService().getEvent();
        return true;
    }
    // END delete when events will be produced by MDC

    //TODO for tests
    @GET
    @Path("/new")
    public IssueInfo createNew(@BeanParam StandardParametersBean params){
        IssueInfo<DeviceInfo> response = null;
        try (TransactionContext context = getTransactionService().getContext()) {
            Issue issue = getIssueHelpService().createTestIssue(
                    params.get("status").get(0),
                    params.get("reason").get(0),
                    params.get("device").get(0),
                    Long.parseLong(params.get("due").get(0))).get();
            response = new IssueInfo<>(issue, DeviceInfo.class);
            context.commit();
        }
        return response;
    }
}
