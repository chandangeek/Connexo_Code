package com.elster.jupiter.users.rest;

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

    public UserInfos(User user) {
        add(user);
    }

    public UserInfos(Iterable<? extends User> users) {
        addAll(users);
    }

    public UserInfo add(User user) {
        UserInfo result = new UserInfo(user);
        users.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends User> users) {
        for (User each : users) {
            add(each);
        }
    }

}
