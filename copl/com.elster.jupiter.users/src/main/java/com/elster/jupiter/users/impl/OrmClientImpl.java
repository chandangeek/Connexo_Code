package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;

class OrmClientImpl implements OrmClient {

	private final DataModel dataModel;
	
	OrmClientImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}
	
	@Override
	public DataMapper<Privilege> getPrivilegeFactory() {
		return dataModel.mapper(Privilege.class);
	}

	@Override
	public DataMapper<Group> getGroupFactory() {
		return dataModel.mapper(Group.class);
	}
	
	@Override
	public DataMapper<User> getUserFactory() {
		return dataModel.mapper(User.class);
	}
	
	@Override
	public DataMapper<PrivilegeInGroup> getPrivilegeInGroupFactory() {
		return dataModel.mapper(PrivilegeInGroup.class);
	}

	@Override
	public DataMapper<UserInGroup> getUserInGroupFactory() {
		return dataModel.mapper(UserInGroup.class);
	}

	@Override
	public void install(boolean executeDdl,boolean saveMappings) {
		dataModel.install(executeDdl,saveMappings);		
	}

	@Override
	public DataModel getDataModel() {
		return dataModel;
	}
	
}
