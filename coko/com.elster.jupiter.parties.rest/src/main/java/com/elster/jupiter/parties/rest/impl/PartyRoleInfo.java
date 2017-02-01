/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyRole;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PartyRoleInfo {

    public String componentName;
    public String mRID;
    public String name;
    public String aliasName;
    public String description;
    public long version;

    PartyRoleInfo() {
    }

    public PartyRoleInfo(PartyRole role) {
        componentName = role.getComponentName();
        mRID = role.getMRID();
        name = role.getName();
        aliasName = role.getAliasName();
        description = role.getDescription();
        version = role.getVersion();

    }

}
