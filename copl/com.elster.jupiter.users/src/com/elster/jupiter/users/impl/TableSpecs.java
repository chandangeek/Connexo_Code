package com.elster.jupiter.users.impl;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.*;

import com.elster.jupiter.orm.*;

public enum TableSpecs {
	USR_PRIVILEGE {
		void describeTable(Table table) {
			Column nameColumn = table.addColumn("NAME", "varchar2(80)" , true , NOCONVERSION , "name");
			Column componentName = table.addColumn("COMPONENT","varchar2(3)",true,NOCONVERSION,"componentName");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addPrimaryKeyConstraint("USR_PK_PRIVILEGES", componentName, nameColumn);			
		}
	},
	USR_GROUP {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			Column nameColumn = table.addColumn("NAME", "varchar2(80)" , true , NOCONVERSION , "name");
			table.addVersionCountColumn("VERSIONCOUNT", "number", "versionCount");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addModTimeColumn("MODTIME", "modTime");
			table.addPrimaryKeyConstraint("USR_PK_GROUP", idColumn);
			table.addUniqueConstraint("IDS_U_GROUP", nameColumn);
		}
	},
	USR_USER {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			Column authenticationNameColumn = table.addColumn("AUTHNAME", "varchar2(80)" , true , NOCONVERSION , "authenticationName");
			table.addColumn("FIRSTNAME", "varchar2(80)" , true , NOCONVERSION , "firstName");
			table.addColumn("LASTNAME", "varchar2(80)" , true , NOCONVERSION , "lastName");
			table.addVersionCountColumn("VERSIONCOUNT", "number", "versionCount");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addModTimeColumn("MODTIME", "modTime");
			table.addPrimaryKeyConstraint("USR_PK_USER", idColumn);
			table.addUniqueConstraint("USR_U_USERAUTHNAME" , authenticationNameColumn);			
		}
	},
	USR_PRIVILEGEINGROUP {
		void describeTable(Table table) {
			Column groupIdColumn = table.addColumn("GROUPID", "number" , true, NUMBER2LONG , "groupId");
			Column privilegeNameColumn = table.addColumn("PRIVILEGENAME", "number" , true, NOCONVERSION , "privilegeName");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addPrimaryKeyConstraint("USR_PK_PRIVILEGEINGROUP", groupIdColumn , privilegeNameColumn);		
			table.addForeignKeyConstraint("FK_PRIVINGROUP2GROUP", USR_GROUP.name(), CASCADE, new AssociationMapping("group"),groupIdColumn);
			table.addForeignKeyConstraint("FK_PRIVINGROUP2PRIV", USR_PRIVILEGE.name(), CASCADE, new AssociationMapping("privilege"), privilegeNameColumn);
		}
	},
	USR_USERINGROUP {
		void describeTable(Table table) {
			Column userIdColumn = table.addColumn("USERID", "number" , true, NUMBER2LONG , "userId");
			Column groupIdColumn = table.addColumn("GROUPID", "number" , true, NUMBER2LONG , "groupId");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addPrimaryKeyConstraint("USR_PK_USERINGROUP", groupIdColumn , userIdColumn);		
			table.addForeignKeyConstraint("FK_USERINGROUP2GROUP", USR_GROUP.name(), CASCADE, new AssociationMapping("group") , groupIdColumn);
			table.addForeignKeyConstraint("FK_USERINGROUP2USER", USR_USER.name(), CASCADE, new AssociationMapping("user") , userIdColumn);
		}
	};
	
	void addTo(DataModel component) {
		Table table = component.addTable(name());
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}