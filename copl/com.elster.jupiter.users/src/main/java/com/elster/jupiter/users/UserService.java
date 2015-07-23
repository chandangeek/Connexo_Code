package com.elster.jupiter.users;

import com.elster.jupiter.domain.util.Query;

import java.util.List;
import java.util.Optional;

public interface UserService {

    String COMPONENTNAME = "USR";

    String DEFAULT_ADMIN_ROLE = "Administrators";
    String DEFAULT_ADMIN_ROLE_DESCRIPTION = "Administrative privileges";
    String BATCH_EXECUTOR_ROLE = "Batch executors";
    String BATCH_EXECUTOR_ROLE_DESCRIPTION = "Batch executors privileges";

    User createUser(String name, String description);

    User createApacheDirectoryUser(String name, String domain);

    User createActiveDirectoryUser(String name, String domain);

    Group createGroup(String name, String description);

    void grantGroupWithPrivilege(String roleName, String applicationName, String[] privileges);

    Optional<User> findUser(String authenticationName);

    Optional<Resource> findResource(String name);

    Optional<Group> findGroup(String name);

    Optional<Group> getGroup(long id);

    Optional<User> getUser(long id);

    Optional<Privilege> getPrivilege(String privilegeName);

    Optional<Resource> getResource(String resourceName);

    Optional<User> authenticateBase64(String base64String);

    List<Group> getGroups();

    String getRealm();

    Query<User> getUserQuery();

    Query<Group> getGroupQuery();

    Query<Privilege> getPrivilegeQuery();

    Query<Resource> getResourceQuery();

    Group newGroup(String name, String description);

    List<Privilege> getPrivileges();

    List<Privilege> getPrivileges(String applicationName);


    List<Resource> getResources();

    List<Resource> getResources(String component);

    UserDirectory createInternalDirectory(String domain);

    Optional<UserDirectory> findUserDirectory(String domain);

    UserDirectory findDefaultUserDirectory();

    LdapUserDirectory createActiveDirectory(String domain);

    LdapUserDirectory createApacheDirectory(String domain);

    User findOrCreateUser(String name, String domain, String directoryType);

    Group findOrCreateGroup(String group);

    List<UserDirectory> getUserDirectories();

    UserPreferencesService getUserPreferencesService();

    ResourceDefinition createModuleResourceWithPrivileges(String moduleName, String resourceName, String resourceDescription, List<String> privileges);

    void saveResourceWithPrivileges(String moduleName, String name, String description, String[] privileges);

    Optional<User> getLoggedInUser(long userId);

    void addLoggedInUser(User user);

    void removeLoggedUser(User user);
}
