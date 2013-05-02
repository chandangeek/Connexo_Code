package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.users.*;

import static com.elster.jupiter.users.impl.TableSpecs.*;

class OrmClientImpl implements OrmClient {

	private final DataModel dataModel;
	
	OrmClientImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}
	
	@Override
	public DataMapper<PrivilegeDescription> getPrivilegeDescriptionFactory() {
		return dataModel.getDataMapper(PrivilegeDescription.class, PrivilegeDescriptionImpl.class, USR_PRIVILEGES.name());
	}

	@Override
	public DataMapper<Role> getRoleFactory() {
		return dataModel.getDataMapper(Role.class, RoleImpl.class, USR_ROLES.name());
	}
	
	@Override
	public DataMapper<User> getUserFactory() {
		return dataModel.getDataMapper(User.class, UserImpl.class, USR_USERS.name());
	}
	
	@Override
	public DataMapper<PrivilegeInRole> getPrivilegeInRoleFactory() {
		return dataModel.getDataMapper(PrivilegeInRole.class, PrivilegeInRole.class, USR_PRIVILEGEINROLE.name());
	}

	@Override
	public DataMapper<UserInRole> getUserInRoleFactory() {
		return dataModel.getDataMapper(UserInRole.class, UserInRole.class, USR_USERINROLE.name());
	}

	@Override
	public void install(boolean executeDdl,boolean saveMappings) {
		dataModel.install(executeDdl,saveMappings);		
	}
	
}
