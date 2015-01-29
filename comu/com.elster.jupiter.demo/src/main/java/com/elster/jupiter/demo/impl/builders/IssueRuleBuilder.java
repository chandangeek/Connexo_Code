package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;

import javax.inject.Inject;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class IssueRuleBuilder extends NamedBuilder<CreationRule, IssueRuleBuilder> {
    private static final String BASIC_DATA_COLLECTION_UUID = "e29b-41d4-a716";
    private final IssueCreationService issueCreationService;
    private final IssueService issueService;

    private String type;
    private String reason;

    @Inject
    public IssueRuleBuilder(IssueCreationService issueCreationService, IssueService issueService) {
        super(IssueRuleBuilder.class);
        this.issueCreationService = issueCreationService;
        this.issueService = issueService;
    }

    public IssueRuleBuilder withType(String type){
        this.type = type;
        return this;
    }

    public IssueRuleBuilder withReason(String reason){
        this.reason = reason;
        return this;
    }

    @Override
    public Optional<CreationRule> find() {
        return issueCreationService.getCreationRuleQuery().select(where("name").isEqualTo(getName())).stream().findFirst();
    }

    @Override
    public CreationRule create() {
        Log.write(this);
        CreationRule rule = issueCreationService.createRule();
        rule.setName(getName());
        rule.setReason(getReasonForRule());
        rule.setTemplateUuid(BASIC_DATA_COLLECTION_UUID);
        rule.setDueInType(DueInType.WEEK);
        rule.setDueInValue(1);
        rule.setContent(getCreationRuleTemplate().getContent());
        rule.addParameter("eventType", type);
        rule.addParameter("autoResolution", "true");
        rule.validate();
        rule.save();
        rule.updateContent();
        rule.save();
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
        CreationRuleTemplate template = issueCreationService.findCreationRuleTemplate(BASIC_DATA_COLLECTION_UUID).orElse(null);
        if (template == null) {
            throw new UnableToCreate("Unable to find creation rule template with id = " + BASIC_DATA_COLLECTION_UUID);
        }
        return template;
    }
}
