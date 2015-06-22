package com.energyict.mdc.issue.datavalidation.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.rest.response.IssueCommentInfo;
import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionTypeInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/issues")
public class IssueResource {

    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final IssueDataValidationService issueDataValidationService;
    private final MeteringService meteringService;
    private final UserService userService;
    private final TransactionService transactionService;

    private final PropertyUtils propertyUtils;
    private final DataValidationIssueInfoFactory issueInfoFactory;
    private final CreationRuleActionInfoFactory actionInfoFactory;

    @Inject
    public IssueResource(IssueService issueService, IssueActionService issueActionService, IssueDataValidationService issueDataValidationService,
                         MeteringService meteringService, TransactionService transactionService, DataValidationIssueInfoFactory dataCollectionIssuesInfoFactory,
                         CreationRuleActionInfoFactory actionInfoFactory, PropertyUtils propertyUtils, UserService userService) {
        this.issueService = issueService;
        this.issueActionService = issueActionService;
        this.issueDataValidationService = issueDataValidationService;
        this.meteringService = meteringService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.issueInfoFactory = dataCollectionIssuesInfoFactory;
        this.actionInfoFactory = actionInfoFactory;
        this.propertyUtils = propertyUtils;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public PagedInfoList getDataValidationIssues(@BeanParam JsonQueryFilter queryFilter, @BeanParam JsonQueryParameters queryParams) {
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(buildFilterFromJsonQuery(queryFilter)).find();
        return PagedInfoList.fromPagedList("dataValidationIssues", issueInfoFactory.asInfo(issues), queryParams);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE, Privileges.ASSIGN_ISSUE, Privileges.CLOSE_ISSUE, Privileges.COMMENT_ISSUE, Privileges.ACTION_ISSUE})
    public Response getIssueById(@PathParam("id") long id) {
        IssueDataValidation issue = issueDataValidationService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueInfoFactory.asInfo(issue, DeviceInfo.class)).build();
    }

    @GET
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE, Privileges.ASSIGN_ISSUE, Privileges.CLOSE_ISSUE, Privileges.COMMENT_ISSUE, Privileges.ACTION_ISSUE})
    public PagedInfoList getComments(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        IssueDataValidation issue = issueDataValidationService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        Condition condition = where("issueId").isEqualTo(issue.getId());
        Query<IssueComment> query = issueService.query(IssueComment.class, User.class);
        List<IssueComment> commentsList = query.select(condition, Order.ascending("createTime"));
        List<IssueCommentInfo> infos = commentsList.stream().map(IssueCommentInfo::new).collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("comments", infos, queryParameters);
    }

    @POST
    @Path("/{id}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.COMMENT_ISSUE)
    public Response postComment(@PathParam("id") long id, CreateCommentRequest request, @Context SecurityContext securityContext) {
        try (TransactionContext context = transactionService.getContext()) {
            IssueDataValidation issue = issueDataValidationService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
            User author = (User) securityContext.getUserPrincipal();
            IssueComment comment = issue.addComment(request.getComment(), author).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
            context.commit();
            return Response.ok(new IssueCommentInfo(comment)).status(Response.Status.CREATED).build();
        }
    }

    private DataValidationIssueFilter buildFilterFromJsonQuery(JsonQueryFilter jsonQueryFilter) {
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
//        if (jsonQueryFilter.hasProperty("status")) {
//            jsonQueryFilter.getStringList("status").stream()
//                    .flatMap(s -> issueService.findStatus(s)
//                            .map(status -> Stream.of(status))
//                            .orElse(Stream.empty()))
//                    .forEach(status -> filter.addStatus(status));
//        }
//        if (jsonQueryFilter.hasProperty("reason")) {
//            String reason = jsonQueryFilter.getString("reason");
//            issueService.findReason(reason).ifPresent(r -> filter.setIssueReason(r));
//        }
//        if (jsonQueryFilter.hasProperty("meter")) {
//            meteringService.findEndDevice(jsonQueryFilter.getLong("meter")).ifPresent(m -> filter.setDevice(m));
//        }
//        if (jsonQueryFilter.hasProperty("assigneeType") && jsonQueryFilter.hasProperty("assigneeId")) {
//            issueService.findIssueAssignee(AssigneeType.USER, jsonQueryFilter.getLong("assigneeId")).ifPresent(a -> filter.setAssignee());
//        }
        return filter;
    }

    private List<? extends IssueDataValidation> getFilteredIssues(JsonQueryParameters queryParams) {

//        Class<? extends IssueDataValidation> apiClass = getQueryApiClass(params);
//        Class<? extends Issue> eagerClass = getEagerApiClass(apiClass);
//        Query<? extends IssueDataValidation> query = getIssueDataCollectionService().query(apiClass, eagerClass, EndDevice.class, User.class, IssueReason.class,
//                IssueStatus.class, IssueType.class);
//        Condition condition = getQueryCondition(params);
//        return query.select(condition, params.getFrom(), params.getTo(), params.getOrder("baseIssue."));
        return Collections.emptyList();
    }
//
//    private List<? extends IssueDataCollection> getIssuesForBulk(StandardParametersBean params) {
//        Condition condition = getQueryCondition(params);
//        Query<OpenIssueDataCollection> openQuery = getIssueDataCollectionService().query(OpenIssueDataCollection.class, OpenIssue.class, EndDevice.class, User.class, IssueReason.class,
//                IssueStatus.class, IssueType.class);
//        Query<HistoricalIssueDataCollection> closeQuery = getIssueDataCollectionService().query(HistoricalIssueDataCollection.class, HistoricalIssue.class, EndDevice.class, User.class, IssueReason.class,
//                IssueStatus.class, IssueType.class);
//        List<IssueDataCollection> issues = new ArrayList<>();
//        issues.addAll(openQuery.select(condition));
//        issues.addAll(closeQuery.select(condition));
//        return issues;
//    }
//

    @GET
    @Path("/{id}/actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public PagedInfoList getActions(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        IssueDataValidation issue = issueDataValidationService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        Query<IssueActionType> query = issueService.query(IssueActionType.class, IssueType.class);

        IssueReason reason = issue.getReason();
        IssueType type = reason.getIssueType();

        Condition c0 = where("issueType").isNull();
        Condition c1 = where("issueType").isEqualTo(type).and(where("issueReason").isNull());
        Condition c2 = where("issueType").isEqualTo(type).and(where("issueReason").isEqualTo(reason));
        Condition condition = (c0).or(c1).or(c2);

        List<CreationRuleActionTypeInfo> infos = query.select(condition).stream()
                               .filter(actionType -> actionType.createIssueAction()
                                           .map(action -> action.isApplicable(issue))
                                           .orElse(false))
                               .map(actionInfoFactory::asInfo)
                               .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("issueActions", infos, queryParameters);
    }
//
//    @PUT
//    @Path("/assign")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    @RolesAllowed(Privileges.ASSIGN_ISSUE)
//    @Deprecated
//    public Response assignIssues(AssignIssueRequest request, @Context SecurityContext securityContext, @BeanParam StandardParametersBean params) {
//        /* TODO this method should be refactored when FE implements dynamic actions for bulk operations */
//        User performer = (User) securityContext.getUserPrincipal();
//        Function<ActionInfo, List<? extends Issue>> issueProvider = null;
//        if (request.allIssues) {
//            issueProvider = bulkResults -> getIssuesForBulk(params);
//        } else {
//            issueProvider = bulkResult -> getUserSelectedIssues(request, bulkResult);
//        }
//        ActionInfo info = getTransactionService().execute(new AssignIssueTransaction(request, performer, issueProvider));
//        return entity(info).build();
//    }
//
//    private List<? extends Issue> getUserSelectedIssues(BulkIssueRequest request, ActionInfo bulkResult) {
//        List<Issue> issuesForBulk = new ArrayList<>(request.issues.size());
//        for (EntityReference issueRef : request.issues) {
//            Issue issue = getIssueDataCollectionService().findOpenIssue(issueRef.getId()).orElse(null);
//            if (issue == null) {
//                issue = getIssueDataCollectionService().findHistoricalIssue(issueRef.getId()).orElse(null);
//            }
//            if (issue == null) {
//                bulkResult.addFail(getString(MessageSeeds.ISSUE_DOES_NOT_EXIST, getThesaurus()), issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
//            } else {
//                issuesForBulk.add(issue);
//            }
//        }
//        return issuesForBulk;
//    }
//
//    @PUT
//    @Path("/close")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed(Privileges.CLOSE_ISSUE)
//    @Deprecated
//    public Response closeIssues(CloseIssueRequest request, @Context SecurityContext securityContext, @BeanParam StandardParametersBean params) {
//        /* TODO this method should be refactored when FE implements dynamic actions for bulk operations */
//        User performer = (User) securityContext.getUserPrincipal();
//        Function<ActionInfo, List<? extends Issue>> issueProvider = null;
//        if (request.allIssues) {
//            issueProvider = bulkResults -> getIssuesForBulk(params);
//        } else {
//            issueProvider = bulkResult -> getUserSelectedIssues(request, bulkResult);
//        }
//        return entity(doBulkClose(request, performer, issueProvider)).build();
//    }
//
//    private ActionInfo doBulkClose(CloseIssueRequest request, User performer, Function<ActionInfo, List<? extends Issue>> issueProvider) {
//        ActionInfo response = new ActionInfo();
//        try (TransactionContext context = getTransactionService().getContext()) {
//            Optional<IssueStatus> status = getIssueService().findStatus(request.status);
//            if (status.isPresent() && status.get().isHistorical()) {
//                for (Issue issue : issueProvider.apply(response)) {
//                    if (issue.getStatus().isHistorical()) {
//                        response.addFail(getString(MessageSeeds.ISSUE_ALREADY_CLOSED, getThesaurus()), issue.getId(), issue.getTitle());
//                    } else {
//                        issue.addComment(request.comment, performer);
//                        if (issue instanceof OpenIssue) {
//                            ((OpenIssue) issue).close(status.get());
//                        } else {
//                            // user set both open and close statuses in filter
//                            getIssueDataCollectionService().findOpenIssue(issue.getId()).ifPresent(
//                                    openIssue -> openIssue.close(status.get())
//                            );
//                        }
//                        response.addSuccess(issue.getId());
//                    }
//                }
//            } else {
//                throw new WebApplicationException(Response.Status.BAD_REQUEST);
//            }
//            context.commit();
//        }
//        return response;
//    }
//
    @GET
    @Path("/{id}/actions/{actionId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getActionTypeById(@PathParam("id") long id, @PathParam("actionId") long actionId){
        issueDataValidationService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        IssueActionType actionType = issueActionService.findActionType(actionId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(actionInfoFactory.asInfo(actionType)).build();
    }

    @PUT
    @Path("/{id}/actions/{actionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ACTION_ISSUE)
    public Response performAction(@PathParam("id") long id, @PathParam("actionId") long actionId, PerformActionRequest request) {
        IssueDataValidation issue = issueDataValidationService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        IssueActionType action = issueActionService.findActionType(actionId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        Map<String, Object> properties = new HashMap<>();
        for (PropertySpec propertySpec : action.createIssueAction().get().getPropertySpecs()) {
            Object value = propertyUtils.findPropertyValue(propertySpec, request.properties);
            if (value != null) {
                properties.put(propertySpec.getName(), value);
            }
        }
        IssueActionResult actionResult;
        try (TransactionContext context = transactionService.getContext()) {
            actionResult = issueActionService.executeAction(action, issue, properties);
            context.commit();
        }
        return Response.ok(actionResult).build();
    }
}