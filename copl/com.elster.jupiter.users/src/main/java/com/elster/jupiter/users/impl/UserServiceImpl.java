package com.elster.jupiter.users.impl;

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

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "User Management");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        this.ormClient = new OrmClientImpl(dataModel);
    }

	public void activate(ComponentContext context) {
		Bus.setServiceLocator(this);
	}
	
	public void deactivate(ComponentContext context) {
		Bus.setServiceLocator(null);
	}
	
	@Override
	public User createUser(String authenticationName, String firstName,String lastName) {
		UserImpl result = new UserImpl(authenticationName,firstName,lastName);
		result.save();
		return result;
	}

    @Override
    public TransactionService getTransactionService() {
        return transactionService;
    }


    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
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
    public User findUser(String authenticationName) {
		User user = Bus.getOrmClient().getUserFactory().getUnique("authenticationName",authenticationName);
		if (user == null) {
			System.out.println("User " + authenticationName + " not found");
		}
		return user;		
	}


    @Override
    public Group findGroup(String name) {
        return Bus.getOrmClient().getGroupFactory().getUnique("name", name);
    }

    @Override
    public Optional<Privilege> getPrivilege(String privilegeName) {
        return Bus.getOrmClient().getPrivilegeFactory().get(privilegeName);
    }

    @Override
    public User authenticateBase64(String base64) {
        if (base64 == null || base64.length() == 0) {
            return null;
        }
        String plainText = new String(DatatypeConverter.parseBase64Binary(base64));
        String[] names = plainText.split(":");
        return authenticate(names[0], names.length > 0 ? null : names[1]);
    }


	public void install() {
		new InstallerImpl().install();		
	}

    public User authenticate(String userName, String password) {
        return findUser(userName);
    }

    @Override
    public String getRealm() {
        return "Jupiter";
    }


}
