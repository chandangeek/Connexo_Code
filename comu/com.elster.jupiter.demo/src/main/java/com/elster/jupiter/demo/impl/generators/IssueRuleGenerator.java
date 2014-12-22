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

public class IssueRuleGenerator extends NamedGenerator<IssueRuleGenerator> {
    private static final String ISSUE_REASON_KEY = "reason.connection.failed";
    private static final String BASIC_DATA_COLLECTION_UUID = "e29b-41d4-a716";
    public static final String TYPE_CONNECTION_LOST = "CONNECTION_LOST";
    public static final String TYPE_COMMUNICATION_FAILED = "DEVICE_COMMUNICATION_FAILURE";

    private final IssueCreationService issueCreationService;
    private final IssueService issueService;
    private final Store store;

    private String type;

    @Inject
    public IssueRuleGenerator(Store store, IssueCreationService issueCreationService, IssueService issueService) {
        super(IssueRuleGenerator.class);
        this.store = store;
        this.issueCreationService = issueCreationService;
        this.issueService = issueService;
    }

    public IssueRuleGenerator withType(String type){
        this.type = type;
        return this;
    }

    public void create(){
        System.out.println("==> Creating issue creation rule " + getName() + "...");
        CreationRule rule = issueCreationService.createRule();
        rule.setName(getName());
        rule.setReason(getReasonForRule());
        rule.setTemplateUuid(BASIC_DATA_COLLECTION_UUID);
        rule.setDueInType(DueInType.MONTH);
        rule.setDueInValue(1);
        rule.setContent(getCreationRuleTemplate().getContent());
        rule.addParameter("eventType", type);
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
