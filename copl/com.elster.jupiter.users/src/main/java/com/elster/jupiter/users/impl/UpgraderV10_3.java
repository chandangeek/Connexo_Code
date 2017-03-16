/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.util.streams.Currying.perform;

class UpgraderV10_3 implements Upgrader {

    private final DataModel dataModel;
    private final UserService userService;

    @Inject
    public UpgraderV10_3(DataModel dataModel, UserService userService) {
        this.dataModel = dataModel;
        this.userService = userService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {

        dataModel.useConnectionRequiringTransaction(this::executePreAutoUpdateScript);
        Group installerGroup = createNewGroups();

        dataModelUpgrader.upgrade(dataModel, Version.version(10, 3));

        ((UserServiceImpl) userService).getGrantPrivilege("privilege.administrate.userAndRole")
                .ifPresent(grantPrivilege -> grantPrivilege.addGrantableCategory(userService.getDefaultPrivilegeCategory()));

        userService.findUser("admin").ifPresent(user -> {
            user.getGroups().stream()
                    .forEach(group -> {
                        if (!group.getName().equals("DEFAULT_INSTALLER_ROLE")) {
                            user.leave(group);
                        }
                    });
            user.join(installerGroup);
        });
    }

    private Group createNewGroups() {
        userService.createGroup(UserService.DEFAULT_ADMIN_ROLE, UserService.DEFAULT_ADMIN_ROLE_DESCRIPTION);
        userService.createGroup(UserService.SYSTEM_ADMIN_ROLE, UserService.SYSTEM_ADMIN_ROLE_DESCRIPTION);
        return userService.createGroup(UserService.DEFAULT_INSTALLER_ROLE, UserService.DEFAULT_INSTALLER_ROLE_DESCRIPTION);
    }

    private void executePreAutoUpdateScript(Connection connection) {
        List<String> ddl = Arrays.asList(
                "CREATE TABLE USR_PRIVILEGE_CATEGORY (NAME VARCHAR2(80 CHAR))",
                "CREATE UNIQUE INDEX USR_PK_USR_PRIVILEGE_CATEGORY ON USR_PRIVILEGE_CATEGORY (NAME)",
                "ALTER TABLE USR_PRIVILEGE_CATEGORY ADD CONSTRAINT USR_PK_USR_PRIVILEGE_CATEGORY PRIMARY KEY (NAME)",
                "ALTER TABLE USR_PRIVILEGE_CATEGORY MODIFY (NAME NOT NULL)",
                "INSERT INTO USR_PRIVILEGE_CATEGORY (NAME) VALUES ('Default')",
                "ALTER TABLE USR_PRIVILEGE ADD (CATEGORY VARCHAR2(80 CHAR) DEFAULT 'Default') ADD (DISCRIMINATOR VARCHAR2(1 CHAR))",
                "CREATE INDEX USR_FK_PRIVILEGE_CATEGORY ON USR_PRIVILEGE (CATEGORY)",
                "UPDATE USR_PRIVILEGE SET DISCRIMINATOR = 'P', CATEGORY = 'Default'",
                "UPDATE USR_PRIVILEGE SET DISCRIMINATOR = 'G' WHERE NAME = 'privilege.administrate.userAndRole'",
                "ALTER TABLE USR_PRIVILEGE MODIFY (CATEGORY NOT NULL)",
                "ALTER TABLE USR_PRIVILEGE MODIFY (DISCRIMINATOR NOT NULL)",
                "ALTER TABLE USR_PRIVILEGE ADD CONSTRAINT USR_FK_PRIVILEGE_CATEGORY FOREIGN KEY (CATEGORY) REFERENCES USR_PRIVILEGE_CATEGORY (NAME)"
        );
        try (Statement statement = connection.createStatement()) {
            ddl.forEach(perform(this::executeDdl).on(statement));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void executeDdl(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

}
