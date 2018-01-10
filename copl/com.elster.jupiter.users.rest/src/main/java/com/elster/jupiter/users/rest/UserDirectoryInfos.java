/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest;


import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.users.LdapUserDirectory;

import java.util.ArrayList;
import java.util.List;

@ProviderType
public class UserDirectoryInfos {

    public int total;

    public List<UserDirectoryInfo> userDirectories = new ArrayList<>();

    public UserDirectoryInfos() {
    }

    @Deprecated
    public UserDirectoryInfos(LdapUserDirectory userDirectory) {
        add(userDirectory);
    }

    @Deprecated
    public UserDirectoryInfos(Iterable<? extends LdapUserDirectory> userDirectories) {
        addAll(userDirectories);
    }

    @Deprecated
    public UserDirectoryInfo add(LdapUserDirectory userDirectory) {
        UserDirectoryInfo result = new UserDirectoryInfo(userDirectory);
        userDirectories.add(result);
        total++;
        return result;
    }

    @Deprecated
    void addAll(Iterable<? extends LdapUserDirectory> infos) {
        for (LdapUserDirectory each : infos) {
            add(each);
        }
    }

    public UserDirectoryInfo add(UserDirectoryInfo userDirectory) {
        userDirectories.add(userDirectory);
        total++;
        return userDirectory;
    }

}


