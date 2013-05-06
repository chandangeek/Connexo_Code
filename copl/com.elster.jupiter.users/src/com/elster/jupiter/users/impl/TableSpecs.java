package com.elster.jupiter.users.impl;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.*;

import com.elster.jupiter.orm.*;

public enum TableSpecs {
	USR_PRIVILEGES {
		void describeTable(Table table) {
			Column componentName = table.addColumn("COMPONENT","varchar2(3)",true,NOCONVERSION,"componentName");
			Column idColumn = table.addColumn("ID", "number" , true, NUMBER2LONG , "id");
			Column nameColumn = table.addColumn("NAME", "varchar2(80)" , true , NOCONVERSION , "name");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addPrimaryKeyConstraint("USR_PK_PRIVILEGES", componentName, idColumn);
			table.addUniqueConstraint("USR_U_PRIVILEGES", componentName , nameColumn);
		}
	},
	USR_ROLES {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			Column nameColumn = table.addColumn("NAME", "varchar2(80)" , true , NOCONVERSION , "name");
			table.addVersionCountColumn("VERSIONCOUNT", "number", "versionCount");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addModTimeColumn("MODTIME", "modTime");
			table.addPrimaryKeyConstraint("USR_PK_ROLES", idColumn);
			table.addUniqueConstraint("IDS_U_ROLES", nameColumn);
		}
	},
	USR_USERS {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			Column authenticationNameColumn = table.addColumn("AUTHNAME", "varchar2(80)" , true , NOCONVERSION , "authenticationName");
			table.addColumn("FIRSTNAME", "varchar2(80)" , true , NOCONVERSION , "firstName");
			table.addColumn("LASTNAME", "varchar2(80)" , true , NOCONVERSION , "lastName");
			table.addVersionCountColumn("VERSIONCOUNT", "number", "versionCount");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addModTimeColumn("MODTIME", "modTime");
			table.addPrimaryKeyConstraint("USR_PK_USERS", idColumn);
			table.addUniqueConstraint("USR_U_AUTHNAME" , authenticationNameColumn);			
		}
	},
	USR_PRIVILEGEINROLE {
		void describeTable(Table table) {
			Column roleIdColumn = table.addColumn("ROLEID", "number" , true, NUMBER2LONG , "roleId");
			Column componentName = table.addColumn("COMPONENT","varchar2(3)",true,NOCONVERSION,"componentName");
			Column privilegeIdColumn = table.addColumn("PRIVILEGEID", "number" , true, NUMBER2LONG , "id");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addPrimaryKeyConstraint("USR_PK_PRIVILEGEINROLE", roleIdColumn , componentName, privilegeIdColumn);		
			table.addForeignKeyConstraint("FK_PRIVROLE2ROLE", USR_ROLES.name(), CASCADE, new AssociationMapping("role"),roleIdColumn);
			table.addForeignKeyConstraint("FK_PRIVROLE2PRIV", USR_PRIVILEGES.name(), CASCADE, new AssociationMapping("privilege"), componentName,privilegeIdColumn);
		}
	},
	USR_USERINROLE {
		void describeTable(Table table) {
			Column userIdColumn = table.addColumn("USERID", "number" , true, NUMBER2LONG , "userId");
			Column roleIdColumn = table.addColumn("ROLEID", "number" , true, NUMBER2LONG , "id");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addPrimaryKeyConstraint("USR_PK_USERINROLE", roleIdColumn , userIdColumn);		
			table.addForeignKeyConstraint("FK_USERROLE2ROLE", USR_ROLES.name(), CASCADE, new AssociationMapping("role") , roleIdColumn);
			table.addForeignKeyConstraint("FK_USERROLE2USER", USR_PRIVILEGES.name(), CASCADE, new AssociationMapping("privilege") , userIdColumn);
		}
	};
	
	void addTo(DataModel component) {
		Table table = component.addTable(name());
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}