package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;

public enum TableSpecs {
	USR_PRIVILEGE {
		void addTo(DataModel dataModel) {
			Table<Privilege> table = dataModel.addTable(name(), Privilege.class);
			table.map(PrivilegeImpl.class);
			Column nameColumn = table.column("NAME").type("varchar2(80)").notNull().map("name").add();
			table.column("COMPONENT").type("varchar2(3)").notNull().map("componentName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.primaryKey("USR_PK_PRIVILEGES").on(nameColumn).add();
		}
	},
	USR_GROUP {
		void addTo(DataModel dataModel) {
			Table<Group> table = dataModel.addTable(name(), Group.class);
			table.map(GroupImpl.class);
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
		void addTo(DataModel dataModel) {
			Table<User> table = dataModel.addTable(name(), User.class);
			table.map(UserImpl.class);
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
		void addTo(DataModel dataModel) {
			Table<PrivilegeInGroup> table = dataModel.addTable(name(), PrivilegeInGroup.class);
			table.map(PrivilegeInGroup.class);
			Column groupIdColumn = table.column("GROUPID").number().notNull().conversion(NUMBER2LONG).map("groupId").add();
			Column privilegeNameColumn = table.column("PRIVILEGENAME").type("varchar2(80)").notNull().map("privilegeName").add();
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.primaryKey("USR_PK_PRIVILEGEINGROUP").on(groupIdColumn , privilegeNameColumn).add();
			table.foreignKey("FK_PRIVINGROUP2GROUP").references(USR_GROUP.name()).onDelete(CASCADE).map("group").reverseMap("privilegeInGroups").on(groupIdColumn).add();
			table.foreignKey("FK_PRIVINGROUP2PRIV").references(USR_PRIVILEGE.name()).onDelete(CASCADE).map("privilege").on(privilegeNameColumn).add();
		}
	},
	USR_USERINGROUP {
		void addTo(DataModel dataModel) {
			Table<UserInGroup> table = dataModel.addTable(name(), UserInGroup.class);
			table.map(UserInGroup.class);
			Column userIdColumn = table.column("USERID").number().notNull().conversion(NUMBER2LONG).map("userId").add();
			Column groupIdColumn = table.column("GROUPID").number().notNull().conversion(NUMBER2LONG).map("groupId").add();
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.primaryKey("USR_PK_USERINGROUP").on(userIdColumn , groupIdColumn).add();
			table.foreignKey("FK_USERINGROUP2GROUP").references(USR_GROUP.name()).onDelete(CASCADE).map("group").on(groupIdColumn).add();
			table.foreignKey("FK_USERINGROUP2USER").references(USR_USER.name()).onDelete(CASCADE).map("user").reverseMap("memberships").on(userIdColumn).add();
		}
	};

	abstract void addTo(DataModel component);	
	
}