package com.elster.jupiter.users;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface UserDirectory {
    public List<Group> getGroups(User user);

    public List<LdapUser> getLdapUsers();

    public Optional<User> authenticate(String name, String password);

    boolean isManageGroupsInternal();

    String getDomain();

    void setDomain(String domain);

    String getType();

    void setType(String type);

    long getId();

    String getPrefix();

    void setPrefix(String prefix);

    public boolean isDefault();

    void setDefault(boolean aDefault);

    void save();

    void delete();

    User newUser(String userName, String description, boolean allowPwdChange);
}
