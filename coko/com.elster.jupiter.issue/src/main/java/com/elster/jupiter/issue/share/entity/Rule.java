package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule extends Entity {
    private static final Logger LOG = Logger.getLogger(Rule.class.getName());

    private int priority;
    private String description;
    private String title;
    private boolean enabled = true;
    private String ruleData;

    private IssueService issueService;
    private UserService userService;

    @Inject
    public Rule(DataModel dataModel, IssueService issueService, UserService userService){
        super(dataModel);
        this.issueService = issueService;
        this.userService = userService;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public byte[] getRuleData() {
        return ruleData != null ? ruleData.getBytes(Charset.defaultCharset()) : new byte[]{};
    }

    public String getRuleBody() {
        return ruleData;
    }

    public void setRuleData(String ruleData) {
        this.ruleData = ruleData;
    }

    public IssueAssignee getAssignee(){
        return buildIssueAssignee(getAssigneeType(), getAssigneeId());
    }

    private IssueAssigneeType getAssigneeType(){
        if (ruleData != null) {
            Pattern pattern = Pattern.compile("IssueAssigneeType\\.(\\w+)\\s*\\)");
            Matcher matcher = pattern.matcher(ruleData);
            if (matcher.find()) {
                return IssueAssigneeType.valueOf(matcher.group(1));
            }
        }
        return null;
    }

    private long getAssigneeId(){
        long assigneeId = 0;
        if (ruleData != null) {
            Pattern pattern = Pattern.compile("\\.setAssigneeId\\s*\\(\\s*(\\d+)\\s*\\);");
            Matcher matcher = pattern.matcher(ruleData);
            if (matcher.find()) {
                try {
                    String assigneeIdStr = matcher.group(1);
                    assigneeId = assigneeIdStr != null ? Long.parseLong(assigneeIdStr) : 0;
                } catch (NumberFormatException ex){
                    assigneeId = 0;
                    LOG.severe("Failed to parse assignee id in rule " + getId() + ", possible error in rule.");
                }
            }
        }
        return assigneeId;
    }

    private IssueAssignee buildIssueAssignee(IssueAssigneeType type, long id) {
        if (type != null) {
            switch (type) {
                case USER:
                    return IssueAssignee.fromUser(userService.getUser(id).orNull());
                case ROLE:
                    return IssueAssignee.fromRole(issueService.findAssigneeRole(id).orNull());
                case TEAM:
                    return IssueAssignee.fromTeam(issueService.findAssigneeTeam(id).orNull());
            }
        }
        return null;
    }
}
