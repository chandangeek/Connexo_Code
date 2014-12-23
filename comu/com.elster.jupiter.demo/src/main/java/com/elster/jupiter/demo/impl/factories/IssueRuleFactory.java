package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;

import javax.inject.Inject;
import java.util.Optional;

public class IssueRuleFactory extends NamedFactory<IssueRuleFactory, CreationRule> {
    private final IssueCreationService issueCreationService;
    private final IssueService issueService;
    private final Store store;

    private String type;
    private String reason;

    @Inject
    public IssueRuleFactory(Store store, IssueCreationService issueCreationService, IssueService issueService) {
        super(IssueRuleFactory.class);
        this.store = store;
        this.issueCreationService = issueCreationService;
        this.issueService = issueService;
    }

    public IssueRuleFactory withType(String type){
        this.type = type;
        return this;
    }

    public IssueRuleFactory withReason(String reason){
        this.reason = reason;
        return this;
    }

    public CreationRule get(){
        Log.write(this);
        CreationRule rule = issueCreationService.createRule();
        rule.setName(getName());
        rule.setReason(getReasonForRule());
        rule.setTemplateUuid(Constants.CreationRuleTemplate.BASIC_DATA_COLLECTION_UUID);
        rule.setDueInType(DueInType.MONTH);
        rule.setDueInValue(1);
        rule.setContent(getCreationRuleTemplate().getContent());
        rule.addParameter("eventType", type);
        rule.validate();
        rule.save();
        store.add(CreationRule.class, rule);
        return rule;
    }

    private com.elster.jupiter.issue.share.entity.IssueReason getReasonForRule() {
        Optional<com.elster.jupiter.issue.share.entity.IssueReason> reasonRef = issueService.findReason(this.reason);
        if (!reasonRef.isPresent()){
            throw new UnableToCreate("Unable to find reason with key = " + this.reason);
        }
        return reasonRef.get();
    }

    private CreationRuleTemplate getCreationRuleTemplate() {
        CreationRuleTemplate template = issueCreationService.findCreationRuleTemplate(Constants.CreationRuleTemplate.BASIC_DATA_COLLECTION_UUID).orElse(null);
        if (template == null) {
            throw new UnableToCreate("Unable to find creation rule template with id = " + Constants.CreationRuleTemplate.BASIC_DATA_COLLECTION_UUID);
        }
        return template;
    }
}
