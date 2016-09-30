package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.users.Group;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class GroupInfos {

    public int total;
    public List<GroupInfo> groups = new ArrayList<>();

    public GroupInfos() {
    }

    public GroupInfos(NlsService nlsService, Group group) {
        this();
        add(nlsService, group);
    }

    public GroupInfos(NlsService nlsService, Iterable<? extends Group> groups) {
        this();
        addAll(nlsService, groups);
    }

    public GroupInfo add(NlsService nlsService, Group group) {
        GroupInfo result = new GroupInfo(nlsService, group);
        groups.add(result);
        total++;
        return result;
    }

    void addAll(NlsService nlsService, Iterable<? extends Group> groups) {
        for (Group each : groups) {
            add(nlsService, each);
        }
    }

}
