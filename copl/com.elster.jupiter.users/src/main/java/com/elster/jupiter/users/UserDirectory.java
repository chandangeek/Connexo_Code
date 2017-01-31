/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import java.util.List;
import java.util.Optional;

public interface UserDirectory {

    List<Group> getGroups(User user);

    List<LdapUser> getLdapUsers();

    boolean getLdapUserStatus(String userName);

    Optional<User> authenticate(String name, String password);

    boolean isManageGroupsInternal();

    void setManageGroupsInternal(boolean manageGroupsInternal);

    String getDomain();

    void setDomain(String domain);

    String getType();

    void setType(String type);

    long getId();

    String getPrefix();

    void setPrefix(String prefix);

    boolean isDefault();

    void setDefault(boolean aDefault);

    void update();

    void delete();

    User newUser(String userName, String description, boolean allowPwdChange,boolean status);

}