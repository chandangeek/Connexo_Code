package com.elster.jupiter.issue.share.entity;

public class IssueReason extends Entity {
    private String name;
    private String topic;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
