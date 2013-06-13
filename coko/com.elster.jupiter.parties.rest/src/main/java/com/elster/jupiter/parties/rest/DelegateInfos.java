package com.elster.jupiter.parties.rest;

import com.elster.jupiter.users.User;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DelegateInfos {

    public int total;

    public List<DelegateInfo> roles = new ArrayList<>();

    DelegateInfos() {
    }

    DelegateInfos(User user) {
        add(user);
    }

    DelegateInfos(List<User> users) {
        addAll(users);
    }

    DelegateInfo add(User user) {
        DelegateInfo result = new DelegateInfo(user);
        roles.add(result);
        total++;
        return result;
    }

    void addAll(List<User> users) {
        for (User each : users) {
            add(each);
        }
    }

}
