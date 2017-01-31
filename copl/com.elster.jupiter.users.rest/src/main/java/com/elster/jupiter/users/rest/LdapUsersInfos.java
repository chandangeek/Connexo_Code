/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest;


import com.elster.jupiter.users.LdapUser;
import java.util.ArrayList;
import java.util.List;

public class LdapUsersInfos {

    public int total;

    public List<LdapUsersInfo> ldapUsers = new ArrayList<>();

    public LdapUsersInfos() {
    }

    public LdapUsersInfos(LdapUser ldapUser) {
        add(ldapUser);
    }

    public LdapUsersInfos(Iterable<? extends LdapUser> ldapUsers) {
        addAll(ldapUsers);
    }

    public LdapUsersInfo add(LdapUser ldapUser) {
        LdapUsersInfo result = new LdapUsersInfo(ldapUser);
        ldapUsers.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends LdapUser> infos) {
        for (LdapUser each : infos) {
            add(each);
        }
    }
}
