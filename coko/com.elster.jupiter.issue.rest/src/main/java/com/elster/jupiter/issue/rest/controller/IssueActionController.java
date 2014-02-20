package com.elster.jupiter.issue.rest.controller;

import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.response.ActionRequestFail;
import com.elster.jupiter.issue.rest.response.BaseActionResponse;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;

@Path("/issue")
public class IssueActionController {
    private final RestQueryService queryService;
    private final IssueService issueService;
    private final TransactionService transactionService;


    @Inject
    public IssueActionController(RestQueryService queryService, IssueService issueService, TransactionService transactionService) {
        this.queryService = queryService;
        this.issueService = issueService;
        this.transactionService = transactionService;
    }

    @PUT
    @Path("/close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeIssues(final CloseIssueRequest request){
        // TODO replace by real actions
        return Response.ok().entity(
                transactionService.execute(new Transaction<BaseActionResponse>() {
                    @Override
                    public BaseActionResponse perform() {
                        BaseActionResponse response = new BaseActionResponse();
                        if (request.getIssues() != null) {
                            long[] success = new long[]{1L, 2L};

                            ActionRequestFail.IssueFailInfo info1 = new ActionRequestFail.IssueFailInfo(12L, "Unable to connect to Eimeter 1");
                            ActionRequestFail.IssueFailInfo info2 = new ActionRequestFail.IssueFailInfo(245L, "Unable to connect to Eimeter 2");

                            ActionRequestFail fail1 = new ActionRequestFail();
                            fail1.setReason("Already closed");
                            fail1.setIssues(Arrays.asList(new ActionRequestFail.IssueFailInfo[]{info1, info2}));

                            ActionRequestFail.IssueFailInfo info3 = new ActionRequestFail.IssueFailInfo(8L, "Unable to connect to Eimeter 3");
                            ActionRequestFail.IssueFailInfo info4 = new ActionRequestFail.IssueFailInfo(98L, "Unable to connect to Eimeter 4");
                            ActionRequestFail.IssueFailInfo info5 = new ActionRequestFail.IssueFailInfo(234L, "Unable to connect to Eimeter 5");

                            ActionRequestFail fail2 = new ActionRequestFail();
                            fail2.setReason("Some problems");
                            fail2.setIssues(Arrays.asList(new ActionRequestFail.IssueFailInfo[]{info3, info4, info5}));

                            response.setSuccess(success);
                            response.setFailure(Arrays.asList(new ActionRequestFail[]{fail1, fail2}));
                        } else {
                            throw new WebApplicationException(Response.Status.BAD_REQUEST);
                        }

                        return response;
                    }
                })).build();
    }

    @PUT
    @Path("/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeIssues(final AssignIssueRequest request){
        // TODO replace by real actions
        return Response.ok().entity(
                transactionService.execute(new Transaction<BaseActionResponse>() {
                    @Override
                    public BaseActionResponse perform() {
                        BaseActionResponse response = new BaseActionResponse();
                        if (request.getIssues() != null) {
                            long[] success = new long[]{1L, 2L};
                            
                            ActionRequestFail.IssueFailInfo info1 = new ActionRequestFail.IssueFailInfo(12L, "Unable to connect to Eimeter 1");
                            ActionRequestFail.IssueFailInfo info2 = new ActionRequestFail.IssueFailInfo(245L, "Unable to connect to Eimeter 2");
                            ActionRequestFail.IssueFailInfo info3 = new ActionRequestFail.IssueFailInfo(8L, "Unable to connect to Eimeter 3");

                            ActionRequestFail fail1 = new ActionRequestFail();
                            fail1.setReason("Already assigned");
                            fail1.setIssues(Arrays.asList(new ActionRequestFail.IssueFailInfo[]{info1, info2, info3}));

                            ActionRequestFail.IssueFailInfo info4 = new ActionRequestFail.IssueFailInfo(98L, "Unable to connect to Eimeter 4");
                            ActionRequestFail.IssueFailInfo info5 = new ActionRequestFail.IssueFailInfo(234L, "Unable to connect to Eimeter 5");

                            ActionRequestFail fail2 = new ActionRequestFail();
                            fail2.setReason("Some problems");
                            fail2.setIssues(Arrays.asList(new ActionRequestFail.IssueFailInfo[]{info4, info5}));

                            response.setSuccess(success);
                            response.setFailure(Arrays.asList(new ActionRequestFail[]{fail1, fail2}));
                        } else {
                            throw new WebApplicationException(Response.Status.BAD_REQUEST);
                        }

                        return response;
                    }
                })).build();
    }
}
