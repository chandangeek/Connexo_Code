package com.elster.jupiter.users;

import com.elster.jupiter.domain.util.Query;
import com.google.common.base.Optional;

import java.util.List;

public interface UserService {

    String COMPONENTNAME = "USR";

    Group createGroup(String name);

    Privilege createPrivilege(String componentName, String name, String description);

    Optional<User> findUser(String authenticationName);

    Optional<Group> findGroup(String name);

    Optional<Group> getGroup(long id);

    Optional<User> getUser(long id);

    Optional<Privilege> getPrivilege(String privilegeName);

    Optional<User> authenticateBase64(String base64String);

    List<Group> getGroups();

    String getRealm();

    Query<User> getUserQuery();

    Group newGroup(String name);

    List<Privilege> getPrivileges();

    UserDirectory createInternalDirectory(String domain);

    Optional<UserDirectory> findUserDirectory(String domain);

    UserDirectory findDefaultUserDirectory();

    LdapUserDirectory createActiveDirectory(String domain);

    LdapUserDirectory createApacheDirectory(String domain);
}
