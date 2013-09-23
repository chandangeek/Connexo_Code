package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.UtcInstant;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ReadingTypeInfo {

    public String mRID;
    public String aliasName;
    public String name;
    public long version;
    public UtcInstant createTime;
    public UtcInstant modTime;
    public String userName;

    public ReadingTypeInfo() {
    }

    public ReadingTypeInfo(ReadingType readingType) {
        this.mRID = readingType.getMRID();
        this.aliasName = readingType.getAliasName();
        this.version = readingType.getVersion();
        this.name = readingType.getName();
    }
}
