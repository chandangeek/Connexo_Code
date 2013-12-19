package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;

public enum TableSpecs {
	USR_PRIVILEGE {
		void describeTable(Table table) {
			Column nameColumn = table.column("NAME").type("varchar2(80)").notNull().map("name").add();
			table.column("COMPONENT").type("varchar2(3)").notNull().map("componentName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.primaryKey("USR_PK_PRIVILEGES").on(nameColumn).add();
		}
	},
	USR_GROUP {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			Column nameColumn = table.column("NAME").type("varchar2(80)").notNull().map("name").add();
			table.addVersionCountColumn("VERSIONCOUNT", "number", "version");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addModTimeColumn("MODTIME", "modTime");
			table.primaryKey("USR_PK_GROUP").on(idColumn).add();
			table.unique("IDS_U_GROUP").on(nameColumn).add();
		}
	},
	USR_USER {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			Column authenticationNameColumn = table.column("AUTHNAME").type("varchar2(80)").notNull().map("authenticationName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.column("HA1").type("varchar2(32)").map("ha1").add();
			table.addVersionCountColumn("VERSIONCOUNT", "number", "version");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addModTimeColumn("MODTIME", "modTime");
			table.primaryKey("USR_PK_USER").on(idColumn).add();
			table.unique("USR_U_USERAUTHNAME").on(authenticationNameColumn).add();
		}
	},
	USR_PRIVILEGEINGROUP {
		void describeTable(Table table) {
			Column groupIdColumn = table.addColumn("GROUPID", "number" , true, NUMBER2LONG , "groupId");
			Column privilegeNameColumn = table.column("PRIVILEGENAME").type("varchar2(80)").notNull().map("privilegeName").add();
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.primaryKey("USR_PK_PRIVILEGEINGROUP").on(groupIdColumn , privilegeNameColumn).add();
			table.foreignKey("FK_PRIVINGROUP2GROUP").references(USR_GROUP.name()).onDelete(CASCADE).map("group").reverseMap("privilegeInGroups").on(groupIdColumn).add();
			table.foreignKey("FK_PRIVINGROUP2PRIV").references(USR_PRIVILEGE.name()).onDelete(CASCADE).map("privilege").on(privilegeNameColumn).add();
		}
	},
	USR_USERINGROUP {
		void describeTable(Table table) {
			Column userIdColumn = table.addColumn("USERID", "number" , true, NUMBER2LONG , "userId");
			Column groupIdColumn = table.addColumn("GROUPID", "number" , true, NUMBER2LONG, "groupId");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.primaryKey("USR_PK_USERINGROUP").on(groupIdColumn , userIdColumn).add();
			table.foreignKey("FK_USERINGROUP2GROUP").references(USR_GROUP.name()).onDelete(CASCADE).map("group").on(groupIdColumn).add();
			table.foreignKey("FK_USERINGROUP2USER").references(USR_USER.name()).onDelete(CASCADE).map("user").reverseMap("memberships").on(userIdColumn).add();
		}
	};
	
	void addTo(DataModel component) {
		Table table = component.addTable(name());
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}