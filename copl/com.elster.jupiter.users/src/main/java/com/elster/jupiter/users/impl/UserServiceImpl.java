package com.elster.jupiter.users.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.ApplicationPrivilegesProvider;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.NoDefaultDomainException;
import com.elster.jupiter.users.NoDomainFoundException;
import com.elster.jupiter.users.NoDomainIdFoundException;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Checks.is;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(
        name = "com.elster.jupiter.users",
        service = {UserService.class, InstallService.class, MessageSeedProvider.class, TranslationKeyProvider.class, PrivilegesProvider.class},
        immediate = true,
        property = "name=" + UserService.COMPONENTNAME)
public class UserServiceImpl implements UserService, InstallService, MessageSeedProvider, TranslationKeyProvider, PrivilegesProvider {

    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile QueryService queryService;
    private volatile Thesaurus thesaurus;
    private volatile UserPreferencesService userPreferencesService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private List<User> loggedInUsers = new ArrayList<>();
    private volatile DataVaultService dataVaultService;


    private static final String TRUSTSTORE_PATH = "com.elster.jupiter.users.truststore";
    private static final String TRUSTSTORE_PASS = "com.elster.jupiter.users.truststorepass";

    private static final String JUPITER_REALM = "Local";

    private class PrivilegeAssociation{
        public String group;
        public String application;
        PrivilegeAssociation(String group, String application){
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
    public UserServiceImpl(OrmService ormService, TransactionService transactionService, QueryService queryService, NlsService nlsService, ThreadPrincipalService threadPrincipalService, DataVaultService dataVaultService) {
        this();
        setTransactionService(transactionService);
        setQueryService(queryService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setDataVaultService(dataVaultService);
        setThreadPrincipalService(threadPrincipalService);
        activate(null);
        if (!dataModel.isInstalled()) {
            installDataModel(true);
        }
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
            }
        });
        userPreferencesService = new UserPreferencesServiceImpl(dataModel);
    }

    public Optional<User> authenticate(String domain, String userName, String password) {
        UserDirectory userDirectory = is(domain).empty() ? findDefaultUserDirectory() : getUserDirectory(domain);
        return userDirectory.authenticate(userName, password);
    }

    private void setTrustStore(BundleContext bundleContext) {
        String trustStorePath = bundleContext.getProperty(TRUSTSTORE_PATH);
        String trustStorePass = bundleContext.getProperty(TRUSTSTORE_PASS);
        if ((trustStorePath != null) && (trustStorePass != null)) {
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePass);
        }
    }

    private UserDirectory getUserDirectory(String domain) {
        List<UserDirectory> found = dataModel.query(UserDirectory.class).select(Operator.EQUALIGNORECASE.compare("name", domain));
        if (found.isEmpty()) {
            throw new NoDomainFoundException(thesaurus, domain);
        }

        return found.get(0);
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
        List<UserDirectory> found = dataModel.query(UserDirectory.class).select(Operator.EQUAL.compare("isDefault", true));
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
        if (base64 == null || base64.isEmpty()) {
            return Optional.empty();
        }
        String plainText = new String(Base64.getDecoder().decode(base64));
        String[] names = plainText.split(":");

        if (names.length <= 1) {
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

        return authenticate(domain, userName, names[1]);
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
        UserImpl result = directory.newUser(name, domain, false,status);
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
                    }
                    catch(DoesNotExistException e){
                        System.out.println("Privilege " + privilege + " not yet registered; grant is delayed for " + groupName + "on " + applicationName);
                        if(!privilegesNotYetRegistered.containsKey(privilege)){
                            privilegesNotYetRegistered.put(privilege, new ArrayList<>());
                        }
                        privilegesNotYetRegistered.get(privilege).add(new PrivilegeAssociation(groupName, applicationName));
                    }
                }
            }
        }
    }


    private Resource createResource(String component, String name, String description) {
        ResourceImpl result = ResourceImpl.from(dataModel, component, name, description);
        result.persist();

        return result;
    }

    @Deactivate
    public void deactivate() {
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
        List<User> users = dataModel.query(User.class, UserDirectory.class).select(userCondition.and(userDirectoryCondition));
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
        // check if dataModel is installed because this method can be/us called before the install is run
        return dataModel.isInstalled() ? privilegeFactory().getOptional(privilegeName) : Optional.<Privilege>empty();
    }

    public Optional<Resource> getResource(String resourceName) {
        // check if dataModel is installed because this method can be/us called before the install is run
        return dataModel.isInstalled() ? resourceFactory().getOptional(resourceName) : Optional.<Resource>empty();
    }

    private DataMapper<Privilege> privilegeFactory() {
        return dataModel.mapper(Privilege.class);
    }

    @Override
    public List<Privilege> getPrivileges(String applicationName) {
        // check if dataModel is installed because this method can be/us called before the install is run
        if (dataModel.isInstalled()) {
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
        } else {
            return Collections.emptyList();
        }
    }


    @Override
    public List<Privilege> getPrivileges() {
        return privilegeFactory().find();
    }

    private DataMapper<Resource> resourceFactory() {
        return dataModel.mapper(Resource.class);
    }

    @Override
    public List<Resource> getResources() {
        return getApplicationResources();
    }

    @Override
    public List<Resource> getResources(String component) {
        // check if dataModel is installed because this method can be/us called before the install is run
        return dataModel.isInstalled() ? resourceFactory().find("componentName", component) : Collections.emptyList();
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

    public void install() {
        installDataModel(false);
    }

    private void installDataModel(boolean inTest) {
        synchronized (privilegeProviderRegistrationLock) {
            InstallerImpl installer = new InstallerImpl(dataModel, this);
            installer.install(getRealm());
            if (inTest) {
                doInstallPrivileges(this);
            }
            installPrivileges();
            installer.addDefaults();
        }
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
    public List<String> getPrerequisiteModules() {
        return Collections.singletonList("ORM");
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
        List<UserDirectory> found = dataModel.query(UserDirectory.class).select(Operator.EQUALIGNORECASE.compare("name", domain));
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

    @Reference(name = "ModulePrivilegesProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    @SuppressWarnings("unused")
    public void addModulePrivileges(PrivilegesProvider privilegesProvider) {
        synchronized (privilegeProviderRegistrationLock) {
            if (dataModel.isInstalled()) {
                try {
                    transactionService.builder().principal(() -> "Jupiter Installer").action("INSTALL-privilege").module(getModuleName()).run(() -> {
                        doInstallPrivileges(privilegesProvider);
                        doAssignPrivileges(privilegesProvider);
                    });
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            privilegesProviders.add(privilegesProvider);
        }
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

    @SuppressWarnings("unused")
    public void removeApplicationPrivileges(ApplicationPrivilegesProvider applicationPrivilegesProvider) {
        applicationPrivilegesProviders.remove(applicationPrivilegesProvider);
    }

    private void installPrivileges() {
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

    private void doInstallPrivileges(PrivilegesProvider privilegesProvider) {
        for (ResourceDefinition resource : privilegesProvider.getModuleResources()) {
            saveResourceWithPrivileges(resource.getComponentName(),
                    resource.getName(),
                    resource.getDescription(),
                    resource.getPrivilegeNames().stream().toArray(String[]::new));
        }
    }

    private void doAssignPrivileges(PrivilegesProvider privilegesProvider) {
        if(!privilegesNotYetRegistered.isEmpty()) {
            privilegesProvider.getModuleResources().stream()
                    .map(resource -> resource.getPrivilegeNames())
                    .flatMap(privileges -> privileges.stream())
                    .filter(item -> privilegesNotYetRegistered.containsKey(item))
                    .forEach(privilege -> {
                        for(PrivilegeAssociation association : privilegesNotYetRegistered.get(privilege)) {
                            Optional<Group> group = findGroup(association.group);
                            if (group.isPresent()) {
                                System.out.println("Granting " + privilege + " to " + association.group + "on " + association.application);
                                group.get().grant(association.application, privilege);
                            }
                        }
                        privilegesNotYetRegistered.remove(privilege);
                    });
        }
    }

    public List<Resource> getApplicationResources(String applicationName) {
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

    public List<Resource> getApplicationResources() {
        return applicationPrivilegesProviders.stream()
                .flatMap(a -> getApplicationResources(a.getApplicationName()).stream()).collect(Collectors.toList());
    }

    @Override
    public ResourceDefinition createModuleResourceWithPrivileges(String moduleName, String resourceName, String resourceDescription, List<String> privileges) {
        return ResourceDefinitionImpl.createResourceDefinition(moduleName, resourceName, resourceDescription, privileges);
    }

    private DataMapper<User> userFactory() {
        return dataModel.mapper(User.class);
    }

    @Override
    public String getModuleName() {
        return UserService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(createModuleResourceWithPrivileges(
                UserService.COMPONENTNAME,
                Privileges.RESOURCE_USERS.getKey(), Privileges.RESOURCE_USERS_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTRATE_USER_ROLE, Privileges.Constants.VIEW_USER_ROLE)));

        return resources;
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
        return membership.stream().map(s -> s.getUser()).collect(Collectors.toList());
    }

    @Override
    public Query<Group> getGroupsQuery() {
        return queryService.wrap(dataModel.query(Group.class));
    }

    @Override
    public Optional<User> getLoggedInUser(long userId) {
        Optional<User> found = this.loggedInUsers.stream().filter(user -> (user.getId() == userId)).findFirst();
        if(!found.isPresent()){
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

}
