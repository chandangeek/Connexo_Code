package com.elster.jupiter.metering.rest;

import java.time.Instant;

import com.elster.jupiter.metering.ReadingType;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ReadingTypeInfo {

    public String mRID;
    public String aliasName;
    public String name;
    public long version;
    public Instant createTime;
    public Instant modTime;
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
