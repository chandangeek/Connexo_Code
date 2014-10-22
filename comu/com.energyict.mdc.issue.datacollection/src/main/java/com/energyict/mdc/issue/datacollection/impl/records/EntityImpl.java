package com.energyict.mdc.issue.datacollection.impl.records;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;

import java.time.Instant;

public abstract class EntityImpl {
    private long id;

    // Audit fields
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private DataModel dataModel;

    protected EntityImpl(DataModel dataModel){
        this.dataModel = dataModel;
    }

    protected DataModel getDataModel() {
        return dataModel;
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

    public Instant getCreateTime() {
        return createTime;
    }

    void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public Instant getModTime() {
        return modTime;
    }

    void setModTime(Instant modTime) {
        this.modTime = modTime;
    }

    public String getUserName() {
        return userName;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    public void save(){
        if (this.createTime == null){
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    public void delete(){
        dataModel.remove(this);
    }
}
