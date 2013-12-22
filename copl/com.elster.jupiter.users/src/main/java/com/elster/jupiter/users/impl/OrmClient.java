package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;

interface OrmClient {
	DataMapper<Privilege> getPrivilegeFactory();
	DataMapper<Group> getGroupFactory();
	DataMapper<User> getUserFactory();
	DataMapper<PrivilegeInGroup> getPrivilegeInGroupFactory();	
	DataMapper<UserInGroup> getUserInGroupFactory();	
	void install(boolean executeDdl , boolean storeMappings);
	DataModel getDataModel();		
}