package com.elster.jupiter.users.rest;

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

    public GroupInfos(Group group) {
        add(group);
    }

    public GroupInfos(Iterable<? extends Group> groups) {
        addAll(groups);
    }

    public GroupInfo add(Group group) {
        GroupInfo result = new GroupInfo(group);
        groups.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends Group> groups) {
        for (Group each : groups) {
            add(each);
        }
    }

}
