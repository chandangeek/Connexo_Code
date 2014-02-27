package com.elster.jupiter.issue.rest.controller;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.Issue;
import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.issue.rest.response.*;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.impl.MeterImpl;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/issue")
public class    GeneralViewController {
    private final RestQueryService queryService;
    private final IssueService issueService;
    private final TransactionService transactionService;


    @Inject
    public GeneralViewController(RestQueryService queryService, IssueService issueService, TransactionService transactionService) {
        this.queryService = queryService;
        this.issueService = issueService;
        this.transactionService = transactionService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllIssues(@Context UriInfo uriInfo) {
        IssueList resultList;
        try {
            QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
            resultList = queryIssueList(true, params);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok().entity(resultList).build();
    }

    //TODO delete when events will be produced by MDC
    @GET
    @Path("/event")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getEvent() {
        issueService.getEvent();
        return true;
    }
    // END delete when events will be produced by MDC

    @GET
    @Path("/groupedlist")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getGroupedList(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        Map<String, Long> resultMap = Collections.EMPTY_MAP;
        if (params.get("reason") != null) {
            try (TransactionContext context = transactionService.getContext()) {
                resultMap = issueService.getIssueGroupList(params.get("reason").get(0), false, params.getStart(), params.getLimit());
                context.commit();
            }
        }
        IssueGroupList resultList = new IssueGroupList();
        for(Map.Entry<String, Long> groupEntry : resultMap.entrySet()) {
            IssueGroupListRow rIssue = new IssueGroupListRow();
            rIssue.setReason(groupEntry.getKey());
            rIssue.setNumber(groupEntry.getValue());
            resultList.add(rIssue);
        }
        long total = params.getStart() + resultMap.size();
        if (resultMap.size() == params.getLimit()){
            total++;
        }
        resultList.setTotal(total);
        return resultList;
    }

    private IssueList queryIssueList(boolean maySeeAny, QueryParameters queryParameters) {
        Query<Issue> query = issueService.getIssueListQuery();
        query.setEager();
        Condition condition = null;
        if (queryParameters.get("reason") != null) {
            condition = where("reason.name").isEqualTo(queryParameters.get("reason").get(0));
        }
        List<String> sortList = queryParameters.get("sort");
        List<Order> orders = convertSortListToOrderList(sortList, queryParameters);
        List<Issue> list = new LinkedList<Issue>();
        if (sortList != null && condition != null) {
            list = query.select(condition, queryParameters.getStart(), queryParameters.getLimit(), orders.toArray(new Order[orders.size()]));
        } else if (condition != null) {
            list = query.select(condition, queryParameters.getStart(), queryParameters.getLimit());
        } else if (sortList != null) {
            RestQuery<Issue> restQuery = queryService.wrap(query);
            list = restQuery.select(queryParameters, orders.toArray(new Order[orders.size()]));
        } else {
            RestQuery<Issue> restQuery = queryService.wrap(query);
            list = restQuery.select(queryParameters);
        }
        IssueList resultList = new IssueList(list, queryParameters.getStart(), queryParameters.getLimit());
        return resultList;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public IssueInfo getIssueById(@PathParam("id") long id) {
        Optional<Issue> issue = issueService.getIssueById(id);
        if (!issue.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return new IssueInfo(issue.get());
    }

    private List<Order> convertSortListToOrderList(List<String> sortList, QueryParameters queryParameters) {
        List<Order> orders = new ArrayList<Order>();
        if (sortList != null) {
            List<String> orderList = queryParameters.get("order");
            if ((orderList == null) || (sortList.size() == orderList.size())) {
                for (int i = 0; i < sortList.size(); i++) {
                    if (orderList != null) {
                        if ("DESC".equalsIgnoreCase(orderList.get(i))) {
                            orders.add(Order.descending(sortList.get(i)));
                        } else {
                            orders.add(Order.ascending(sortList.get(i)));
                        }
                    } else {
                        orders.add(Order.ascending(sortList.get(i)));
                    }
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        return orders;
    }
}