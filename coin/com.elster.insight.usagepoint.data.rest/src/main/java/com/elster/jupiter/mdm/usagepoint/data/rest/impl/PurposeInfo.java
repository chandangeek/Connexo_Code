package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

public class PurposeInfo {
    public long id;
    public String name;
    public boolean required;
    public boolean active;
    public String mRID;
    public IdWithNameInfo status;

    public PurposeInfo() {
    }
}
