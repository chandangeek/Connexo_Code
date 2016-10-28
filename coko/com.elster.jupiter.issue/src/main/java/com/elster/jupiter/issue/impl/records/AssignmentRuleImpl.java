package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssignmentRuleImpl extends EntityImpl implements AssignmentRule {
    private static final Logger LOG = Logger.getLogger(AssignmentRuleImpl.class.getName());

    private int priority;
    @Size(min = 1, max = 400, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String description;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 400, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String title;
    private boolean enabled = true;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String ruleData;

    private IssueService issueService;
    private Thesaurus thesaurus;

    @Inject
    public AssignmentRuleImpl(DataModel dataModel, IssueService issueService, Thesaurus thesaurus){
        super(dataModel);
        this.issueService = issueService;
        this.thesaurus = thesaurus;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public byte[] getRuleData() {
        return ruleData != null ? ruleData.getBytes(Charset.defaultCharset()) : new byte[]{};
    }

    @Override
    public String getRuleBody() {
        return ruleData;
    }

    @Override
    public void setRuleData(String ruleData) {
        this.ruleData = ruleData;
    }

    /**
     * Each assignment rule contains a rule body. Rule body is a set of Drools commands in text format.
     * The main goal of this rule is a set correct assignee for issue. It can be managed by this code:
     * <code><pre>
     *   issue.assignTo("USER", 1);
     * </pre></code>
     * So when you call this method we need to scan a rule's body and find corresponding patterns.
     * We use this pattern:
     * <code><pre>
     *     \.assignTo\s*\(\s*\"(\w+)\"\s*,\s*(\d+)\s*\);
     * </pre></code>
     * This method doesn't affect rule execution.
     */
    @Override
    public IssueAssignee getAssignee(){
        String assigneType = null;
        long assigneeId = 0;
        if (ruleData != null) {
            Pattern pattern = Pattern.compile("\\.assignTo\\s*\\(\\s*\\\"(\\w+)\\\"\\s*,\\s*(\\d+)\\s*\\);");
            Matcher matcher = pattern.matcher(ruleData);
            if (matcher.find()) {
                assigneType = matcher.group(1);
                try {
                    String assigneeIdStr = matcher.group(2);
                    assigneeId = assigneeIdStr != null ? Long.parseLong(assigneeIdStr) : 0;
                } catch (NumberFormatException ex){
                    assigneeId = 0;
                    MessageSeeds.ISSUE_ASSIGN_RULE_GET_ASSIGNEE.log(LOG, thesaurus, getTitle());
                }
            } else {
                MessageSeeds.ISSUE_ASSIGN_RULE_GET_ASSIGNEE.log(LOG, thesaurus, getTitle());
            }
        }
        return issueService.findIssueAssignee(assigneeId, null);
    }
}
