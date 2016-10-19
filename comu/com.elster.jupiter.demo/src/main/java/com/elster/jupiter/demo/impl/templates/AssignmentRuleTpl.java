package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.AssignmentRuleBuilder;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.users.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum  AssignmentRuleTpl implements Template<AssignmentRule, AssignmentRuleBuilder>{
    DEFAULT_TO_MONICA("Assign all issues (default)", UserTpl.MONICA, Arrays.asList(UserTpl.MONICA, UserTpl.PIETER, UserTpl.JOLIEN, UserTpl.MICHELLE, UserTpl.KURT), null, AssignmentRule.ASSIGNMENTRULE),
    UNBOUND_REASON_TO_SAM("Assign 'Unknown outbound device' issues to SAM", UserTpl.SAM, null, "reason.unknown.outbound.device", AssignmentRule.ASSIGNMENTRULE_TO_SAM),
    ;

    private String name;
    private UserTpl userTpl;
    private String reasonKey;
    private String ruleData;
    private List<UserTpl> userTplList;

    AssignmentRuleTpl(String name, UserTpl userTpl, List<UserTpl> userTplList, String reasonKey, String ruleData) {
        this.name = name;
        this.userTpl = userTpl;
        this.ruleData = ruleData;
        this.reasonKey = reasonKey;
        this.userTplList = userTplList;
    }

    @Override
    public Class<AssignmentRuleBuilder> getBuilderClass() {
        return AssignmentRuleBuilder.class;
    }

    @Override
    public AssignmentRuleBuilder get(AssignmentRuleBuilder builder) {
        User user = Builders.from(this.userTpl).get();
        List<Long> userIdList = userTplList != null ? userTplList.stream()
                .map(u -> Builders.from(u).get().getId())
                .collect(Collectors.toList()) : Collections.emptyList();
        return builder.withName(this.name)
                .withDescription(this.name)
                .withDataTemplate(this.ruleData)
                .withReasonKey(this.reasonKey)
                .withUserId(user.getId())
                .withUserIds(userIdList);
    }

    private static class AssignmentRule {

        public static final String ASSIGNMENTRULE =
                "import com.elster.jupiter.issue.share.entity.IssueForAssign;\n" +
                        "import java.util.Random;\n" +
                        "import java.util.Arrays;\n" +
                        "import java.util.List;\n" +
                        "rule \"Assign to (default)\"\n" +
                        "salience 0\n" +
                        "when\n" +
                        "    issue : IssueForAssign(!processed)\n" +
                        "then\n" +
                        "    issue.assignTo(\"User\", Arrays.asList(@USERLIST).get(new Random().nextInt(Arrays.asList(@USERLIST).size())));\n" +
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
