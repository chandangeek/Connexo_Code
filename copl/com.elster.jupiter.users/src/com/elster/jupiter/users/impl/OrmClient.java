package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.users.*;

interface OrmClient {
	DataMapper<Privilege> getPrivilegeFactory();
	DataMapper<Group> getGroupFactory();
	DataMapper<User> getUserFactory();
	DataMapper<PrivilegeInGroup> getPrivilegeInGroupFactory();	
	DataMapper<UserInGroup> getUserInGroupFactory();	
	void install(boolean executeDdl , boolean storeMappings);		
}