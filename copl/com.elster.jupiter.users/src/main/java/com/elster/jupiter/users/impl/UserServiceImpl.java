package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.xml.bind.DatatypeConverter;

@Component(
        name = "com.elster.jupiter.users",
        service = {UserService.class, InstallService.class},
        immediate = true,
        property = "name=" + Bus.COMPONENTNAME)
public class UserServiceImpl implements UserService, InstallService, ServiceLocator {
    private volatile OrmClient ormClient;
    private volatile TransactionService transactionService;
    private volatile QueryService queryService;

	public void activate(ComponentContext context) {
		Bus.setServiceLocator(this);
	}

    public Optional<User> authenticate(String userName, String password) {
        return findUser(userName);
    }

    @Override
    public Optional<User> authenticateBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return Optional.absent();
        }
        String plainText = new String(DatatypeConverter.parseBase64Binary(base64));
        String[] names = plainText.split(":");
        return authenticate(names[0], names.length > 0 ? null : names[1]);
    }

    @Override
    public Group createGroup(String name) {
        GroupImpl result = new GroupImpl(name);
        result.persist();
        return result;
    }

    @Override
    public Privilege createPrivilege(String componentName, String privilegeName, String description) {
        PrivilegeImpl result = new PrivilegeImpl(componentName, privilegeName, description);
        result.persist();
        return result;
    }
	
	@Override
	public User createUser(String authenticationName, String description) {
		UserImpl result = new UserImpl(authenticationName,description);
		result.save();
		return result;
	}
	
	public void deactivate(ComponentContext context) {
		Bus.setServiceLocator(null);
	}

    @Override
    public Optional<Group> findGroup(String name) {
        return groupFactory().getUnique("name", name);
    }

    @Override
    public Optional<User> findUser(String authenticationName) {
        return userFactory().getUnique("authenticationName", authenticationName);
	}

    @Override
    public Optional<Group> getGroup(long id) {
        return groupFactory().get(id);
    }

    @Override
    public Query<Group> getGroupQuery() {
        return getQueryService().wrap(groupFactory().with(getOrmClient().getPrivilegeInGroupFactory(), getOrmClient().getPrivilegeFactory()));
    }

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Override
    public Optional<Privilege> getPrivilege(String privilegeName) {
        return Bus.getOrmClient().getPrivilegeFactory().get(privilegeName);
    }

    @Override
    public Query<Privilege> getPrivilegeQuery() {
        return getQueryService().wrap(Bus.getOrmClient().getPrivilegeFactory().with());
    }

    public QueryService getQueryService() {
        return queryService;
    }

    @Override
    public String getRealm() {
        return "Jupiter";
    }

    @Override
    public TransactionService getTransactionService() {
        return transactionService;
    }

    @Override
    public Optional<User> getUser(long id) {
        return userFactory().get(id);
    }

    @Override
    public Query<User> getUserQuery() {
        return getQueryService().wrap(userFactory().with());
    }

	public void install() {
		new InstallerImpl().install();		
	}

    @Override
    public Group newGroup(String name) {
        return new GroupImpl(name);
    }

    @Override
    public User newUser(String name) {
        return new UserImpl(name);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "User Management");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        this.ormClient = new OrmClientImpl(dataModel);
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private DataMapper<Group> groupFactory() {
        return Bus.getOrmClient().getGroupFactory();
    }

    private DataMapper<User> userFactory() {
        return Bus.getOrmClient().getUserFactory();
    }
}
