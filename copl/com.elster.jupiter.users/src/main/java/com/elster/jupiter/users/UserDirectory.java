package com.elster.jupiter.users;

import com.elster.jupiter.users.impl.UserImpl;
import com.google.common.base.Optional;

import java.util.List;

public interface UserDirectory {
    public List<Group> getGroups(User user);
    public Optional<User> authenticate(String name, String password);

    boolean isManageGroupsInternal();

    String getDomain();

    public boolean isDefault();

    void setDefault(boolean aDefault);

    void save();

    UserImpl newUser(String userName, String description);
}
