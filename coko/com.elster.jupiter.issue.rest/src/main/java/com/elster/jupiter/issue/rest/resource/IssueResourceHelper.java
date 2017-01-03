package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.MessageSeeds;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.request.IssueDueDateInfo;
import com.elster.jupiter.issue.rest.request.IssueDueDateInfoAdapter;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.rest.response.IssueActionInfoFactory;
import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.rest.response.IssueAssigneeInfoAdapter;
import com.elster.jupiter.issue.rest.response.IssueCommentInfo;
import com.elster.jupiter.issue.rest.response.cep.IssueActionTypeInfo;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

public class IssueResourceHelper {

    private final TransactionService transactionService;
    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final IssueActionInfoFactory actionInfoFactory;
    private final PropertyValueInfoService propertyValueInfoService;
    private final Thesaurus thesaurus;
    private final SecurityContext securityContext;
    private final MeteringService meteringService;
    private final UserService userService;

    @Inject
    public IssueResourceHelper(TransactionService transactionService, IssueService issueService, IssueActionService issueActionService, MeteringService meteringService, UserService userService,
                               IssueActionInfoFactory actionFactory, PropertyValueInfoService propertyValueInfoService, Thesaurus thesaurus, @Context SecurityContext securityContext) {
        this.transactionService = transactionService;
        this.issueService = issueService;
        this.issueActionService = issueActionService;
        this.actionInfoFactory = actionFactory;
        this.propertyValueInfoService = propertyValueInfoService;
        this.thesaurus = thesaurus;
        this.securityContext = securityContext;
        this.meteringService = meteringService;
        this.userService = userService;
    }

    public List<IssueActionTypeInfo> getListOfAvailableIssueActions(Issue issue) {
        return getListOfAvailableIssueActionsTypes(issue).stream().map(actionType -> actionInfoFactory.asInfo(issue, actionType)).collect(Collectors.toList());
        //return getListOfAvailableIssueActionsTypes(issue).stream().map(k -> new actionInfoFactory::asInfo(issue, k)).collect(Collectors.toList());
    }

    public List<IssueActionType> getListOfAvailableIssueActionsTypes(Issue issue) {
        User user = ((User) securityContext.getUserPrincipal());
        Query<IssueActionType> query = issueService.query(IssueActionType.class, IssueType.class);
        IssueReason reason = issue.getReason();
        IssueType type = reason.getIssueType();

        Condition c0 = where("issueType").isNull();
        Condition c1 = where("issueType").isEqualTo(type).and(where("issueReason").isNull());
        Condition c2 = where("issueType").isEqualTo(type).and(where("issueReason").isEqualTo(reason));
        Condition condition = (c0).or(c1).or(c2);

        return query.select(condition).stream()
                .filter(actionType -> actionType.createIssueAction()
                        .map(action -> action.isApplicable(issue) && action.isApplicableForUser(user))
                        .orElse(false))
                .collect(Collectors.toList());
    }

    public IssueActionTypeInfo getIssueActionById(long actionId) {
        return getIssueActionById(null, actionId);
    }

    public IssueActionTypeInfo getIssueActionById(Issue issue, long actionId) {
        IssueActionType actionType = issueActionService.findActionType(actionId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return actionInfoFactory.asInfo(issue, actionType);
    }

    public IssueActionResult performIssueAction(Issue issue, PerformActionRequest request) {
        IssueActionType action = issueActionService.findActionType(request.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<PropertySpec> propertySpecs = action.createIssueAction().get().setIssue(issue).getPropertySpecs();
        Map<String, Object> properties = new HashMap<>();
        if(propertySpecs!=null && !propertySpecs.isEmpty()){
            for (PropertySpec propertySpec : propertySpecs) {
                Object value = propertyValueInfoService.findPropertyValue(propertySpec, request.properties);
                if (value != null) {
                    properties.put(propertySpec.getName(), value);
                }
            }
        }
        return issueActionService.executeAction(action, issue, properties);
    }

    public List<IssueCommentInfo> getIssueComments(Issue issue) {
        Condition condition = where("issueId").isEqualTo(issue.getId());
        Query<IssueComment> query = issueService.query(IssueComment.class, User.class);
        List<IssueComment> commentsList = query.select(condition, Order.ascending("createTime"));
        return commentsList.stream().map(IssueCommentInfo::new).collect(Collectors.toList());
    }

    public IssueCommentInfo postComment(Issue issue, CreateCommentRequest request, SecurityContext securityContext) {
        User author = (User) securityContext.getUserPrincipal();
        IssueComment comment = issue.addComment(request.getComment(), author).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        return new IssueCommentInfo(comment);
    }

    public IssueFilter buildFilterFromQueryParameters(JsonQueryFilter jsonFilter) {
        IssueFilter filter = issueService.newIssueFilter();
        if (jsonFilter.hasProperty(IssueRestModuleConst.ID)) {
            filter.setIssueId(jsonFilter.getString(IssueRestModuleConst.ID));
        }
        jsonFilter.getStringList(IssueRestModuleConst.STATUS).stream()
                .flatMap(s -> issueService.findStatus(s).map(Stream::of).orElse(Stream.empty()))
                .forEach(filter::addStatus);
        if (jsonFilter.hasProperty(IssueRestModuleConst.REASON)) {
            issueService.findReason(jsonFilter.getString(IssueRestModuleConst.REASON))
                    .ifPresent(filter::setIssueReason);
        }
        if (jsonFilter.hasProperty(IssueRestModuleConst.METER)) {
            meteringService.findEndDeviceByName(jsonFilter.getString(IssueRestModuleConst.METER))
                    .ifPresent(filter::addDevice);
        }

        if(jsonFilter.getLongList(IssueRestModuleConst.ASSIGNEE).stream().allMatch(s-> s == null)){
            jsonFilter.getStringList(IssueRestModuleConst.ASSIGNEE).stream().map(id -> userService.getUser(Long.valueOf(id)).orElse(null))
                    .filter(user -> user != null)
                    .forEach(filter::addAssignee);
            if(jsonFilter.getStringList(IssueRestModuleConst.ASSIGNEE).stream().anyMatch(id -> id.equals("-1"))){
                filter.setUnassignedSelected();
            }
        }else {
            jsonFilter.getLongList(IssueRestModuleConst.ASSIGNEE)
                    .stream().map(id -> userService.getUser(id).orElse(null))
                    .filter(user -> user != null)
                    .forEach(filter::addAssignee);
            if (jsonFilter.getLongList(IssueRestModuleConst.ASSIGNEE).stream().anyMatch(id -> id == -1L)) {
                filter.setUnassignedSelected();
            }
        }


        if(jsonFilter.getLongList(IssueRestModuleConst.WORKGROUP).stream().allMatch(s-> s == null)){
            jsonFilter.getStringList(IssueRestModuleConst.WORKGROUP).stream().map(id -> userService.getWorkGroup(Long.valueOf(id)).orElse(null))
                    .filter(workGroup -> workGroup != null)
                    .forEach(filter::addWorkGroupAssignee);
            if(jsonFilter.getStringList(IssueRestModuleConst.WORKGROUP).stream().anyMatch(id -> id.equals("-1"))){
                filter.setUnassignedWorkGroupSelected();
            }
        }else{
            jsonFilter.getLongList(IssueRestModuleConst.WORKGROUP)
                    .stream().map(id -> userService.getWorkGroup(id).orElse(null))
                    .filter(workGroup -> workGroup != null)
                    .forEach(filter::addWorkGroupAssignee);
            if(jsonFilter.getLongList(IssueRestModuleConst.WORKGROUP).stream().anyMatch(id -> id == -1L)){
                filter.setUnassignedWorkGroupSelected();
            }
        }

        jsonFilter.getStringList(IssueRestModuleConst.ISSUE_TYPE).stream()
                .flatMap(it -> issueService.findIssueType(it).map(Stream::of).orElse(Stream.empty()))
                .forEach(filter::addIssueType);
        getDueDates(jsonFilter).stream().forEach(dd -> filter.addDueDate(dd.startTime, dd.endTime));
        return filter;
    }

    public List<IssueDueDateInfo> getDueDates(JsonQueryFilter filter) {
        IssueDueDateInfoAdapter issueDueDateInfoAdapter = new IssueDueDateInfoAdapter();
        return filter.getStringList(IssueRestModuleConst.DUE_DATE).stream().map(dd -> {
            try {
                return issueDueDateInfoAdapter.unmarshal(dd);
            } catch (Exception ex){
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "filter");
            }
        }).collect(Collectors.toList());
    }

    public List<IssueAssigneeInfo> getAssignees(JsonQueryFilter filter) {
        IssueAssigneeInfoAdapter issueAssigneeInfoAdapter = new IssueAssigneeInfoAdapter();
        return filter.getStringList(IssueRestModuleConst.ASSIGNEE).stream().map(ai -> {
            try {
                return issueAssigneeInfoAdapter.unmarshal(ai);
            } catch (Exception ex){
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "filter");
            }
        }).collect(Collectors.toList());
    }
}
