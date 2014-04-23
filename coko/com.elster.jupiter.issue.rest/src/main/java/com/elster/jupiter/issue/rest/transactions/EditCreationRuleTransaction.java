package com.elster.jupiter.issue.rest.transactions;

import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplateParameter;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.transaction.Transaction;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Map;

public class EditCreationRuleTransaction implements Transaction<CreationRule> {

    private IssueService issueService;
    private IssueCreationService issueCreationService;
    private CreationRuleInfo request;

    public EditCreationRuleTransaction(IssueService issueService, IssueCreationService issueCreationService, CreationRuleInfo request) {
        this.issueService = issueService;
        this.issueCreationService = issueCreationService;
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
        checkRuleParameters(template);

        rule.setName(request.getName());
        rule.setComment(request.getComment());
        rule.setReason(issueReason);
        rule.setTemplateUuid(template.getUUID());
        rule.setDueInType(dueInType);
        rule.setDueInValue(dueValue);
        rule.setContent(template.getContent());
        addParameters(rule);
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
        Map<String, String> parameters = request.getParameters();
        rule.getParameters().clear();
        if (parameters != null) {
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                rule.addParameter(parameter.getKey(), parameter.getValue());
            }
        }
    }

    private void checkRuleParameters(CreationRuleTemplate template) {
        for (CreationRuleTemplateParameter parameter : template.getParameters()) {
            if (!parameter.isOptional() && (request.getParameters() == null || request.getParameters().get(parameter.getName()) == null)) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
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
