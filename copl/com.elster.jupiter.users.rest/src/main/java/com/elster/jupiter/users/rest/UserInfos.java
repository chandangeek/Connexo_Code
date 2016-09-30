package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.users.User;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class UserInfos {

    public int total;

    public List<UserInfo> users = new ArrayList<>();

    public UserInfos() {
    }

    public UserInfos(NlsService nlsService, User user) {
        this();
        add(nlsService, user);
    }

    public UserInfos(NlsService nlsService, Iterable<? extends User> users) {
        this();
        addAll(nlsService, users);
    }

    public UserInfo add(NlsService nlsService, User user) {
        UserInfo result = new UserInfo(nlsService, user);
        users.add(result);
        total++;
        return result;
    }

    private void addAll(NlsService nlsService, Iterable<? extends User> users) {
        for (User each : users) {
            add(nlsService, each);
        }
    }

}