package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

public class IssueReason extends Entity {
    private String name;
    private String topic;

    @Inject
    public IssueReason(DataModel dataModel) {
        super(dataModel);
    }

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
