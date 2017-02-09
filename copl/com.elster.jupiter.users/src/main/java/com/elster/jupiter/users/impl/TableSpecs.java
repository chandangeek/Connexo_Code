package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserPreference;
import com.elster.jupiter.users.WorkGroup;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {
    USR_RESOURCE {
        void addTo(DataModel dataModel) {
            Table<Resource> table = dataModel.addTable(name(), Resource.class);
            table.map(ResourceImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column nameColumn = table.column("NAME").varChar().notNull().map("name").add();
            table.column("COMPONENT").varChar().notNull().map("componentName").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            table.addCreateTimeColumn("CREATETIME", "createTime");
            table.primaryKey("USR_PK_RESOURCE").on(idColumn).add();
            table.unique("IDS_U_RESOURCE").on(nameColumn).add();
        }
    },
    USR_PRIVILEGE {
        void addTo(DataModel dataModel) {
            Table<Privilege> table = dataModel.addTable(name(), Privilege.class);
            table.map(PrivilegeImpl.class);
            table.cache();
            Column idColumn = table.column("NAME").varChar().notNull().map("name").add();
            Column resourceColumn = table.column("RESOURCEID").type("number").notNull().add();
            table.primaryKey("USR_PK_PRIVILEGES").on(idColumn).add();
            table
                .foreignKey("USR_FK_PRIVILEGES_RESOURCE")
                .references(USR_RESOURCE.name())
                .onDelete(CASCADE)
                .map("resource")
                .on(resourceColumn)
                .add();
        }
    },
    USR_GROUP {
        void addTo(DataModel dataModel) {
            Table<Group> table = dataModel.addTable(name(), Group.class);
            table.map(GroupImpl.class);
            table.setJournalTableName("USR_GROUPJRNL").since(version(10, 2));
            Column idColumn = table.addAutoIdColumn();
            Column nameColumn = table.column("NAME").varChar().notNull().map("name").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            table.addAuditColumns().get(3).since(version(10, 2));
            table.primaryKey("USR_PK_GROUP").on(idColumn).add();
            table.unique("IDS_U_GROUP").on(nameColumn).add();
        }
    },
    USR_WORKGROUP {
        void addTo(DataModel dataModel) {
            Table<WorkGroup> table = dataModel.addTable(name(), WorkGroup.class);
            table.map(WorkGroupImpl.class);
            table.since(version(10, 3));
            Column idColumn = table.addAutoIdColumn();
            Column nameColumn = table.column("NAME").varChar().notNull().map("name").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            table.addAuditColumns();
            table.primaryKey("USR_PK_WORKGROUP").on(idColumn).add();
            table.unique("IDS_U_WORKGROUP").on(nameColumn).add();
        }
    },
    USR_USERDIRECTORY {
        @Override
        void addTo(DataModel dataModel) {
            Table<UserDirectory> table = dataModel.addTable(name(), UserDirectory.class);
            table.map(AbstractUserDirectoryImpl.IMPLEMENTERS);
            table.setJournalTableName("USR_USERDIRECTORYJRNL").since(version(10, 2));
            Column idColumn = table.addAutoIdColumn();
            Column domain = table.column("DOMAIN").varChar().notNull().map("name").add();
            table.addDiscriminatorColumn("DIRECTORY_TYPE", "char(3)");
            table.column("IS_DEFAULT").bool().map("isDefault").add();
            table.column("GROUPS_INTERNAL").type("char(1)").conversion(CHAR2BOOLEAN).map("manageGroupsInternal").add();
            table.column("DIRECTORY_USER").varChar().map("directoryUser").add();
            table.column("PASSWORD").varChar().map("password").add();
            table.column("URL").varChar().map("url").add();
            table.column("BACKUPURL").varChar().map("backupUrl").add();
            table.column("SECURITY").varChar().map("securityProtocol").add();
            table.column("BASE_USER").varChar().map("baseUser").add();
            table.column("BASE_GROUP").varChar().map("baseGroup").add();
            table.addAuditColumns();
            table.primaryKey("USR_PK_USERDIRECTORY").on(idColumn).add();
            table.unique("IDS_U_UDNAME").on(domain).add();
        }
    },
    USR_USER {
        void addTo(DataModel dataModel) {
            Table<User> table = dataModel.addTable(name(), User.class);
            table.map(UserImpl.class);
            table.setJournalTableName("USR_USERJRNL").since(version(10, 2));
            Column idColumn = table.addAutoIdColumn();
            Column authenticationNameColumn = table.column("AUTHNAME").varChar(NAME_LENGTH).notNull().map("authenticationName").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            table.column("HA1").varChar().map("ha1").add();
            table.column("SALT").number().conversion(NUMBER2INT).map("salt").add();
            table.column("LANGUAGETAG").varChar().map("languageTag").add();
            Column userDirColumn = table.column("USER_DIRECTORY").number().notNull().add();
            table.column("Active").type("char(1)").conversion(CHAR2BOOLEAN).map("status").add();
            table.column("LASTSUCCESSFULOGIN").number().conversion(NUMBER2INSTANT).map("lastSuccessfulLogin").notAudited().add();
            table.column("LASTUNSUCCESSFULOGIN").number().conversion(NUMBER2INSTANT).map("lastUnSuccessfulLogin").notAudited().add();
            table.addAuditColumns().get(3).since(version(10, 2));
            table.primaryKey("USR_PK_USER").on(idColumn).add();
            table.unique("USR_U_USERAUTHNAME").on(userDirColumn, authenticationNameColumn).add();
            table
                .foreignKey("USR_FK_USER_USERDIR")
                .references(USR_USERDIRECTORY.name())
                .map("userDirectory")
                .on(userDirColumn).add();
        }
    },
    USR_PRIVILEGEINGROUP {
        void addTo(DataModel dataModel) {
            Table<PrivilegeInGroup> table = dataModel.addTable(name(), PrivilegeInGroup.class);
            table.map(PrivilegeInGroup.class);
            table.setJournalTableName("USR_PRIVILEGEINGROUPJRNL").since(version(10, 2));
            Column groupIdColumn = table.column("GROUPID").number().notNull().conversion(NUMBER2LONG).map("groupId").add();
            Column applicationColumn = table.column("APPLICATION").varChar().notNull().map("applicationName").add();
            Column privilegeIdColumn = table.column("PRIVILEGENAME").varChar().notNull().map("privilegeName").add();
            table.addAuditColumns().stream().filter(column -> !"CREATETIME".equals(column.getName())).forEach(column -> column.since(version(10, 2)));
            table
                .primaryKey("USR_PK_PRIVILEGEINGROUP")
                .on(groupIdColumn, applicationColumn, privilegeIdColumn)
                .add();
            table
                .foreignKey("FK_PRIVINGROUP2GROUP")
                .references(USR_GROUP.name())
                .map("group")
                .reverseMap("privilegeInGroups")
                .on(groupIdColumn)
                .add();
            table
                .foreignKey("FK_PRIVINGROUP2PRIV")
                .references(USR_PRIVILEGE.name())
                .map("privilege")
                .on(privilegeIdColumn)
                .add();
        }
    },
    USR_USERINWORKGROUP {
        void addTo(DataModel dataModel) {
            Table<UsersInWorkGroup> table = dataModel.addTable(name(), UsersInWorkGroup.class);
            table.map(UsersInWorkGroup.class);
            table.since(version(10,3));
            Column workGroupIdColumn = table.column("WORKGROUPID").number().notNull().conversion(NUMBER2LONG).map("workGroupId").add();
            Column userIdColumn = table.column("USERID").number().notNull().conversion(NUMBER2LONG).map("userId").add();
            table.addAuditColumns();
            table.primaryKey("USR_PK_USERINWORKGROUP")
                    .on(workGroupIdColumn, userIdColumn)
                    .add();
            table.foreignKey("FK_USER2WORKGROUP")
                    .references(USR_WORKGROUP.name())
                    .map("workGroup")
                    .reverseMap("usersInWorkGroups")
                    .on(workGroupIdColumn)
                    .add();
            table.foreignKey("FK_USERINGROUP2WORKGROUP")
                    .references(USR_USER.name())
                    .map("user")
                    .on(userIdColumn)
                    .add();
        }
    },
    USR_USERINGROUP {
        void addTo(DataModel dataModel) {
            Table<UserInGroup> table = dataModel.addTable(name(), UserInGroup.class);
            table.map(UserInGroup.class);
            table.setJournalTableName("USR_USERINGROUPJRNL").since(version(10, 2));
            Column userIdColumn = table.column("USERID").number().notNull().conversion(NUMBER2LONG).map("userId").add();
            Column groupIdColumn = table.column("GROUPID").number().notNull().conversion(NUMBER2LONG).map("groupId").add();
            table.addAuditColumns().stream().filter(column -> !"CREATETIME".equals(column.getName())).forEach(column -> column.since(version(10, 2)));
            table.primaryKey("USR_PK_USERINGROUP").on(userIdColumn, groupIdColumn).add();
            table
                .foreignKey("FK_USERINGROUP2GROUP")
                .references(USR_GROUP.name())
                .map("group")
                .on(groupIdColumn)
                .add();
            table
                .foreignKey("FK_USERINGROUP2USER")
                .references(USR_USER.name())
                .map("user")
                .reverseMap("memberships")
                .on(userIdColumn).add();
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
    };

    abstract void addTo(DataModel dataModel);

}