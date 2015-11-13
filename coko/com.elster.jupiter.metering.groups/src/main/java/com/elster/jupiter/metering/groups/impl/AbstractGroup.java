package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;

import javax.validation.constraints.Size;
import java.time.Instant;

public class AbstractGroup {

    protected long id;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max= Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;
    private String mRID;
    private String description;
    private String aliasName;
    private String type;

    //audit columns
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    public String getAliasName() {
        return aliasName;
    }

    public String getDescription() {
        return description;
    }

    public String getMRID() {
        return mRID;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMRID(String mrid) {
        this.mRID = mrid;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getVersion() {
        return this.version;
    }
}
