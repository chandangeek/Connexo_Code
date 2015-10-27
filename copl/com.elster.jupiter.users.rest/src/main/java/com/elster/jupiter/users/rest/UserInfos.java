package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.Thesaurus;
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

    public UserInfos(Thesaurus thesaurus, User user) {
        add(thesaurus, user);
    }

    public UserInfos(Thesaurus thesaurus, Iterable<? extends User> users) {
        addAll(thesaurus, users);
    }

    public UserInfo add(Thesaurus thesaurus, User user) {
        UserInfo result = new UserInfo(thesaurus, user);
        users.add(result);
        total++;
        return result;
    }

    void addAll(Thesaurus thesaurus, Iterable<? extends User> users) {
        for (User each : users) {
            add(thesaurus, each);
        }
    }

}
