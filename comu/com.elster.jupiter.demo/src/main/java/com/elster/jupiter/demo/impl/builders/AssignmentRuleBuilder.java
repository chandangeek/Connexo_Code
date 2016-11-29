package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.templates.UserTpl;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

public class AssignmentRuleBuilder extends NamedBuilder<AssignmentRule, AssignmentRuleBuilder> {
    private final IssueAssignmentService issueAssignmentService;
    private final IssueService issueService;

    private String description;
    private String ruleData;
    private Long userId;
    private String reasonKey;
    private String userIds;

    @Inject
    public AssignmentRuleBuilder(IssueAssignmentService issueAssignmentService, IssueService issueService) {
        super(AssignmentRuleBuilder.class);
        this.issueAssignmentService = issueAssignmentService;
        this.issueService = issueService;
    }

    public AssignmentRuleBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public AssignmentRuleBuilder withDataTemplate(String ruleData) {
        this.ruleData = ruleData;
        return this;
    }

    public AssignmentRuleBuilder withReasonKey(String reasonKey){
        this.reasonKey = reasonKey;
        return this;
    }

    public AssignmentRuleBuilder withUserId(Long userId){
        this.userId = userId;
        return this;
    }

    public AssignmentRuleBuilder withUserIds(List<Long> users){
        this.userIds = String.join(",",users.stream().map(String::valueOf).collect(Collectors.toList()));
        return this;
    }

    @Override
    public Optional<AssignmentRule> find() {
        return issueAssignmentService.getAssignmentRuleQuery().select(where("title").isEqualTo(getName())).stream().findFirst();
    }

    @Override
    public AssignmentRule create() {
        Log.write(this);
        if (this.ruleData == null){
            throw new UnableToCreate("Rule data for assignment rule can't be null");
        }
        if (this.userId != null){
            this.ruleData = this.ruleData.replace("@USERID", Long.toString(this.userId));
        }
        if (this.userIds != null){
            this.ruleData = this.ruleData.replace("@USERLIST", this.userIds);
        }
        if (this.reasonKey != null){
            Optional<IssueReason> reasonRef = issueService.findReason(this.reasonKey);
            if (!reasonRef.isPresent()){
                throw new UnableToCreate("Unable to find issue reason with key = " + this.reasonKey);
            }
            this.ruleData = this.ruleData.replace("@REASON", reasonRef.get().getName());
        }
        AssignmentRule rule = issueAssignmentService.createAssignmentRule(getName(), ruleData);
        rule.setDescription(description);
        rule.setPriority(0);
        rule.setEnabled(true);
        rule.update();
        return rule;
    }
}
