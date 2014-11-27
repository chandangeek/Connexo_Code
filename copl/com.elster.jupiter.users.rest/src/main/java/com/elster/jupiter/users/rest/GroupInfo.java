package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupInfo {

    public long id;
    public String name;
    public long version;
    public String description;
    public String createdOn;
    public String modifiedOn;
    public List<PrivilegeInfo> privileges = new ArrayList<>();

    public GroupInfo() {
    }

    public GroupInfo(Group group) {
        id = group.getId();
        name = group.getName();
        version = group.getVersion();
        description = group.getDescription();
        createdOn= DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(group.getCreationDate().atZone(ZoneId.systemDefault()));
        modifiedOn=DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(group.getModifiedDate().atZone(ZoneId.systemDefault()));
        for (Privilege privilege : group.getPrivileges()) {
            privileges.add(new PrivilegeInfo(privilege));
        }

        Collections.sort(privileges, new Comparator<PrivilegeInfo>() {
            public int compare(PrivilegeInfo p1, PrivilegeInfo p2) {
                return p1.name.compareTo(p2.name);
                }
        });
    }

    public boolean update(Group group) {
        if(description != null && !description.equals(group.getDescription())){
            group.setDescription(description);
            return true;
        }
        return false;
    }
    
}
