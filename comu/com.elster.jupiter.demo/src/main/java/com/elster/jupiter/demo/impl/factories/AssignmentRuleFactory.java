package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;

import javax.inject.Inject;

public class AssignmentRuleFactory extends NamedFactory<AssignmentRuleFactory, AssignmentRule> {

    private IssueAssignmentService issueAssignmentService;
    private String description;
    private String ruleData;

    @Inject
    public AssignmentRuleFactory(IssueAssignmentService issueAssignmentService) {
        super(AssignmentRuleFactory.class);
        this.issueAssignmentService = issueAssignmentService;
    }

    @Override
    public AssignmentRule get() {
        AssignmentRule rule = issueAssignmentService.createAssignmentRule();
        rule.setTitle(getName());
        rule.setDescription(description);
        rule.setPriority(0);
        rule.setRuleData(ruleData);
        rule.setEnabled(true);
        rule.save();
        return rule;
    }

    public AssignmentRuleFactory withDescription(String description) {
        this.description = description;
        return this;
    }

    public AssignmentRuleFactory withRuleData(String ruleData) {
        this.ruleData = ruleData;
        return this;
    }
}
