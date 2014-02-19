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

@Component(
        name = "com.elster.jupiter.users",
        service = {UserService.class, InstallService.class},
        immediate = true,
        property = "name=" + UserService.COMPONENTNAME)
public class UserServiceImpl implements UserService, InstallService {

    private static final String REALM = "Jupiter";
    private volatile DataModel dataModel;
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

    public Optional<User> authenticate(String userName, String password) {
        //TODO check password *
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
	
	@Override
	public User createUser(String authenticationName, String description) {
		UserImpl result = UserImpl.from(dataModel, authenticationName, description);
		result.save();
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
    	Condition condition = Operator.EQUAL.compare("authenticationName",authenticationName);
    	List<User> users = dataModel.query(User.class,UserInGroup.class).select(condition);
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
    public String getRealm() {
        return REALM;
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
		new InstallerImpl(dataModel).install();
	}

    @Override
    public Group newGroup(String name) {
        return GroupImpl.from(dataModel, name);
    }

    @Override
    public User newUser(String name) {
        return UserImpl.from(dataModel, name);
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

    private DataMapper<User> userFactory() {
        return dataModel.mapper(User.class);
    }


}
