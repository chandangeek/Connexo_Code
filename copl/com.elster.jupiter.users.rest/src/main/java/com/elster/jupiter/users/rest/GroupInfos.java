package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.Thesaurus;
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

    public GroupInfos(Thesaurus thesaurus, Group group) {
        add(thesaurus, group);
    }

    public GroupInfos(Thesaurus thesaurus, Iterable<? extends Group> groups) {
        addAll(thesaurus, groups);
    }

    public GroupInfo add(Thesaurus thesaurus, Group group) {
        GroupInfo result = new GroupInfo(thesaurus, group);
        groups.add(result);
        total++;
        return result;
    }

    void addAll(Thesaurus thesaurus, Iterable<? extends Group> groups) {
        for (Group each : groups) {
            add(thesaurus, each);
        }
    }

}
