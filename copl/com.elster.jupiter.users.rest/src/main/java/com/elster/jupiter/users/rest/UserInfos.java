package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.User;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class UserInfos {

    public int total;

    public List<UserInfo> users = new ArrayList<>();

    UserInfos() {
    }

    UserInfos(User user) {
        add(user);
    }

    UserInfos(List<User> users) {
        addAll(users);
    }

    UserInfo add(User user) {
        UserInfo result = new UserInfo(user);
        users.add(result);
        total++;
        return result;
    }

    void addAll(List<User> users) {
        for (User each : users) {
            add(each);
        }
    }

}
