package com.elster.jupiter.users;

import com.google.common.base.Optional;

import java.util.List;

public interface UserDirectory {
    public List<Group> getGroups(User user);
    public Optional<User> authenticate(String name, String password);

}
