package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.NlsService;
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

    UserInGroupInfos(NlsService nlsService, User user, Group group) {
        this();
        add(nlsService, user, group);
    }

    UserInGroupInfo add(NlsService nlsService, User user, Group group) {
        UserInGroupInfo result = new UserInGroupInfo(nlsService, user, group);
        userInGroupInfos.add(result);
        total++;
        return result;
    }

    void addAll(NlsService nlsService, User user, Iterable<? extends Group> groups) {
        for (Group each : groups) {
            add(nlsService, user, each);
        }
    }

}