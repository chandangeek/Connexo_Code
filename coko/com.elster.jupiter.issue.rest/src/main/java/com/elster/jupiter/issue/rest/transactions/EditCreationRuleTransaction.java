package com.elster.jupiter.issue.rest.transactions;

import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class EditCreationRuleTransaction implements Transaction<CreationRule> {

    private IssueService issueService;
    private IssueCreationService issueCreationService;
    private IssueActionService issueActionService;
    private CreationRuleInfo request;

    public EditCreationRuleTransaction(IssueService issueService, IssueCreationService issueCreationService, IssueActionService issueActionService, CreationRuleInfo request) {
        this.issueService = issueService;
        this.issueCreationService = issueCreationService;
        this.issueActionService = issueActionService;
        this.request = request;
    }

    protected IssueService getIssueService() {
        return issueService;
    }

    protected IssueCreationService getIssueCreationService() {
        return issueCreationService;
    }

    protected CreationRuleInfo getRequest() {
        return request;
    }

    @Override
    public CreationRule perform() {
        CreationRule rule = getCreaionRule();
        IssueReason issueReason = getIssueReason();
        CreationRuleTemplate template = getCreationRuleTemplate();
        DueInType dueInType = getDueInType();
        long dueValue = getDueInValue();

        rule.setName(request.getName());
        rule.setComment(request.getComment());
        rule.setReason(issueReason);
        rule.setTemplateUuid(template.getUUID());
        rule.setDueInType(dueInType);
        rule.setDueInValue(dueValue);
        rule.setContent(template.getContent());
        addParameters(rule);
        addActions(rule);
        rule.validate();
        saveChanges(rule);
        updateRuleContent(rule);

        getIssueCreationService().reReadRules();

        return rule;
    }

    private void updateRuleContent(CreationRule rule){
        rule.updateContent();
        rule.update();
    }

    protected void saveChanges(CreationRule rule){
        // empty for edit creation rule
    }

    protected CreationRule getCreaionRule() {
        CreationRule rule = issueCreationService.findCreationRule(request.getId()).orNull();
        if (rule == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (rule.getVersion() != request.getVersion()){
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        return rule;
    }

    protected void addParameters(CreationRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Unable to add parameters to a null rule value");
        }
        Map<String, String> parameters = request.getParameters();
        rule.getParameters().clear();
        if (parameters != null) {
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                rule.addParameter(parameter.getKey(), parameter.getValue());
            }
        }
    }

    protected void addActions(CreationRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Unable to add actions to a null rule value");
        }
        List<CreationRuleActionInfo> actions = request.getActions();
        rule.getActions().clear();
        if (actions != null) {
            for (CreationRuleActionInfo action : actions) {
                Optional<IssueActionType> actionTypeRef = issueActionService.findActionType(action.getId());
                if (!actionTypeRef.isPresent()) {
                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
                }
                CreationRuleActionPhase phase = CreationRuleActionPhase.fromString(action.getPhase().getUuid());
                CreationRuleAction newAction = rule.addAction(actionTypeRef.get(), phase);
                addActionParameters(newAction, action);
            }
        }
    }

    private void addActionParameters(CreationRuleAction newAction, CreationRuleActionInfo action) {
        if (action.getParameters() != null) {
            for (Map.Entry <String, String> actionParam : action.getParameters().entrySet()) {
                newAction.addParameter(actionParam.getKey(), actionParam.getValue());
            }
        }
    }

    private long getDueInValue() {
        return request.getDueIn() != null ? request.getDueIn().getNumber() : 0;
    }

    private DueInType getDueInType() {
        DueInType dueInType = null;
        if (request.getDueIn() != null) {
            dueInType = DueInType.fromString(request.getDueIn().getType());
        }
        return dueInType;
    }

    private CreationRuleTemplate getCreationRuleTemplate() {
        CreationRuleTemplate template = issueCreationService.findCreationRuleTemplate(request.getTemplate().getUid()).orNull();
        if (template == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return template;
    }

    private IssueReason getIssueReason() {
        IssueReason issueReason = issueService.findReason(request.getReason().getId()).orNull();
        if (issueReason == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return issueReason;
    }
}
