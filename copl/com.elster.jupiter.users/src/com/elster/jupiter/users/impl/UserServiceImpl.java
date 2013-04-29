package com.elster.jupiter.users.impl;

import javax.xml.bind.DatatypeConverter;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeDescription;
import com.elster.jupiter.users.Role;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

@Component (name = "com.elster.jupiter.users" , service = UserService.class)
public class UserServiceImpl implements UserService , ServiceLocator {
	
	private volatile OrmClient ormClient;
	private volatile TransactionService transactionService;
	
	@Override
	public OrmClient getOrmClient() {
		return ormClient;
	}

	@Reference
	public void setOrmService(OrmService ormService) {
		this.ormClient = new OrmClientImpl(ormService);
	}

	@Override
	public TransactionService getTransactionService() {
		return transactionService;
	}

	@Reference
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@Activate
	public void activate() {
		Bus.setServiceLocator(this);
	}
	
	@Deactivate
	public void deActivate() {
		Bus.setServiceLocator(null);
	}
	
	@Override
	public User createUser(String authenticationName, String firstName,String lastName) {
		UserImpl result = new UserImpl(authenticationName,firstName,lastName);
		result.persist();
		return result;
	}

	@Override
	public Role createRole(String name) {
		RoleImpl result = new RoleImpl(name);
		result.persist();
		return result;
	}

	@Override
	public PrivilegeDescription createPrivilegeDescription(Privilege privilege,String description) {
		PrivilegeDescriptionImpl result = new PrivilegeDescriptionImpl(privilege, description);
		result.persist();
		return result;
	}

	@Override
	public User findUser(String authenticationName) {
		/*
		User user = Bus.getOrmClient().getUserFactory().getUnique("authenticationName",authenticationName);
		if (user == null) {
			System.out.println("User " + authenticationName + " not found");
		}
		*/
		return new UserImpl(authenticationName,"Karel","Haeck");
	}

	@Override
	public Role findRole(String roleName) {
		return Bus.getOrmClient().getRoleFactory().getUnique("name",roleName);
	}

	@Override
	public PrivilegeDescription findPrivilegeDescription(Privilege privilege) {
		return Bus.getOrmClient().getPrivilegeDescriptionFactory().get(privilege.getComponentName(),privilege.getId());
	}

	@Override
	public User authenticateBase64(String base64) {
		if (base64 == null || base64.length() == 0) {
			return null;
		}
		String plainText = new String(DatatypeConverter.parseBase64Binary(base64));
		String[] names = plainText.split(":");
		return authenticate(names[0] , names.length > 0 ? null : names[1]);
	}
	
	public User authenticate(String userName , String password) {
		return findUser(userName);
	}
	
	@Override
	public String getRealm() {
		return "Jupiter";
	}

}
