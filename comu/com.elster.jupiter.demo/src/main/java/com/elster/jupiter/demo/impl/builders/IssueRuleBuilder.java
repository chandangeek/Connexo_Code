package com.elster.jupiter.demo.impl.builders;

import static com.elster.jupiter.util.conditions.Where.where;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueService;
import com.energyict.mdc.issue.datacollection.impl.templates.BasicDataCollectionRuleTemplate;

public class IssueRuleBuilder extends NamedBuilder<CreationRule, IssueRuleBuilder> {
    private static final String BASIC_DATA_COLLECTION_RULE_TEMPLATE = "BasicDataCollectionRuleTemplate";
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
        CreationRuleBuilder builder = issueCreationService.newCreationRule();
        builder.setName(getName());
        builder.setReason(getReasonForRule());
        builder.setDueInTime(DueInType.WEEK, 1);
        CreationRuleTemplate template = getCreationRuleTemplate();
        builder.setTemplate(template.getName());
        Map<String, Object> properties = new HashMap<>();
        properties.put(BasicDataCollectionRuleTemplate.EVENTTYPE,
                template.getPropertySpec(BasicDataCollectionRuleTemplate.EVENTTYPE).getValueFactory().fromStringValue(type));
        properties.put(BasicDataCollectionRuleTemplate.AUTORESOLUTION,
                template.getPropertySpec(BasicDataCollectionRuleTemplate.AUTORESOLUTION).getValueFactory().fromStringValue("1"));
        builder.setProperties(properties);
        CreationRule rule = builder.complete();
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
        CreationRuleTemplate template = issueCreationService.findCreationRuleTemplate(BASIC_DATA_COLLECTION_RULE_TEMPLATE).orElse(null);
        if (template == null) {
            throw new UnableToCreate("Unable to find creation rule template = " + BASIC_DATA_COLLECTION_RULE_TEMPLATE);
        }
        return template;
    }
}
