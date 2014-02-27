package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.*;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;

@Component(
        name = "com.elster.jupiter.users",
        service = {UserService.class, InstallService.class},
        immediate = true,
        property = "name=" + UserService.COMPONENTNAME)
public class UserServiceImpl implements UserService, InstallService {

    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile QueryService queryService;
    private volatile Thesaurus thesaurus;
    private static final String JUPITER_REALM = "Local";

    public UserServiceImpl() {
    }

    @Inject
    public UserServiceImpl(OrmService ormService, TransactionService transactionService, QueryService queryService) {
        setTransactionService(transactionService);
        setQueryService(queryService);
        setOrmService(ormService);
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
            }
        });
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
            return Optional.absent();
        }
        String plainText = new String(DatatypeConverter.parseBase64Binary(base64));
        String[] names = plainText.split(":");

        String domain = null;
        String userName = names[0];

        String[] items = userName.split("/");
        if(items.length > 1){
            domain = items[0];
            userName = items[1];
        }
        return authenticate(domain, userName, names.length > 0 ? names[1] : null);
    }

    @Override
    public User createInternalUser(String name, String description){
        UserImpl result = createInternalDirectory(getRealm()).newUser(name, description);
        result.save();
        return result;
    }

    @Override
    public Group createGroup(String name) {
        GroupImpl result = GroupImpl.from(dataModel, name);
        result.persist();
        return result;
    }

    @Override
    public Privilege createPrivilege(String componentName, String privilegeName, String description) {
        PrivilegeImpl result = PrivilegeImpl.from(dataModel, componentName, privilegeName, description);
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
        return Optional.<Group>absent();
    }

    @Override
    public Optional<User> findUser(String authenticationName) {
        Condition condition = Operator.EQUAL.compare("authenticationName", authenticationName);
        List<User> users = dataModel.query(User.class, UserInGroup.class).select(condition);
        return users.isEmpty() ? Optional.<User>absent() : Optional.of(users.get(0));
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

    private DataMapper<Privilege> privilegeFactory() {
        return dataModel.mapper(Privilege.class);
    }

    @Override
    public List<Privilege> getPrivileges() {
        return privilegeFactory().find();
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

    public void install() {
        new InstallerImpl(dataModel).install(getRealm());
    }

    @Override
    public Group newGroup(String name) {
        return GroupImpl.from(dataModel, name);
    }

    @Override
    public UserDirectory createInternalDirectory(String domain) {
        return InternalDirectoryImpl.from(dataModel, domain);
    }

    @Override
    public LdapUserDirectory createActiveDirectory(String domain) {
        return ActiveDirectoryImpl.from(dataModel, domain);
    }

    @Override
    public LdapUserDirectory createApacheDirectory(String domain) {
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

    private DataMapper<User> userFactory() {
        return dataModel.mapper(User.class);
    }


}
