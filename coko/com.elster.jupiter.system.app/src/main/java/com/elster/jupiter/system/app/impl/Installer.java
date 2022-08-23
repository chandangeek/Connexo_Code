/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system.app.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.system.app.SysAppService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.SqlExceptionThrowingFunction;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Stream;

final class Installer implements FullInstaller, Upgrader {

    private final UserService userService;

    @Inject
    Installer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        grantPrivileges();
    }

    @Override
    public <T> T executeQuery(Statement statement, String sql, SqlExceptionThrowingFunction<ResultSet, T> resultMapper) {
        return FullInstaller.super.executeQuery(statement, sql, resultMapper);
    }

    @Override
    public <T> T executeQuery(DataModel dataModel, String sql, SqlExceptionThrowingFunction<ResultSet, T> resultMapper) {
        return FullInstaller.super.executeQuery(dataModel, sql, resultMapper);
    }

    @Override
    public void execute(Statement statement, String sql) {
        FullInstaller.super.execute(statement, sql);
    }

    @Override
    public void execute(DataModel dataModel, String... sql) {
        FullInstaller.super.execute(dataModel, sql);
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        grantPrivileges();
    }

    private String[] getAdminPrivileges() {
        return SysAppPrivileges.getApplicationPrivileges().stream()
                .filter(name -> !name.equals(com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL))
                .filter(name -> !name.equals(com.elster.jupiter.users.security.Privileges.Constants.ADMINISTRATE_USER_ROLE))
                .filter(name -> !name.equals(com.elster.jupiter.users.security.Privileges.Constants.VIEW_USER_ROLE))
                .toArray(String[]::new);
    }

    private void grantPrivileges() {
        String[] adminPrivileges = userService.userAdminPrivileges();
        userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, SysAppService.APPLICATION_KEY, adminPrivileges);
        userService.grantGroupWithPrivilege(UserService.BATCH_EXECUTOR_ROLE, SysAppService.APPLICATION_KEY, batchExecutorPrivileges());
        userService.grantGroupWithPrivilege(UserService.DEFAULT_INSTALLER_ROLE, SysAppService.APPLICATION_KEY, installerPrivileges());
        userService.grantGroupWithPrivilege(UserService.SYSTEM_ADMIN_ROLE, SysAppService.APPLICATION_KEY, getAdminPrivileges());
    }

    private String[] batchExecutorPrivileges() {
        return Stream.concat(Arrays.stream(userService.userAdminPrivileges()),
                        Stream.of(com.elster.jupiter.systemproperties.security.Privileges.Constants.VIEW_SYS_PROPS,
                                com.elster.jupiter.systemproperties.security.Privileges.Constants.ADMINISTER_SYS_PROPS,
                                com.elster.jupiter.soap.whiteboard.cxf.security.Privileges.Constants.INVOKE_WEB_SERVICES))
                .toArray(String[]::new);
    }

    private String[] installerPrivileges() {
        return new String[]{
                //license
                com.elster.jupiter.license.security.Privileges.Constants.VIEW_LICENSE,
                com.elster.jupiter.license.security.Privileges.Constants.UPLOAD_LICENSE,
                //users
                com.elster.jupiter.users.security.Privileges.Constants.ADMINISTRATE_USER_ROLE,
                com.elster.jupiter.users.security.Privileges.Constants.VIEW_USER_ROLE,
                //certificates
                com.elster.jupiter.pki.security.Privileges.Constants.VIEW_CERTIFICATES,
        };
    }
}
