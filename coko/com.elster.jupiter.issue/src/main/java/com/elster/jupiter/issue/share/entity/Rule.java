package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.impl.database.NonSearchable;

import java.nio.charset.Charset;

public class Rule extends Entity {
    @NonSearchable
    private int priority;
    private String description;
    private String title;
    private boolean enabled = true;
    private String ruleData;

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
}
