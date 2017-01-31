/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest;


import com.elster.jupiter.users.LdapUserDirectory;

import java.util.ArrayList;
import java.util.List;

public class UserDirectoryInfos {

    public int total;

    public List<UserDirectoryInfo> userDirectories = new ArrayList<>();

    public UserDirectoryInfos() {
    }

    public UserDirectoryInfos(LdapUserDirectory userDirectory) {
        add(userDirectory);
    }

    public UserDirectoryInfos(Iterable<? extends LdapUserDirectory> userDirectories) {
        addAll(userDirectories);
    }

    public UserDirectoryInfo add(LdapUserDirectory userDirectory) {
        UserDirectoryInfo result = new UserDirectoryInfo(userDirectory);
        userDirectories.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends LdapUserDirectory> infos) {
        for (LdapUserDirectory each : infos) {
            add(each);
        }
    }

}


