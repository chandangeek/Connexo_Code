package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.IssueReason;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;

public class IssueReasonImpl implements IssueReason {
    private final DataModel dataModel;

    private long id;
    private String name;
    private String topic;

    // Audit fields
    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

   @Inject
   IssueReasonImpl(DataModel dataModel) {
       this.dataModel = dataModel;
   }

   public String getTopic() {
       return topic;
   }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setCreateTime(UtcInstant createTime) {
        this.createTime = createTime;
    }

    public void setModTime(UtcInstant modTime) {
        this.modTime = modTime;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    IssueReasonImpl init (String topic, String name) {
        this.topic = topic;
        this.name = name;
        return this;
    }

    static IssueReasonImpl from(DataModel dataModel, String topic, String name) {
        return dataModel.getInstance(IssueReasonImpl.class).init(topic, name);
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getUserName() {
        return userName;
    }

    public UtcInstant getModTime() {
        return modTime;
    }

    public UtcInstant getCreateTime() {
        return createTime;
    }

    public long getVersion() {
        return version;
    }
}
