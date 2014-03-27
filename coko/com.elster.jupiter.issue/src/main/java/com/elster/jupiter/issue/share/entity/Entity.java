package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;

public abstract class Entity {
    protected long id;

    // Audit fields
    protected long version;
    protected UtcInstant createTime;
    protected UtcInstant modTime;
    protected String userName;

    protected DataModel dataModel;

    protected Entity(DataModel dataModel){
        this.dataModel = dataModel;
    }

    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }


    public long getVersion() {
        return version;
    }

    void setVersion(long version) {
        this.version = version;
    }

    public UtcInstant getCreateTime() {
        return createTime;
    }

    void setCreateTime(UtcInstant createTime) {
        this.createTime = createTime;
    }

    public UtcInstant getModTime() {
        return modTime;
    }

    void setModTime(UtcInstant modTime) {
        this.modTime = modTime;
    }

    public String getUserName() {
        return userName;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    public void save(){
        dataModel.persist(this);
    }

    public void update(){
        dataModel.update(this);
    }

    public void delete(){
        dataModel.remove(this);
    }
}
