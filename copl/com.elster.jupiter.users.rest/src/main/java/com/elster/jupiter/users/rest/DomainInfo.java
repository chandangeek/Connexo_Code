/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.UserDirectory;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DomainInfo {
    public String domain;
    public boolean isDefault;
    public boolean manageGroupsInternal;

    public DomainInfo() {
    }

    public DomainInfo(UserDirectory directory) {
        domain=directory.getDomain();
        isDefault=directory.isDefault();
        manageGroupsInternal=directory.isManageGroupsInternal();
    }
}
