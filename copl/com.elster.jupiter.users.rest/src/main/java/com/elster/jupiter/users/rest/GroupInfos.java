package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Group;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class GroupInfos {
        public int total;

        public List<GroupInfo> groups = new ArrayList<>();

        GroupInfos() {
        }

        GroupInfos(Group group) {
            add(group);
        }

        GroupInfos(List<Group> groups) {
            addAll(groups);
        }

        GroupInfo add(Group group) {
            GroupInfo result = new GroupInfo(group);
            groups.add(result);
            total++;
            return result;
        }

        void addAll(List<Group> groups) {
            for (Group each : groups) {
                add(each);
            }
        }

}
