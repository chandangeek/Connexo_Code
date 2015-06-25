package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.rest.response.IssueActionInfoFactory;
import com.elster.jupiter.issue.rest.response.IssueCommentInfo;
import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.rest.response.cep.IssueActionTypeInfo;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class IssueResourceHelper {

    private final TransactionService transactionService;
    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final IssueActionInfoFactory actionInfoFactory;
    private final PropertyUtils propertyUtils;
    private final Thesaurus thesaurus;

    @Inject
    public IssueResourceHelper(TransactionService transactionService, IssueService issueService, IssueActionService issueActionService, IssueActionInfoFactory actionFactory, PropertyUtils propertyUtils, Thesaurus thesaurus) {
        this.transactionService = transactionService;
        this.issueService = issueService;
        this.issueActionService = issueActionService;
        this.actionInfoFactory = actionFactory;
        this.propertyUtils = propertyUtils;
        this.thesaurus = thesaurus;
    }

    public List<IssueActionTypeInfo> getListOfAvailableIssueActions(Issue issue) {
        Query<IssueActionType> query = issueService.query(IssueActionType.class, IssueType.class);
        IssueReason reason = issue.getReason();
        IssueType type = reason.getIssueType();

        Condition c0 = where("issueType").isNull();
        Condition c1 = where("issueType").isEqualTo(type).and(where("issueReason").isNull());
        Condition c2 = where("issueType").isEqualTo(type).and(where("issueReason").isEqualTo(reason));
        Condition condition = (c0).or(c1).or(c2);

        return query.select(condition).stream()
                .filter(actionType -> actionType.createIssueAction()
                        .map(action -> action.isApplicable(issue))
                        .orElse(false))
                .map(actionInfoFactory::asInfo)
                .collect(Collectors.toList());
    }

    public IssueActionTypeInfo getIssueActionById(long actionId) {
        IssueActionType actionType = issueActionService.findActionType(actionId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return actionInfoFactory.asInfo(actionType);
    }

    public IssueActionResult performIssueAction(Issue issue, PerformActionRequest request) {
        IssueActionType action = issueActionService.findActionType(request.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
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
        return actionResult;
    }

    public List<IssueCommentInfo> getIssueComments(Issue issue) {
        Condition condition = where("issueId").isEqualTo(issue.getId());
        Query<IssueComment> query = issueService.query(IssueComment.class, User.class);
        List<IssueComment> commentsList = query.select(condition, Order.ascending("createTime"));
        return commentsList.stream().map(IssueCommentInfo::new).collect(Collectors.toList());
    }

    public IssueCommentInfo postComment(Issue issue, CreateCommentRequest request, SecurityContext securityContext) {
        try (TransactionContext context = transactionService.getContext()) {
            User author = (User) securityContext.getUserPrincipal();
            IssueComment comment = issue.addComment(request.getComment(), author).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
            context.commit();
            return new IssueCommentInfo(comment);
        }
    }
}
