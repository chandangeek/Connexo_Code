package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.users.*;

import static com.elster.jupiter.users.impl.TableSpecs.*;

class OrmClientImpl implements OrmClient {
	
	private static final String COMPONENTNAME = "USR";
	
	private final OrmService service;
	
	OrmClientImpl(OrmService service) {
		this.service = service;
	}

	
	@Override
	public DataMapper<PrivilegeDescription> getPrivilegeDescriptionFactory() {
		return service.getDataMapper(PrivilegeDescription.class, PrivilegeDescriptionImpl.class , COMPONENTNAME , USR_PRIVILEGES.name());
	}

	@Override
	public DataMapper<Role> getRoleFactory() {
		return service.getDataMapper(Role.class, RoleImpl.class , COMPONENTNAME , USR_ROLES.name());
	}
	
	@Override
	public DataMapper<User> getUserFactory() {
		return service.getDataMapper(User.class, UserImpl.class , COMPONENTNAME , USR_USERS.name());
	}
	
	@Override
	public DataMapper<PrivilegeInRole> getPrivilegeInRoleFactory() {
		return service.getDataMapper(PrivilegeInRole.class, PrivilegeInRole.class , COMPONENTNAME , USR_PRIVILEGEINROLE.name());
	}

	@Override
	public DataMapper<UserInRole> getUserInRoleFactory() {
		return service.getDataMapper(UserInRole.class, UserInRole.class , COMPONENTNAME , USR_USERINROLE.name());
	}

	@Override
	public void install(boolean executeDdl,boolean saveMappings) {
		service.install(createComponent(),executeDdl,saveMappings);		
	}
	
	private Component createComponent() {
		Component result = service.newComponent(COMPONENTNAME,"User admin");
		for (TableSpecs spec : TableSpecs.values()) {
			spec.addTo(result);			
		}
		return result;
	}
}
