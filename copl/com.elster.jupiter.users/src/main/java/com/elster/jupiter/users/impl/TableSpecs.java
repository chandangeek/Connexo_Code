package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.users.*;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;
import static com.elster.jupiter.orm.Table.*;

public enum TableSpecs {
    USR_RESOURCE {
        void addTo(DataModel dataModel) {
            Table<Resource> table = dataModel.addTable(name(), Resource.class);
            table.map(ResourceImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("COMPONENT").type("varchar2(3)").notNull().map("componentName").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.addCreateTimeColumn("CREATETIME", "createTime");
            table.primaryKey("USR_PK_RESOURCE").on(idColumn).add();
            table.unique("IDS_U_RESOURCE").on(nameColumn).add();
        }
    },
    USR_PRIVILEGE {
		void addTo(DataModel dataModel) {
			Table<Privilege> table = dataModel.addTable(name(), Privilege.class);
			table.map(PrivilegeImpl.class);
            Column idColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            Column resourceColumn = table.column("RESOURCEID").type("number").notNull().add();
			table.primaryKey("USR_PK_PRIVILEGES").on(idColumn).add();
            table.foreignKey("USR_FK_PRIVILEGES_RESOURCE").references(USR_RESOURCE.name()).onDelete(CASCADE).map("resource").on(resourceColumn).add();
		}
	},
	USR_GROUP {
		void addTo(DataModel dataModel) {
			Table<Group> table = dataModel.addTable(name(), Group.class);
			table.map(GroupImpl.class);
			Column idColumn = table.addAutoIdColumn();
			Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
			table.addVersionCountColumn("VERSIONCOUNT", "number", "version");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addModTimeColumn("MODTIME", "modTime");
			table.primaryKey("USR_PK_GROUP").on(idColumn).add();
			table.unique("IDS_U_GROUP").on(nameColumn).add();
		}
	},
    USR_USERDIRECTORY {
        @Override
        void addTo(DataModel dataModel) {
            Table<UserDirectory> table = dataModel.addTable(name(), UserDirectory.class);
			table.map(AbstractUserDirectoryImpl.IMPLEMENTERS);
            Column domain = table.column("DOMAIN").type("varchar(128)").notNull().map("domain").add();
            table.addDiscriminatorColumn("DIRECTORY_TYPE", "char(3)");
            table.column("IS_DEFAULT").bool().map("isDefault").add();
            table.column("GROUPS_INTERNAL").type("char(1)").conversion(CHAR2BOOLEAN).map("manageGroupsInternal").add();
            table.column("DIRECTORY_USER").type("varchar(4000)").map("directoryUser").add();
            table.column("PASSWORD").type("varchar(128)").map("password").add();
            table.column("URL").type("varchar(4000)").map("url").add();
            table.column("BASE_USER").type("varchar(4000)").map("baseUser").add();
            table.column("BASE_GROUP").type("varchar(4000)").map("baseGroup").add();
            table.addAuditColumns();
            table.primaryKey("USR_PK_USERDIRECTORY").on(domain).add();
        }
    },
	USR_USER {
		void addTo(DataModel dataModel) {
			Table<User> table = dataModel.addTable(name(), User.class);
			table.map(UserImpl.class);
			Column idColumn = table.addAutoIdColumn();
			Column authenticationNameColumn = table.column("AUTHNAME").varChar(NAME_LENGTH).notNull().map("authenticationName").add();
			table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
			table.column("HA1").type("varchar2(32)").map("ha1").add();
            table.column("LANGUAGETAG").type("varchar2(64)").map("languageTag").add();
            Column userDirColumn = table.column("USER_DIRECTORY").varChar(128).notNull().add();
            table.addVersionCountColumn("VERSIONCOUNT", "number", "version");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addModTimeColumn("MODTIME", "modTime");
			table.primaryKey("USR_PK_USER").on(idColumn).add();
			table.unique("USR_U_USERAUTHNAME").on(userDirColumn, authenticationNameColumn).add();
            table.foreignKey("USR_FK_USER_USERDIR").references(USR_USERDIRECTORY.name()).onDelete(CASCADE).map("userDirectory").on(userDirColumn).add();
		}
	},
	USR_PRIVILEGEINGROUP {
		void addTo(DataModel dataModel) {
			Table<PrivilegeInGroup> table = dataModel.addTable(name(), PrivilegeInGroup.class);
			table.map(PrivilegeInGroup.class);
			Column groupIdColumn = table.column("GROUPID").number().notNull().conversion(NUMBER2LONG).map("groupId").add();
			Column applicationColumn = table.column("APPLICATION").type("varchar2(10)").notNull().map("applicationName").add();

			Column privilegeIdColumn = table.column("PRIVILEGENAME").varChar(NAME_LENGTH).notNull().map("privilegeName").add();
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.primaryKey("USR_PK_PRIVILEGEINGROUP").on(groupIdColumn ,applicationColumn, privilegeIdColumn).add();
			table.foreignKey("FK_PRIVINGROUP2GROUP").references(USR_GROUP.name()).onDelete(CASCADE).map("group").reverseMap("privilegeInGroups").on(groupIdColumn).add();
			table.foreignKey("FK_PRIVINGROUP2PRIV").references(USR_PRIVILEGE.name()).onDelete(CASCADE).map("privilege").on(privilegeIdColumn).add();
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
	},
	USR_PREFERENCES {
	    @Override
	    void addTo(DataModel dataModel) {
	        Table<UserPreference> table = dataModel.addTable(name(), UserPreference.class);
	        table.map(UserPreferenceImpl.class);
	        Column locale = table.column("LOCALE").varChar().notNull().map("locale").add();
	        Column key = table.column("FORMATKEY").number().notNull().conversion(ColumnConversion.NUMBER2ENUM).map("key").add();
	        Column formatBE = table.column("FORMAT_BE").notNull().varChar().map("formatBE").add();
	        Column formatFE = table.column("FORMAT_FE").notNull().varChar().map("formatFE").add();
	        table.column("ISDEFAULT").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("isDefault").add();
	        table.primaryKey("USR_PK_PREFERENCES").on(locale, key, formatBE, formatFE).add();
	    }
	}
	;

	abstract void addTo(DataModel dataModel);
	
}