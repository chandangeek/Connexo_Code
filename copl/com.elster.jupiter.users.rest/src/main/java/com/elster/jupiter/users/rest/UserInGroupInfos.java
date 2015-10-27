package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.Thesaurus;
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

    UserInGroupInfos(Thesaurus thesaurus, User user, Group group) {
        add(thesaurus, user, group);
    }

    UserInGroupInfo add(Thesaurus thesaurus, User user, Group group) {
        UserInGroupInfo result = new UserInGroupInfo(thesaurus, user, group);
        userInGroupInfos.add(result);
        total++;
        return result;
    }

    void addAll(Thesaurus thesaurus, User user, Iterable<? extends Group> groups) {
        for (Group each : groups) {
            add(thesaurus, user, each);
        }
    }

}
