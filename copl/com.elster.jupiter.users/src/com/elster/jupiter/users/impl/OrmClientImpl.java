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
	public DataMapper<Privilege> getPrivilegeFactory() {
		return dataModel.getDataMapper(Privilege.class, PrivilegeImpl.class, USR_PRIVILEGE.name());
	}

	@Override
	public DataMapper<Group> getGroupFactory() {
		return dataModel.getDataMapper(Group.class, GroupImpl.class, USR_GROUP.name());
	}
	
	@Override
	public DataMapper<User> getUserFactory() {
		return dataModel.getDataMapper(User.class, UserImpl.class, USR_USER.name());
	}
	
	@Override
	public DataMapper<PrivilegeInGroup> getPrivilegeInGroupFactory() {
		return dataModel.getDataMapper(PrivilegeInGroup.class, PrivilegeInGroup.class, USR_PRIVILEGEINGROUP.name());
	}

	@Override
	public DataMapper<UserInGroup> getUserInGroupFactory() {
		return dataModel.getDataMapper(UserInGroup.class, UserInGroup.class, USR_USERINGROUP.name());
	}

	@Override
	public void install(boolean executeDdl,boolean saveMappings) {
		dataModel.install(executeDdl,saveMappings);		
	}
	
}
