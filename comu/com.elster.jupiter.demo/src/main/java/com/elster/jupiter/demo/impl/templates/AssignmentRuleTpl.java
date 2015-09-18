package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.AssignmentRuleBuilder;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.users.User;

public enum  AssignmentRuleTpl implements Template<AssignmentRule, AssignmentRuleBuilder>{
    DEFAULT_TO_MONICA("Assign all issues to Monica (default)", UserTpl.MONICA, null, AssignmentRule.ASSIGNMENTRULE_TO_MONICA),
    UNBOUND_REASON_TO_SAM("Assign 'Unknown outbound device' issues to SAM", UserTpl.SAM, "reason.unknown.outbound.device", AssignmentRule.ASSIGNMENTRULE_TO_SAM),
    ;

    private String name;
    private UserTpl userTpl;
    private String reasonKey;
    private String ruleData;

    AssignmentRuleTpl(String name, UserTpl userTpl, String reasonKey, String ruleData) {
        this.name = name;
        this.userTpl = userTpl;
        this.ruleData = ruleData;
        this.reasonKey = reasonKey;
    }

    @Override
    public Class<AssignmentRuleBuilder> getBuilderClass() {
        return AssignmentRuleBuilder.class;
    }

    @Override
    public AssignmentRuleBuilder get(AssignmentRuleBuilder builder) {
        User user = Builders.from(this.userTpl).get();
        return builder.withName(this.name).withDescription(this.name).withDataTemplate(this.ruleData).withReasonKey(this.reasonKey).withUserId(user.getId());
    }

    private static class AssignmentRule {
        public static final String ASSIGNMENTRULE_TO_MONICA =
                "import com.elster.jupiter.issue.share.entity.IssueForAssign;\n" +
                        "rule \"Assign to @USERID (default)\"\n" +
                        "salience 0\n" +
                        "when\n" +
                        "    issue : IssueForAssign(!processed)\n" +
                        "then\n" +
                        "    issue.assignTo(\"User\", @USERID);\n" +
                        "    update(issue);\n" +
                        "end\n";
        public static final String ASSIGNMENTRULE_TO_SAM =
                "import com.elster.jupiter.issue.share.entity.IssueForAssign;\n" +
                        "rule \"Assign to @USERID\"\n" +
                        "salience 100\n" +
                        "when\n" +
                        "    issue : IssueForAssign(reason == \"@REASON\", !processed)\n" +
                        "then\n" +
                        "    issue.assignTo(\"User\", @USERID);\n" +
                        "    update(issue);\n" +
                        "end\n";
    }
}
