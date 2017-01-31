/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Group;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupInfo {

    public long id;
    public String name;
    public long version;
    public String description;
    public String createdOn;
    public String modifiedOn;
    public boolean canEdit = false;
    public boolean currentUserCanGrant = false;
    public List<PrivilegeInfo> privileges = new ArrayList<>();

    public boolean update(Group group) {
        if(description != null && !description.equals(group.getDescription())){
            group.setDescription(description);
            return true;
        }
        return false;
    }

}
