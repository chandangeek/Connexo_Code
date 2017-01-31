/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DoesNotExistException;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.ApplicationPrivilegesProvider;
import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.NoDefaultDomainException;
import com.elster.jupiter.users.NoDomainFoundException;
import com.elster.jupiter.users.NoDomainIdFoundException;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.ResourceBuilder;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.security.Principal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;
import static com.elster.jupiter.util.Checks.is;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(
        name = "com.elster.jupiter.users",
        service = {UserService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true,
        property = "name=" + UserService.COMPONENTNAME)
public class UserServiceImpl implements UserService, MessageSeedProvider, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile QueryService queryService;
    private volatile Thesaurus thesaurus;
    private volatile UserPreferencesService userPreferencesService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private List<User> loggedInUsers = new CopyOnWriteArrayList<>();
    private volatile DataVaultService dataVaultService;
    private volatile UpgradeService upgradeService;
    private volatile Publisher publisher;
    private volatile Clock clock;

    private static final String TRUSTSTORE_PATH = "com.elster.jupiter.users.truststore";
    private static final String TRUSTSTORE_PASS = "com.elster.jupiter.users.truststorepass";
    private static final String SUCCESSFUL_LOGIN = "Successful login for user ";
    private static final String UNSUCCESSFUL_LOGIN = "Unsuccessful login attempt for user ";

    private static final String JUPITER_REALM = "Local";
    private Logger userLogin = Logger.getLogger("userLog");

    private class PrivilegeAssociation {
        public String group;
        public String application;

        PrivilegeAssociation(String group, String application) {
            this.group = group;
            this.application = application;
        }
    }

    private final Map<String, List<PrivilegeAssociation>> privilegesNotYetRegistered = new HashMap<>();

    @GuardedBy("privilegeProviderRegistrationLock")
    private final List<PrivilegesProvider> privilegesProviders = new ArrayList<>();

    private final List<ApplicationPrivilegesProvider> applicationPrivilegesProviders = new CopyOnWriteArrayList<>();

    public UserServiceImpl() {
        super();
    }

    private final Object privilegeProviderRegistrationLock = new Object();

    @Inject
    public UserServiceImpl(OrmService ormService, TransactionService transactionService, QueryService queryService, NlsService nlsService, ThreadPrincipalService threadPrincipalService, DataVaultService dataVaultService, UpgradeService upgradeService, BundleContext bundleContext, Publisher publisher) {
        this();
        setTransactionService(transactionService);
        setQueryService(queryService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setDataVaultService(dataVaultService);
        setThreadPrincipalService(threadPrincipalService);
        setUpgradeService(upgradeService);
        setPublisher(publisher);
        activate(bundleContext);
    }

    @Activate
    public void activate(BundleContext context) {
        if (context != null) {
            setTrustStore(context);
        }
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TransactionService.class).toInstance(transactionService);
                bind(QueryService.class).toInstance(queryService);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(DataVaultService.class).toInstance(dataVaultService);
                bind(UserService.class).toInstance(UserServiceImpl.this);
                bind(BundleContext.class).toInstance(context);
                bind(Publisher.class).toInstance(publisher);
            }
        });
        userPreferencesService = new UserPreferencesServiceImpl(dataModel);
        synchronized (privilegeProviderRegistrationLock) {
            upgradeService.register(identifier("Pulse", COMPONENTNAME), dataModel, InstallerImpl.class, ImmutableMap.of(
                    version(10, 2), UpgraderV10_2.class, version(10, 3), UpgraderV10_3.class
            ));
        }
    }

    public Optional<User> authenticate(String domain, String userName, String password, String ipAddr) {
        UserDirectory userDirectory = is(domain).empty() ? findDefaultUserDirectory() : getUserDirectory(domain, userName, ipAddr);
        return userDirectory.authenticate(userName, password);
    }

    @Override
    public LdapUserDirectory getLdapUserDirectory(long id) {
        Optional<LdapUserDirectory> found = dataModel.mapper(LdapUserDirectory.class).getOptional(id);
        if (!found.isPresent()) {
            throw new NoDomainIdFoundException(thesaurus, id);
        }
        return found.get();
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public List<UserDirectory> getUserDirectories() {
        return dataModel.mapper(UserDirectory.class).find();
    }

    @Override
    public Query<UserDirectory> getLdapDirectories() {
        return getQueryService().wrap(dataModel.query(UserDirectory.class));
    }

    @Override
    public UserDirectory findDefaultUserDirectory() {
        List<UserDirectory> found = dataModel.query(UserDirectory.class)
                .select(Operator.EQUAL.compare("isDefault", true));
        if (found.isEmpty()) {
            throw new NoDefaultDomainException(thesaurus);
        }
        return found.get(0);
    }

    @Override
    public List<User> getAllUsers(long id) {
        return dataModel.mapper(User.class).find()
                .stream()
                .filter(s -> s.getUserDirectoryId() == id)
                .sorted((s1, s2) -> s1.getName().toLowerCase().compareTo(s2.getName().toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> authenticateBase64(String base64) {
        return authenticateBase64(base64, null);
    }

    @Override
    public Optional<User> authenticateBase64(String base64, String ipAddr) {
        if (base64 == null || base64.isEmpty()) {
            return Optional.empty();
        }
        String plainText = new String(Base64.getDecoder().decode(base64));
        String[] names = plainText.split(":");

        if (names.length <= 1) {
            if (names.length == 1) {
                logMessage(UNSUCCESSFUL_LOGIN, names[0], null, ipAddr);
            }
            return Optional.empty();
        }

        String domain = null;
        String userName = names[0];

        String[] items = userName.split("/");
        if (items.length > 1) {
            domain = items[0];
            userName = items[1];
        }
        items = userName.split("\\\\");
        if (items.length > 1) {
            domain = items[0];
            userName = items[1];
        }
        Optional<User> user = authenticate(domain, userName, names[1], ipAddr);
        if (user.isPresent() && !user.get().getPrivileges().isEmpty()) {
            logMessage(SUCCESSFUL_LOGIN, userName, domain, ipAddr);
        } else {
            if (!userName.equals("")) {
                logMessage(UNSUCCESSFUL_LOGIN, userName, domain, ipAddr);
            }
        }
        return user;
    }

    @Override
    public User createUser(String name, String description) {
        InternalDirectoryImpl directory = (InternalDirectoryImpl) this.findUserDirectory(getRealm()).orElse(null);
        UserImpl result = directory.newUser(name, description, false, true);
        result.update();
        return result;
    }

    @Override
    public User createApacheDirectoryUser(String name, String domain, boolean status) {
        ApacheDirectoryImpl directory = (ApacheDirectoryImpl) this.findUserDirectory(domain).orElse(null);
        UserImpl result = directory.newUser(name, domain, false, status);
        result.update();

        return result;
    }

    @Override
    public User createActiveDirectoryUser(String name, String domain, boolean status) {
        ActiveDirectoryImpl directory = (ActiveDirectoryImpl) this.findUserDirectory(domain).orElse(null);
        UserImpl result = directory.newUser(name, domain, false, status);
        result.update();

        return result;
    }

    @Override
    public Group createGroup(String name, String description) {
        GroupImpl result = GroupImpl.from(dataModel, name, description);
        result.update();
        return result;
    }

    @Override
    public void grantGroupWithPrivilege(String groupName, String applicationName, String[] privileges) {
        synchronized (privilegeProviderRegistrationLock) {
            Optional<Group> group = findGroup(groupName);
            if (group.isPresent()) {
                for (String privilege : privileges) {
                    try {
                        group.get().grant(applicationName, privilege);
                    } catch (DoesNotExistException e) {
                        if (!privilegesNotYetRegistered.containsKey(privilege)) {
                            privilegesNotYetRegistered.put(privilege, new ArrayList<>());
                        }
                        privilegesNotYetRegistered.get(privilege)
                                .add(new PrivilegeAssociation(groupName, applicationName));
                    }
                }
            }
        }
    }



    @Override
    public Optional<Group> findGroup(String name) {
        for (Group group : getGroups()) {
            if (group.getName().equals(name)) {
                return Optional.of(group);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Resource> findResource(String name) {
        Condition condition = Operator.EQUALIGNORECASE.compare("name", name);
        List<Resource> resources = dataModel.query(Resource.class).select(condition);
        return resources.isEmpty() ? Optional.empty() : Optional.of(resources.get(0));
    }

    @Override
    public Optional<User> findUser(String authenticationName) {
        Condition condition = Operator.EQUALIGNORECASE.compare("authenticationName", authenticationName);
        List<User> users = dataModel.query(User.class, UserInGroup.class).select(condition);
        if (!users.isEmpty()) {
            if (users.get(0).getStatus()) {
                return Optional.of(users.get(0));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findUser(String authenticationName, String userDirectoryName) {
        Condition userCondition = Operator.EQUALIGNORECASE.compare("authenticationName", authenticationName);
        Condition userDirectoryCondition = Operator.EQUALIGNORECASE.compare("userDirectory.name", userDirectoryName);
        List<User> users = dataModel.query(User.class, UserDirectory.class)
                .select(userCondition.and(userDirectoryCondition));
        if (!users.isEmpty()) {
            if (users.get(0).getStatus()) {
                return Optional.of(users.get(0));
            }
        }
        return Optional.empty();
    }

    @Override
    public User findOrCreateUser(String name, String domain, String directoryType, boolean status) {
        Condition userCondition = Operator.EQUALIGNORECASE.compare("authenticationName", name);
        Condition domainCondition = Operator.EQUALIGNORECASE.compare("userDirectory.name", domain);
        List<User> users = dataModel.query(User.class, UserDirectory.class).select(userCondition.and(domainCondition));
        if (users.isEmpty()) {
            if (ApacheDirectoryImpl.TYPE_IDENTIFIER.equals(directoryType)) {
                return createApacheDirectoryUser(name, domain, status);
            }
            if (ActiveDirectoryImpl.TYPE_IDENTIFIER.equals(directoryType)) {
                return createActiveDirectoryUser(name, domain, status);
            }
        }
        if (!users.isEmpty()) {
            if (users.get(0).getStatus() != status) {
                users.get(0).setStatus(status);
                users.get(0).update();
            }
            return users.get(0);
        } else {
            return createUser(name, domain);
        }

    }

    @Override
    public User findOrCreateUser(String name, String domain, String directoryType) {
        return findOrCreateUser(name, domain, directoryType, true);
    }

    @Override
    public Group findOrCreateGroup(String name) {
        Condition groupCondition = Operator.EQUALIGNORECASE.compare("name", name);
        List<Group> groups = dataModel.query(Group.class).select(groupCondition);
        if (groups.isEmpty()) {
            return createGroup(name, "");
        }
        return groups.get(0);
    }

    @Override
    public Optional<Group> getGroup(long id) {
        return dataModel.mapper(Group.class).getOptional(id);
    }

    @Override
    public Optional<Group> findAndLockGroupByIdAndVersion(long id, long version) {
        return dataModel.mapper(Group.class).lockObjectIfVersion(version, id);
    }

    @Override
    public List<Group> getGroups() {
        return dataModel
                .query(Group.class, PrivilegeInGroup.class)
                .select(Condition.TRUE);
    }

    @Override
    public String getRealm() {
        return JUPITER_REALM;
    }

    @Override
    public Optional<Privilege> getPrivilege(String privilegeName) {
        return privilegeFactory().getOptional(privilegeName);
    }

    public Optional<GrantPrivilege> getGrantPrivilege(String privilegeName) {
        return dataModel.mapper(GrantPrivilege.class).getOptional(privilegeName);
    }

    public Optional<Resource> getResource(String resourceName) {
        return resourceFactory().getOptional(resourceName);
    }

    @Override
    public List<Privilege> getPrivileges(String applicationName) {
        List<String> applicationPrivileges = applicationPrivilegesProviders
                .stream()
                .filter(ap -> ap.getApplicationName().equalsIgnoreCase(applicationName))
                .flatMap(ap -> ap.getApplicationPrivileges().stream())
                .collect(Collectors.toList());
        return privilegeFactory()
                .find()
                .stream()
                .filter(p -> applicationPrivileges.contains(p.getName()))
                .collect(Collectors.toList());
    }


    @Override
    public List<Privilege> getPrivileges() {
        return privilegeFactory().find();
    }

    @Override
    public List<Resource> getResources() {
        return getApplicationResources();
    }

    @Override
    public List<Resource> getResources(String component) {
        return resourceFactory().find("componentName", component);
    }

    @Override
    public QueryService getQueryService() {
        return queryService;
    }

    @Override
    public DataVaultService getDataVaultService() {
        return dataVaultService;
    }

    @Override
    public Optional<User> getUser(long id) {
        return userFactory().getOptional(id);
    }

    @Override
    public Optional<User> findAndLockUserByIdAndVersion(long id, long version) {
        return userFactory().lockObjectIfVersion(version, id);
    }

    @Override
    public Query<User> getUserQuery() {
        return getQueryService().wrap(dataModel.query(User.class));
    }

    @Override
    public Query<Group> getGroupQuery() {
        return getQueryService().wrap(dataModel.query(Group.class));
    }

    @Override
    public Query<Privilege> getPrivilegeQuery() {
        return getQueryService().wrap(dataModel.query(Privilege.class));
    }

    @Override
    public Query<Resource> getResourceQuery() {
        return getQueryService().wrap(dataModel.query(Resource.class));
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }


    @Override
    public String getComponentName() {
        return UserService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public Group newGroup(String name, String description) {
        return GroupImpl.from(dataModel, name, description);
    }

    @Override
    public InternalDirectoryImpl createInternalDirectory(String domain) {
        return InternalDirectoryImpl.from(dataModel, domain);
    }

    @Override
    public ActiveDirectoryImpl createActiveDirectory(String domain) {
        return ActiveDirectoryImpl.from(dataModel, domain);
    }

    @Override
    public ApacheDirectoryImpl createApacheDirectory(String domain) {
        return ApacheDirectoryImpl.from(dataModel, domain);
    }

    @Override
    public Optional<UserDirectory> findUserDirectory(String domain) {
        List<UserDirectory> found = dataModel.query(UserDirectory.class).select(Operator.EQUAL.compare("name", domain));
        if (found.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(found.get(0));
        }
    }

    @Override
    public Optional<UserDirectory> findUserDirectoryIgnoreCase(String domain) {
        List<UserDirectory> found = dataModel.query(UserDirectory.class)
                .select(Operator.EQUALIGNORECASE.compare("name", domain));
        if (found.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(found.get(0));
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "User Management");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(UserService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public UserPreferencesService getUserPreferencesService() {
        return userPreferencesService;
    }

    @Reference
    public final void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setClockService(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void addModulePrivileges(PrivilegesProvider privilegesProvider) {
        synchronized (privilegeProviderRegistrationLock) {
            if (upgradeService.isInstalled(identifier("Pulse", COMPONENTNAME), version(1, 0))) {
                try {
                    doInstallPrivileges(privilegesProvider);
                    doAssignPrivileges(privilegesProvider);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            privilegesProviders.add(privilegesProvider);
        }
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @SuppressWarnings("unused")
    public void removeModulePrivileges(PrivilegesProvider privilegesProvider) {
        synchronized (privilegeProviderRegistrationLock) {
            privilegesProviders.remove(privilegesProvider);
        }
    }

    @Reference(name = "ApplicationPrivilegesProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    @SuppressWarnings("unused")
    public void addApplicationPrivileges(ApplicationPrivilegesProvider applicationPrivilegesProvider) {
        applicationPrivilegesProviders.add(applicationPrivilegesProvider);
    }

    @Reference
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    @SuppressWarnings("unused")
    public void removeApplicationPrivileges(ApplicationPrivilegesProvider applicationPrivilegesProvider) {
        applicationPrivilegesProviders.remove(applicationPrivilegesProvider);
    }

    void installPrivileges() {
        for (PrivilegesProvider privilegesProvider : privilegesProviders) {
            try {
                doInstallPrivileges(privilegesProvider);
            } catch (Exception e) {
                System.out.print("Fail to register for " + privilegesProvider.getModuleName() + " " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void saveResourceWithPrivileges(String moduleName, String name, String description, String[] privileges) {
        Optional<Resource> found = findResource(name);
        Resource resource = found.isPresent() ? found.get() : createResource(moduleName, name, description);
        if (!resource.getComponentName().equalsIgnoreCase(moduleName)) {
            resource.delete();
            resource = createResource(moduleName, name, description);
        }
        for (String privilege : privileges) {
            resource.createPrivilege(privilege);
        }
    }

    @Override
    public ResourceDefinition createModuleResourceWithPrivileges(String moduleName, String resourceName, String resourceDescription, List<String> privileges) {
        return ResourceDefinitionImpl.createResourceDefinition(moduleName, resourceName, resourceDescription, privileges);
    }

    @Override
    public Optional<Group> getGroup(String name) {
        return getGroupsQuery()
                .select(where("name").isEqualTo(name))
                .stream()
                .findFirst();
    }

    @Override
    public List<User> getGroupMembers(String groupName) {
        QueryExecutor<UserInGroup> queryExecutor = dataModel.query(UserInGroup.class, Group.class);
        List<UserInGroup> membership = queryExecutor.select(where("group.name").isEqualTo(groupName));
        return membership.stream().map(UserInGroup::getUser).collect(Collectors.toList());
    }

    @Override
    public Query<Group> getGroupsQuery() {
        return queryService.wrap(dataModel.query(Group.class));
    }

    @Override
    public Optional<User> getLoggedInUser(long userId) {
        Optional<User> found = this.loggedInUsers.stream().filter(user -> (user.getId() == userId)).findFirst();
        if (!found.isPresent()) {
            found = this.getUser(userId);
        }

        return found;
    }

    @Override
    public void addLoggedInUser(User user) {
        if (!this.loggedInUsers.contains(user)) {
            this.loggedInUsers.add(user);
        }
    }


    @Override
    public void removeLoggedUser(User user) {
        this.loggedInUsers.remove(user);
    }

    @Override
    public Optional<WorkGroup> getWorkGroup(long id){
        return dataModel.mapper(WorkGroup.class).getOptional(id);
    }

    @Override
    public Optional<WorkGroup> getWorkGroup(String name){
        return getWorkGroupsQuery()
                .select(where("name").isEqualTo(name))
                .stream()
                .findFirst();
    }

    @Override
    public WorkGroup createWorkGroup(String name, String description) {
        WorkGroupImpl workGroup = WorkGroupImpl.from(dataModel, name, description);
        workGroup.update();
        return workGroup;
    }

    @Override
    public Query<WorkGroup> getWorkGroupsQuery() {
        return queryService.wrap(dataModel.query(WorkGroup.class));
    }

    @Override
    public List<WorkGroup> getWorkGroups(){
        return dataModel.mapper(WorkGroup.class).find();
    }

    @Override
    public List<User> getUsers(){
        return dataModel.mapper(User.class).find();
    }

    @Override
    public Optional<WorkGroup> findAndLockWorkGroupByIdAndVersion(long id, long version) {
        return dataModel.mapper(WorkGroup.class).lockObjectIfVersion(version, id);
    }

    @Override
    public PrivilegeCategory createPrivilegeCategory(String name) {
        PrivilegeCategoryImpl category = PrivilegeCategoryImpl.of(dataModel, name);
        dataModel.mapper(PrivilegeCategory.class).persist(category);
        return category;
    }

    @Override
    public Optional<PrivilegeCategory> findPrivilegeCategory(String name) {
        return dataModel.mapper(PrivilegeCategory.class).getOptional(name);
    }

    @Override
    public PrivilegeCategory getDefaultPrivilegeCategory() {
        return findPrivilegeCategory(DEFAULT_CATEGORY_NAME).orElseThrow(() -> new IllegalStateException("Cannot get default privilege category before installation"));
    }

    @Override
    public ResourceBuilder buildResource() {
        return new ResourceBuilderImpl(dataModel);
    }

    @Override
    public Set<User> findUsers(Group group) {
        return dataModel.stream(UserInGroup.class)
                .filter(Operator.EQUAL.compare("groupId", group.getId()))
                .map(UserInGroup::getUser)
                .collect(Collectors.toSet());
    }

    void createDefaultPrivilegeCategory() {
        createPrivilegeCategory(DEFAULT_CATEGORY_NAME);
    }

    private DataMapper<Privilege> privilegeFactory() {
        return dataModel.mapper(Privilege.class);
    }

    private DataMapper<Resource> resourceFactory() {
        return dataModel.mapper(Resource.class);
    }

    private void doInstallPrivileges(PrivilegesProvider privilegesProvider) {
        for (ResourceDefinition resource : privilegesProvider.getModuleResources()) {
            saveResourceWithPrivileges(resource.getComponentName(),
                    resource.getName(),
                    resource.getDescription(),
                    resource.getPrivilegeNames().stream().toArray(String[]::new));
        }
    }

    private void doAssignPrivileges(PrivilegesProvider privilegesProvider) {
        if (!privilegesNotYetRegistered.isEmpty()) {
            privilegesProvider.getModuleResources().stream()
                    .map(ResourceDefinition::getPrivilegeNames)
                    .flatMap(Collection::stream)
                    .filter(privilegesNotYetRegistered::containsKey)
                    .forEach(privilege -> {
                        for (PrivilegeAssociation association : privilegesNotYetRegistered.get(privilege)) {
                            Optional<Group> group = findGroup(association.group);
                            if (group.isPresent()) {
                                System.out.println("Granting " + privilege + " to " + association.group + " on " + association.application);
                                group.get().grant(association.application, privilege);
                            }
                        }
                        privilegesNotYetRegistered.remove(privilege);
                    });
        }
    }

    private List<Resource> getApplicationResources(String applicationName) {
        List<String> applicationPrivileges = applicationPrivilegesProviders
                .stream()
                .filter(app -> app.getApplicationName().equals(applicationName))
                .flatMap(app -> app.getApplicationPrivileges().stream())
                .collect(Collectors.toList());

        return resourceFactory().find().stream()
                .filter(r -> r.getPrivileges()
                        .stream()
                        .anyMatch(p -> applicationPrivileges.contains(p.getName())))
                .map(r -> ResourceDefinitionImpl.createApplicationResource(applicationName,
                        applicationName,
                        r.getName(),
                        r.getDescription(),
                        r.getPrivileges()
                                .stream()
                                .filter(p -> applicationPrivileges.contains(p.getName()))
                                .collect(Collectors.toList()))
                ).collect(Collectors.toList());
    }

    private DataMapper<User> userFactory() {
        return dataModel.mapper(User.class);
    }

    private List<Resource> getApplicationResources() {
        return applicationPrivilegesProviders.stream()
                .flatMap(a -> getApplicationResources(a.getApplicationName()).stream()).collect(Collectors.toList());
    }

    private void logMessage(String message, String userName, String domain, String ipAddr) {
        try {
            this.threadPrincipalService.set(getPrincipal());
            ipAddr = "0:0:0:0:0:0:0:1".equals(ipAddr) ? "localhost" : ipAddr;
            if (message.equals(SUCCESSFUL_LOGIN)) {
                String userNameFormatted = domain == null ? findDefaultUserDirectory().getDomain() + "/" + userName : domain + "/" + userName;
                userLogin.log(Level.INFO, message + "[" + userNameFormatted + "] ", ipAddr);
                this.findUserIgnoreStatus(userName, domain).ifPresent(user -> {
                    try (TransactionContext context = transactionService.getContext()) {
                        user.setLastSuccessfulLogin(clock.instant());
                        context.commit();
                    }
                });
            } else {
                String userNameFormatted = domain == null ? findDefaultUserDirectory().getDomain() + "/" + userName : domain + "/" + userName;
                userLogin.log(Level.WARNING, message + "[" + userNameFormatted + "] ", ipAddr);
                this.findUserIgnoreStatus(userName, domain).ifPresent(user -> {
                    try (TransactionContext context = transactionService.getContext()) {
                        user.setLastUnSuccessfulLogin(clock.instant());
                        context.commit();
                    }
                });
            }
        } finally {
            this.threadPrincipalService.clear();
        }
    }

    private Optional<User> findUserIgnoreStatus(String authenticationName, String domain) {
        Condition authenticationNameCondition = Operator.EQUALIGNORECASE.compare("authenticationName", authenticationName);
        Condition userDirectoryCondition = Operator.EQUALIGNORECASE.compare("userDirectory.name", domain == null ? this.findDefaultUserDirectory().getDomain() : domain);
        List<User> users = dataModel.query(User.class, UserDirectory.class).select(authenticationNameCondition.and(userDirectoryCondition));
        if (!users.isEmpty()) {
            return Optional.of(users.get(0));
        }
        return Optional.empty();
    }

    private Principal getPrincipal() {
        return () -> "Authentication process";
    }

    private void setTrustStore(BundleContext bundleContext) {
        String trustStorePath = bundleContext.getProperty(TRUSTSTORE_PATH);
        String trustStorePass = bundleContext.getProperty(TRUSTSTORE_PASS);
        if ((trustStorePath != null) && (trustStorePass != null)) {
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePass);
        }
    }

    private UserDirectory getUserDirectory(String domain, String userName, String ipAddr) {
        List<UserDirectory> found = dataModel.query(UserDirectory.class)
                .select(Operator.EQUALIGNORECASE.compare("name", domain));
        if (found.isEmpty()) {
            logMessage(UNSUCCESSFUL_LOGIN, userName, domain, ipAddr);
            throw new NoDomainFoundException(thesaurus, domain);
        }

        return found.get(0);
    }

    Resource createResource(String component, String name, String description) {
        ResourceImpl result = ResourceImpl.from(dataModel, component, name, description);
        result.persist();
        return result;
    }

}