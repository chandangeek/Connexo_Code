package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;

public abstract class EntityImpl implements Entity {
    private long id;

    // Audit fields
    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

    private DataModel dataModel;

    protected EntityImpl(DataModel dataModel){
        this.dataModel = dataModel;
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    void setVersion(long version) {
        this.version = version;
    }

    @Override
    public UtcInstant getCreateTime() {
        return createTime;
    }

    void setCreateTime(UtcInstant createTime) {
        this.createTime = createTime;
    }

    @Override
    public UtcInstant getModTime() {
        return modTime;
    }

    void setModTime(UtcInstant modTime) {
        this.modTime = modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void save(){
        Save.CREATE.save(dataModel, this);
    }

    @Override
    public void update(){
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public void delete(){
        dataModel.remove(this);
    }
}
