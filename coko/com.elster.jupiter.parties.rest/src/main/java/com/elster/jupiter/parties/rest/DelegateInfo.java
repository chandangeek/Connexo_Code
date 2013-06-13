package com.elster.jupiter.parties.rest;

import com.elster.jupiter.users.User;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DelegateInfo {

    public String delegate;

    public DelegateInfo() {
    }

    public DelegateInfo(User user) {
        delegate = user.getName();

    }
}
