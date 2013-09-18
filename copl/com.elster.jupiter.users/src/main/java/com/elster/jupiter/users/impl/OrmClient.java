package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.users.*;

interface OrmClient {
	TypeCache<Privilege> getPrivilegeFactory();
	TypeCache<Group> getGroupFactory();
	DataMapper<User> getUserFactory();
	DataMapper<PrivilegeInGroup> getPrivilegeInGroupFactory();	
	DataMapper<UserInGroup> getUserInGroupFactory();	
	void install(boolean executeDdl , boolean storeMappings);
	DataModel getDataModel();		
}