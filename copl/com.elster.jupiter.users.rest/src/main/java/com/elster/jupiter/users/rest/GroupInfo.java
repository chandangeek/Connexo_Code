package com.elster.jupiter.users.rest;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.users.Group;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<PrivilegeInfo> privileges = new ArrayList<>();

    public GroupInfo() {
    }

    public GroupInfo(NlsService nlsService, Group group) {
        id = group.getId();
        name = group.getName();
        version = group.getVersion();
        description = group.getDescription();
        createdOn = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(group.getCreationDate().atZone(ZoneId.systemDefault()));
        modifiedOn = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(group.getModifiedDate().atZone(ZoneId.systemDefault()));
        privileges.addAll(group.getPrivileges()
                .entrySet()
                .stream()
                .flatMap(x->x.getValue().stream().map(p->PrivilegeInfo.asApplicationPrivilege(nlsService, x.getKey(), p)))
                .collect(Collectors.toList()));

        Collections.sort(privileges, (p1, p2) -> {
            int result = p1.applicationName.compareTo(p2.applicationName);
            return result != 0 ? result : p1.name.compareTo(p2.name);
            });
        canEdit = !privileges.stream()
                .filter(privilegeInfo -> !privilegeInfo.canGrant)
                .findAny()
                .isPresent();

    }

    public boolean update(Group group) {
        if(description != null && !description.equals(group.getDescription())){
            group.setDescription(description);
            return true;
        }
        return false;
    }

}
