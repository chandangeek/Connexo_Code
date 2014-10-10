package com.elster.jupiter.users;

import java.util.List;
import java.util.Optional;

public interface UserDirectory {
    public List<Group> getGroups(User user);
    public Optional<User> authenticate(String name, String password);

    boolean isManageGroupsInternal();

    String getDomain();

    public boolean isDefault();

    void setDefault(boolean aDefault);

    void save();

    User newUser(String userName, String description, boolean allowPwdChange);
}
