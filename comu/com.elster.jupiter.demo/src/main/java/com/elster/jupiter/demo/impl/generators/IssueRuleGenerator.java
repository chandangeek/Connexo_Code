package com.elster.jupiter.demo.impl.generators;

import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;

import javax.inject.Inject;
import java.util.Optional;

public class IssueRuleGenerator {
    private static final String ISSUE_REASON_KEY = "reason.connection.failed";
    private static final String BASIC_DATA_COLLECTION_UUID = "e29b-41d4-a716";

    private final IssueCreationService issueCreationService;
    private final IssueService issueService;
    private final Store store;

    private String name;

    @Inject
    public IssueRuleGenerator(Store store, IssueCreationService issueCreationService, IssueService issueService) {
        this.store = store;
        this.issueCreationService = issueCreationService;
        this.issueService = issueService;
    }

    public IssueRuleGenerator withName(String name){
        this.name = name;
        return this;
    }

    public void create(){
        System.out.println("==> Creating issue creation rule " + name + "...");
        CreationRule rule = issueCreationService.createRule();
        rule.setName(this.name);
        rule.setReason(getReasonForRule());
        rule.setTemplateUuid(BASIC_DATA_COLLECTION_UUID);
        rule.setDueInType(DueInType.MONTH);
        rule.setDueInValue(1);
        rule.setContent(getCreationRuleTemplate().getContent());
        rule.addParameter("eventType", "CONNECTION_LOST");
        rule.validate();
        rule.save();
        store.add(CreationRule.class, rule);
    }

    private IssueReason getReasonForRule() {
        Optional<IssueReason> reasonRef = issueService.findReason(ISSUE_REASON_KEY);
        if (!reasonRef.isPresent()){
            throw new UnableToCreate("Unable to find reason with key = " + ISSUE_REASON_KEY);
        }
        return reasonRef.get();
    }

    private CreationRuleTemplate getCreationRuleTemplate() {
        CreationRuleTemplate template = issueCreationService.findCreationRuleTemplate(BASIC_DATA_COLLECTION_UUID).orElse(null);
        if (template == null) {
            throw new UnableToCreate("Unable to find creation rule template with id = " + BASIC_DATA_COLLECTION_UUID);
        }
        return template;
    }
}
