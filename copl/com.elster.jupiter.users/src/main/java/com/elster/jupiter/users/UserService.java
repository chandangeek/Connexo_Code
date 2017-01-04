package com.elster.jupiter.users;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.Thesaurus;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface UserService {

    String DEFAULT_CATEGORY_NAME = "Default";
    String COMPONENTNAME = "USR";

    String SYSTEM_ADMIN_ROLE = "System administrator";
    String SYSTEM_ADMIN_ROLE_DESCRIPTION = "System administrative privileges";
    String DEFAULT_ADMIN_ROLE = "User administrator";
    String DEFAULT_ADMIN_ROLE_DESCRIPTION = "User administrative privileges";
    String DEFAULT_INSTALLER_ROLE = "Installer";
    String DEFAULT_INSTALLER_ROLE_DESCRIPTION = "Installation privileges";
    String BATCH_EXECUTOR_ROLE = "Batch executor";
    String BATCH_EXECUTOR_ROLE_DESCRIPTION = "Batch executors privileges";

    User createUser(String name, String description);

    User createApacheDirectoryUser(String name, String domain,boolean status);

    User createActiveDirectoryUser(String name, String domain,boolean status);

    Group createGroup(String name, String description);

    void grantGroupWithPrivilege(String roleName, String applicationName, String[] privileges);

    Optional<User> findUser(String authenticationName);

    Optional<User> findUser(String authenticationName, String userDirectoryName);

    Optional<Resource> findResource(String name);

    Optional<Group> findGroup(String name);

    Optional<Group> getGroup(long id);

    Optional<Group> findAndLockGroupByIdAndVersion(long id, long version);
    /**
     * @return the group with specified name
     */
    Optional<Group> getGroup(String name);

    /**
     * @return query to the import schedules
     */

    Query<Group> getGroupsQuery();

    Optional<User> getUser(long id);

    Optional<User> findAndLockUserByIdAndVersion(long id, long version);

    Optional<Privilege> getPrivilege(String privilegeName);

    Optional<Resource> getResource(String resourceName);

    Optional<User> authenticateBase64(String base64String);

    Optional<User> authenticateBase64(String base64String, String ipAddr);

    List<Group> getGroups();

    List<User> getGroupMembers(String groupName);

    String getRealm();

    Query<User> getUserQuery();

    Query<Group> getGroupQuery();

    Query<Privilege> getPrivilegeQuery();

    Query<Resource> getResourceQuery();

    Group newGroup(String name, String description);

    List<Privilege> getPrivileges();

    List<Privilege> getPrivileges(String applicationName);

    public QueryService getQueryService();

    public DataVaultService getDataVaultService();

    List<Resource> getResources();

    List<Resource> getResources(String component);

    UserDirectory createInternalDirectory(String domain);

    Optional<UserDirectory> findUserDirectory(String domain);

    Optional<UserDirectory> findUserDirectoryIgnoreCase(String domain);

    UserDirectory findDefaultUserDirectory();

    List<User> getAllUsers(long id);

    LdapUserDirectory createActiveDirectory(String domain);

    LdapUserDirectory createApacheDirectory(String domain);

    User findOrCreateUser(String name, String domain, String directoryType,boolean status);

    User findOrCreateUser(String name, String domain, String directoryType);

    Group findOrCreateGroup(String group);

    List<UserDirectory> getUserDirectories();

    Query<UserDirectory> getLdapDirectories();

    LdapUserDirectory getLdapUserDirectory(long id);

    Thesaurus getThesaurus();

    UserPreferencesService getUserPreferencesService();

    ResourceDefinition createModuleResourceWithPrivileges(String moduleName, String resourceName, String resourceDescription, List<String> privileges);

    void addModulePrivileges(PrivilegesProvider privilegesProvider);

    void saveResourceWithPrivileges(String moduleName, String name, String description, String[] privileges);

    Optional<User> getLoggedInUser(long userId);

    void addLoggedInUser(User user);

    void removeLoggedUser(User user);

    Optional<WorkGroup> getWorkGroup(long id);

    Optional<WorkGroup> getWorkGroup(String name);

    WorkGroup createWorkGroup(String name, String description);

    Query<WorkGroup> getWorkGroupsQuery();

    List<WorkGroup> getWorkGroups();

    List<User> getUsers();

    Optional<WorkGroup> findAndLockWorkGroupByIdAndVersion(long id, long version);

    PrivilegeCategory createPrivilegeCategory(String name);

    Optional<PrivilegeCategory> findPrivilegeCategory(String name);

    PrivilegeCategory getDefaultPrivilegeCategory();

    ResourceBuilder buildResource();

    Set<User> findUsers(Group group);
}
