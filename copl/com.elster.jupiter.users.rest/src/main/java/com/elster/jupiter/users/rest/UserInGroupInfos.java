package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class UserInGroupInfos {
    public int total;

    public List<UserInGroupInfo> userInGroupInfos = new ArrayList<>();

    UserInGroupInfos() {
    }

    UserInGroupInfos(User user, Group group) {
        add(user, group);
    }

    UserInGroupInfo add(User user, Group group) {
        UserInGroupInfo result = new UserInGroupInfo(user, group);
        userInGroupInfos.add(result);
        total++;
        return result;
    }

    void addAll(User user, Iterable<? extends Group> groups) {
        for (Group each : groups) {
            add(user, each);
        }
    }

}
