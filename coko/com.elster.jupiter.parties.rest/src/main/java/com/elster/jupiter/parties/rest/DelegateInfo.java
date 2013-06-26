package com.elster.jupiter.parties.rest;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.rest.UserInfo;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DelegateInfo {

    public UserInfo delegate;

    public DelegateInfo() {
    }

    public DelegateInfo(User user) {
        delegate = new UserInfo(user);
    }

}
