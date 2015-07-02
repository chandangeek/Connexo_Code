package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.*;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.*;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.Checks.is;

@Component(
        name = "com.elster.jupiter.users",
        service = {UserService.class, InstallService.class, TranslationKeyProvider.class, PrivilegesProvider.class},
        immediate = true,
        property = "name=" + UserService.COMPONENTNAME)
public class UserServiceImpl implements UserService, InstallService, TranslationKeyProvider, PrivilegesProvider {

    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile QueryService queryService;
    private volatile Thesaurus thesaurus;
    private volatile UserPreferencesService userPreferencesService;
    private volatile ThreadPrincipalService threadPrincipalService;


    private static final String JUPITER_REALM = "Local";

    //List<Resource> moduleResources = new ArrayList<>();//CopyOnWriteArrayList<>();
    //Map<String, List<String> >applicationResources = new ConcurrentHashMap<>();

    //Map<String, String> privilegeResource = new ConcurrentHashMap<>();

    private volatile List<PrivilegesProvider> privilegesProviders = new CopyOnWriteArrayList<>();

    private volatile List<ApplicationPrivilegesProvider> applicationPrivilegesProviders = new CopyOnWriteArrayList<>();

    public UserServiceImpl() {
    }

    @Inject
    public UserServiceImpl(OrmService ormService, TransactionService transactionService, QueryService queryService, NlsService nlsService) {
        setTransactionService(transactionService);
        setQueryService(queryService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setThreadPrincipalService(threadPrincipalService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TransactionService.class).toInstance(transactionService);
                bind(UserService.class).toInstance(UserServiceImpl.this);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(Thesaurus.class).toInstance(thesaurus);
            }
        });
        userPreferencesService = new UserPreferencesServiceImpl(dataModel);
    }

    public Optional<User> authenticate(String domain, String userName, String password) {
        UserDirectory userDirectory = is(domain).empty() ? findDefaultUserDirectory() : getUserDirectory(domain);
        return userDirectory.authenticate(userName, password);
    }

    private UserDirectory getUserDirectory(String domain) {
        Optional<UserDirectory> found = dataModel.mapper(UserDirectory.class).getOptional(domain);
        if (!found.isPresent()) {
            throw new NoDomainFoundException(thesaurus, domain);
        }

        return found.get();
    }

    @Override
    public List<UserDirectory> getUserDirectories() {
        return dataModel.mapper(UserDirectory.class).find();
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

        return authenticate(domain, userName, names[1]);
    }

    @Override
    public User createUser(String name, String description) {
        UserImpl result = createInternalDirectory(getRealm()).newUser(name, description, false);
        result.save();

        return result;
    }

    @Override
    public User createApacheDirectoryUser(String name, String domain) {
        UserImpl result = createApacheDirectory(domain).newUser(name, domain, false);
        result.save();

        return result;
    }

    @Override
    public User createActiveDirectoryUser(String name, String domain) {
        UserImpl result = createActiveDirectory(domain).newUser(name, domain, false);
        result.save();

        return result;
    }


    @Override
    public Group createGroup(String name, String description) {
        GroupImpl result = GroupImpl.from(dataModel, name, description);
        result.persist();

        return result;
    }

    @Override
    public void grantGroupWithPrivilege(String groupName, String applicationName, String[] privileges) {
        Optional<Group> group = findGroup(groupName);
        if (group.isPresent()) {
            for (String privilege : privileges) {
                group.get().grant(applicationName, privilege);
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
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public User findOrCreateUser(String name, String domain, String directoryType) {
        Condition userCondition = Operator.EQUALIGNORECASE.compare("authenticationName", name);
        Condition domainCondition = Operator.EQUALIGNORECASE.compare("userDirectory.domain", domain);
        List<User> users = dataModel.query(User.class, UserDirectory.class).select(userCondition.and(domainCondition));
        if (users.isEmpty()) {
            if (ApacheDirectoryImpl.TYPE_IDENTIFIER.equals(directoryType)) {
                return createApacheDirectoryUser(name, domain);
            }
            if (ActiveDirectoryImpl.TYPE_IDENTIFIER.equals(directoryType)) {
                return createActiveDirectoryUser(name, domain);
            }
            return createUser(name, domain);
        }
        return users.get(0);
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
    public List<Group> getGroups() {
        return dataModel.mapper(Group.class).find();
    }

    @Override
    public String getRealm() {
        return JUPITER_REALM;
    }

    @Override
    public Optional<Privilege> getPrivilege(String privilegeName) {
        return privilegeFactory().getOptional(privilegeName);
    }

    public Optional<Resource> getResource(String resourceName) {
        return resourceFactory().getOptional(resourceName);
    }

    private DataMapper<Privilege> privilegeFactory() {
        return dataModel.mapper(Privilege.class);
    }

    @Override
    public List<Privilege> getPrivileges(String applicationName) {
        List<String> applicationPrivileges = applicationPrivilegesProviders
                .stream()
                .filter(ap->ap.getApplicationName().equalsIgnoreCase(applicationName))
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

    private DataMapper<Resource> resourceFactory() {
        return dataModel.mapper(Resource.class);
    }

    @Override
    public List<Resource> getResources() {
        return getApplicationResources();
    }

    @Override
    public List<Resource> getResources(String component) {
        return resourceFactory().find("componentName", component);
    }

    public QueryService getQueryService() {
        return queryService;
    }

    @Override
    public Optional<User> getUser(long id) {
        return userFactory().getOptional(id);
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
        new InstallerImpl(dataModel).install(this, getRealm());
        installPrivileges();
    }
    
    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }
    
    @Override
    public String getComponentName() {
        return COMPONENTNAME;
    }
    
    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM");
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
        return dataModel.mapper(UserDirectory.class).getOptional(domain);
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

    private void clearPrincipal() {
        threadPrincipalService.clear();
    }

    private void setPrincipal() {
        threadPrincipalService.set(getPrincipal());
    }

    private Principal getPrincipal() {
        return new Principal() {

            @Override
            public String getName() {
                return "Jupiter Installer";
            }
        };
    }

    @Reference(name = "ModulePrivilegesProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addModulePrivileges(PrivilegesProvider privilegesProvider) {

        if (dataModel.isInstalled()) {
            try {
                setPrincipal();
                threadPrincipalService.set("INSTALL-privilege", privilegesProvider.getModuleName());
                transactionService.execute(()->{
                    doInstallPrivileges(privilegesProvider);
                    return null;
                });
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            } finally {
                clearPrincipal();
            }
        }

        privilegesProviders.add(privilegesProvider);
    }



    public void removeModulePrivileges(PrivilegesProvider privilegesProvider) {
        privilegesProviders.remove(privilegesProvider);

    }


    @Reference(name = "ApplicationPrivilegesProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addApplicationPrivileges(ApplicationPrivilegesProvider applicationPrivilegesProvider) {

        applicationPrivilegesProviders.add(applicationPrivilegesProvider);
    }

    public void removeApplicationPrivileges(ApplicationPrivilegesProvider applicationPrivilegesProvider) {

        applicationPrivilegesProviders.remove(applicationPrivilegesProvider);
    }

    void installResources() {
        for (ApplicationPrivilegesProvider privilegesProvider : applicationPrivilegesProviders) {
            doInstallApplicationPrivileges(privilegesProvider);
        }
    }

    private void doInstallApplicationPrivileges(ApplicationPrivilegesProvider applicationPrivilegesProvider) {
        /*for(String resource:applicationPrivilegesProvider.getApplicationPrivileges()){
            createPr(resource.getComponentName(),
                    resource.getName(),
                    resource.getDescription(),
                    resource.getPrivileges().stream().toArray(String[]::new));
        }*/
    }

    private void installPrivileges() {
        for (PrivilegesProvider privilegesProvider : privilegesProviders) {
            doInstallPrivileges(privilegesProvider);
        }
    }

    private void createOrUpdateResourceWithPrivileges(String component, String name, String description, String[] privileges) {
        Optional<Resource> found = findResource(name);
        Resource resource = found.isPresent() ? found.get() : createResource(component, name, description);
        if(!resource.getComponentName().equalsIgnoreCase(component)){
            resource.delete();
            resource = createResource(component, name, description);
        }
        for (String privilege : privileges) {
            resource.createPrivilege(privilege);
        }
    }

    private void doInstallPrivileges(PrivilegesProvider privilegesProvider) {
        for(ResourceDefinition resource:privilegesProvider.getModuleResources()){
            createOrUpdateResourceWithPrivileges(resource.getComponentName(),
                    resource.getName(),
                    resource.getDescription(),
                    resource.getPrivileges().stream().toArray(String[]::new));
        }
    }


    public List<Resource> getApplicationResources(String applicationName){

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

    public List<Resource> getApplicationResources(){

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
                "userAndRole.usersAndRoles", "userAndRole.usersAndRoles.description",
                Arrays.asList(Privileges.ADMINISTRATE_USER_ROLE, Privileges.VIEW_USER_ROLE)));

        return resources;
    }
}
