package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.users.*;

interface OrmClient {
	DataMapper<PrivilegeDescription> getPrivilegeDescriptionFactory();
	DataMapper<Role> getRoleFactory();
	DataMapper<User> getUserFactory();
	DataMapper<PrivilegeInRole> getPrivilegeInRoleFactory();	
	DataMapper<UserInRole> getUserInRoleFactory();	
	void install(boolean executeDdl , boolean storeMappings);		
}