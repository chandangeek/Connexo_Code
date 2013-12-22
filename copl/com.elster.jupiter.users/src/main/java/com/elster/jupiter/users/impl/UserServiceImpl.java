package com.elster.jupiter.users.impl;

import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

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
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.google.common.base.Optional;

@Component(
        name = "com.elster.jupiter.users",
        service = {UserService.class, InstallService.class},
        immediate = true,
        property = "name=" + Bus.COMPONENTNAME)
public class UserServiceImpl implements UserService, InstallService, ServiceLocator {
	private volatile DataModel dataModel;
    private volatile OrmClient ormClient;
    private volatile TransactionService transactionService;
    private volatile QueryService queryService;

    public UserServiceImpl() {
    }
    
    @Inject
    public UserServiceImpl(OrmService ormService, TransactionService transactionService, QueryService queryService) {
    	setTransactionService(transactionService);
    	setQueryService(queryService);
    	setOrmService(ormService);
    	activate();
    	install();
    }
    
    @Activate
	public void activate() {
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
	
	@Deactivate
	public void deactivate() {
		Bus.clearServiceLocator(this);
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
    	Condition condition = Operator.EQUAL.compare("authenticationName",authenticationName);
        List<User> users = userFactory().with(Bus.getOrmClient().getUserInGroupFactory()).select(condition,new String[] {}, true, new String[] {});
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
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Override
    public Optional<Privilege> getPrivilege(String privilegeName) {
        return Bus.getOrmClient().getPrivilegeFactory().getOptional(privilegeName);
    }

    @Override
    public List<Privilege> getPrivileges() {
        return Bus.getOrmClient().getPrivilegeFactory().find();
    }

    public QueryService getQueryService() {
        return queryService;
    }

    @Override
    public String getRealm() {
        return Bus.REALM;
    }

    @Override
    public TransactionService getTransactionService() {
        return transactionService;
    }

    @Override
    public Optional<User> getUser(long id) {
        return userFactory().getOptional(id);
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
        dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "User Management");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        dataModel.register();
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

    private DataMapper<User> userFactory() {
        return Bus.getOrmClient().getUserFactory();
    }


}
