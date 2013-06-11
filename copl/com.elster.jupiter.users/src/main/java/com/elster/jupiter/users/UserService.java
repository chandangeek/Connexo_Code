package com.elster.jupiter.users;

import com.elster.jupiter.domain.util.Query;
import com.google.common.base.Optional;

public interface UserService {

    User createUser(String authenticationName, String description);

    Group createGroup(String name);

    Privilege createPrivilege(String componentName, String name, String description);

    Optional<User> findUser(String authenticationName);

    Optional<Group> findGroup(String name);

    Optional<Group> getGroup(long id);

    Optional<User> getUser(long id);

    Optional<Privilege> getPrivilege(String privilegeName);

    Optional<User> authenticateBase64(String base64String);

    String getRealm();

    Query<Group> getGroupQuery();

    Query<User> getUserQuery();

    Query<Privilege> getPrivilegeQuery();

    Group newGroup(String name);

    User newUser(String name);
}
