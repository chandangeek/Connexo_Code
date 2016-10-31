package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.rest.MessageSeeds;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.BulkIssueRequest;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.rest.resource.IssueResourceHelper;
import com.elster.jupiter.issue.rest.resource.IssueRestModuleConst;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.IssueGroupInfo;
import com.elster.jupiter.issue.rest.response.IssueInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.IssueActionTypeInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfoFactoryService;
import com.elster.jupiter.issue.rest.transactions.AssignIssueTransaction;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.request.RequestHelper.KEY;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

@Path("/issues")
public class IssueResource extends BaseResource {

    private final IssueResourceHelper issueResourceHelper;
    private final IssueInfoFactory issueInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final IssueInfoFactoryService issueInfoFactoryService;

    @Inject
    public IssueResource(IssueResourceHelper issueResourceHelper, IssueInfoFactory issueInfoFactory, ConcurrentModificationExceptionFactory conflictFactory, IssueInfoFactoryService issueInfoFactoryService) {
        this.issueResourceHelper = issueResourceHelper;
        this.issueInfoFactory = issueInfoFactory;
        this.conflictFactory = conflictFactory;
        this.issueInfoFactoryService = issueInfoFactoryService;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getAllIssues(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParams, @BeanParam JsonQueryFilter filter) {
        validateMandatory(params, START, LIMIT);
        Finder<? extends Issue> finder = getIssueService().findIssues(issueResourceHelper.buildFilterFromQueryParameters(filter));
        addSorting(finder, params);
        if (queryParams.getStart().isPresent() && queryParams.getLimit().isPresent()) {
            finder.paged(queryParams.getStart().get(), queryParams.getLimit().get());
        }
        List<? extends Issue> issues = finder.find();
        List<IssueInfo> issueInfos = new ArrayList<>();
        for(Issue baseIssue : issues) {
            for (IssueProvider issueProvider : getIssueService().getIssueProviders()) {
                Optional<? extends Issue> issueRef = issueProvider.findIssue(baseIssue.getId());
               if (issueRef.isPresent()) {
                    issueInfos.add(IssueInfo.class.cast(issueInfoFactoryService.getInfoFactoryFor(issueRef.get()).from(issueRef.get())));
                }
            }
        }
        return PagedInfoList.fromPagedList("data", issueInfos, queryParams);
    }

    @GET @Transactional
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getComments(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        Issue issue = getIssueService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return PagedInfoList.fromCompleteList("comments", issueResourceHelper.getIssueComments(issue), queryParameters);
    }

    @POST
    @Transactional
    @Path("/{id}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.COMMENT_ISSUE)
    public Response postComment(@PathParam("id") long id, CreateCommentRequest request, @Context SecurityContext securityContext) {
        Issue issue = getIssueService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueResourceHelper.postComment(issue, request, securityContext)).status(Response.Status.CREATED).build();
    }

    @GET @Transactional
    @Path("/{" + ID + "}/actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getActions(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        Issue issue = getIssueService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<IssueActionTypeInfo> issueActions = new ArrayList<>();
        for (IssueProvider issueProvider : getIssueService().getIssueProviders()) {
            Optional<? extends Issue> issueRef =  issue.getStatus().isHistorical() ?
                    issueProvider.getHistoricalIssue((HistoricalIssue)issue) : issueProvider.getOpenIssue((OpenIssue)issue);
            if (issueRef.isPresent()) {
                issueActions = issueResourceHelper.getListOfAvailableIssueActions(issueRef.get());
            }
        }
        return PagedInfoList.fromCompleteList("issueActions", issueActions, queryParameters);
    }

    @GET @Transactional
    @Path("/{" + ID + "}/actions/{" + KEY + "}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public Response getActionTypeById(@PathParam(ID) long id, @PathParam(KEY) long actionId){
        getIssueService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueResourceHelper.getIssueActionById(actionId)).build();
    }

    @PUT @Transactional
    @Path("/{" + ID + "}/actions/{" + KEY + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response performAction(@PathParam(ID) long id, @PathParam(KEY) long actionId, PerformActionRequest request) {
        Issue baseIssue = getIssueService().findAndLockIssueByIdAndVersion(id, request.issue.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.issue.title)
                        .withActualVersion(() -> getIssueService().findIssue(id)
                                .map(Issue::getVersion)
                                .orElse(null))
                        .supplier());
        request.id = actionId;

        for (IssueProvider issueProvider : getIssueService().getIssueProviders()) {
            Optional<? extends Issue> issueRef =  baseIssue.getStatus().isHistorical() ?
                    issueProvider.getHistoricalIssue((HistoricalIssue)baseIssue) : issueProvider.getOpenIssue((OpenIssue)baseIssue);
            if (issueRef.isPresent()) {
                return Response.ok(issueResourceHelper.performIssueAction(issueRef.get(), request)).build();
            }
        }
        return Response.ok(issueResourceHelper.performIssueAction(baseIssue, request)).build();
    }


    @GET
    @Path("/groupedlist")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getGroupedList(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        IssueGroupFilter groupFilter = getIssueService().newIssueGroupFilter();
        groupFilter.using(getQueryApiClass(filter)) // Issues, Historical Issues or Both
              .onlyGroupWithKey(filter.getString(IssueRestModuleConst.REASON))  // Reason id
              .withIssueTypes(filter.getStringList(IssueRestModuleConst.ISSUE_TYPE)) // Reasons only with specific issue type
              .withStatuses(filter.getStringList(IssueRestModuleConst.STATUS)) // All selected statuses
              .withMeterMrid(filter.getString(IssueRestModuleConst.METER)) // Filter by meter MRID
              .groupBy(filter.getString(IssueRestModuleConst.FIELD)) // Main grouping column
              .setAscOrder(false) // Sorting (descending direction)
              .from(params.getFrom()).to(params.getTo()); // Pagination
        issueResourceHelper.getDueDates(filter).stream().forEach(dd -> groupFilter.withDueDate(dd.startTime, dd.endTime));
        List<IssueGroup> resultList = getIssueService().getIssueGroupList(groupFilter);
        List<IssueGroupInfo> infos = resultList.stream().map(IssueGroupInfo::new).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("issueGroups", infos, queryParameters);
    }

    @PUT
    @Path("/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ASSIGN_ISSUE)
    @Deprecated
    public Response assignIssues(AssignIssueRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter) {
        /* TODO this method should be refactored when FE implements dynamic actions for bulk operations */
        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, List<? extends Issue>> issueProvider;
        if (request.allIssues) {
            issueProvider = bulkResults -> getIssuesForBulk(filter);
        } else {
            issueProvider = bulkResult -> getUserSelectedIssues(request, bulkResult);
        }
        ActionInfo info = getTransactionService().execute(new AssignIssueTransaction(request, performer, issueProvider));
        return entity(info).build();
    }

    private List<? extends Issue> getIssuesForBulk(JsonQueryFilter filter) {
        return getIssueService().findIssues(issueResourceHelper.buildFilterFromQueryParameters(filter)).stream()
                .map(issue -> {
                    if (issue.getStatus().isHistorical()) {
                        return getIssueService().findHistoricalIssue(issue.getId());
                    } else {
                        return getIssueService().findOpenIssue(issue.getId());
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<? extends Issue> getUserSelectedIssues(BulkIssueRequest request, ActionInfo bulkResult) {
        List<Issue> issuesForBulk = new ArrayList<>(request.issues.size());
        for (EntityReference issueRef : request.issues) {
            Optional<? extends Issue> issue = getIssueService().findIssue(issueRef.getId());
            if (issue.isPresent()) {
                issuesForBulk.add(issue.get());
            } else {
                bulkResult.addFail(getThesaurus().getFormat(MessageSeeds.ISSUE_DOES_NOT_EXIST).format(), issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
            }
        }
        return issuesForBulk;
    }

    private Class<? extends Issue> getQueryApiClass(JsonQueryFilter filter) {
        List<IssueStatus> statuses = filter.hasProperty(IssueRestModuleConst.STATUS)
                ? filter.getStringList(IssueRestModuleConst.STATUS).stream().map(s -> getIssueService().findStatus(s).get()).collect(Collectors.toList())
                : Collections.EMPTY_LIST;
        if (statuses.isEmpty()) {
            return Issue.class;
        }
        if (statuses.stream().allMatch(status -> !status.isHistorical())) {
            return OpenIssue.class;
        }
        if (statuses.stream().allMatch(IssueStatus::isHistorical)) {
            return HistoricalIssue.class;
        }
        return Issue.class;
    }

    private Finder<? extends Issue> addSorting(Finder<? extends Issue> finder, StandardParametersBean parameters) {
        Order[] orders = parameters.getOrder("");
        for(Order order : orders) {
            finder.sorted(order.getName(), order.ascending());
        }
        return finder;
    }
}
