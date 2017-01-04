package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.PreferenceType;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.security.Privileges;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.util.Checks.is;

public class InstallerImpl implements FullInstaller {
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final UserServiceImpl userService;
    private final BundleContext bundleContext;

    @Inject
    public InstallerImpl(DataModel dataModel, UserService userService, BundleContext bundleContext) {
        this.dataModel = dataModel;
        this.userService = (UserServiceImpl) userService;
        this.bundleContext = bundleContext;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create default privilege category",
                userService::createDefaultPrivilegeCategory,
                logger
        );
        doTry(
                "Create batch executors group",
                () -> createBatchExecutorRole(logger),
                logger
        );
        doTry(
                "Install User privileges.",
                this::installPrivileges,
                logger
        );
        addDefaults(bundleContext != null ? bundleContext.getProperty("admin.password") : null, logger);
    }

    public void addDefaults(String adminPassword, Logger logger) {
        createMasterData(adminPassword, logger);
        doTry(
                "Create user preferences.",
                () -> createUserPreferences(userService.getUserPreferencesService()),
                logger
        );
    }

    private void installPrivileges() {
        Resource resource = userService.createResource(UserService.COMPONENTNAME, Privileges.RESOURCE_USERS.getKey(), Privileges.RESOURCE_USERS_DESCRIPTION.getKey());
        PrivilegeCategory defaultPrivilegeCategory = userService.getDefaultPrivilegeCategory();
        resource.createGrantPrivilege(Privileges.Constants.ADMINISTRATE_USER_ROLE)
                .in(defaultPrivilegeCategory)
                .forCategory(defaultPrivilegeCategory)
                .create();
        resource.createPrivilege(Privileges.Constants.VIEW_USER_ROLE);
    }

    private void createBatchExecutorRole(Logger logger) {
        try {
            Group group = userService.createGroup(UserService.BATCH_EXECUTOR_ROLE, UserService.BATCH_EXECUTOR_ROLE_DESCRIPTION);
            group.update();
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed to create a batch executor user.", e);
            throw e;
        }
    }


    private void createMasterData(String adminPassword, Logger logger) {
        InternalDirectoryImpl directory = (InternalDirectoryImpl) userService.findUserDirectory(userService.getRealm())
                .orElseGet(this::createDirectory);
        GroupImpl administrators = (GroupImpl) userService.findGroup(UserService.DEFAULT_ADMIN_ROLE)
                .orElseGet(() -> createAdministratorGroup(logger));

        doTry(
                "Grant system administrator privileges to " + UserService.DEFAULT_ADMIN_ROLE,
                () -> grantSystemAdministratorPrivileges(administrators),
                logger
        );

        GroupImpl installers = (GroupImpl) userService.findGroup(UserService.DEFAULT_INSTALLER_ROLE)
                .orElseGet(() -> createInstallerGroup(logger));

        doTry(
                "Grant system administrator privileges to " + UserService.DEFAULT_INSTALLER_ROLE,
                () -> grantSystemAdministratorPrivileges(installers),
                logger
        );

        GroupImpl systemAdmins = (GroupImpl) userService.findGroup(UserService.SYSTEM_ADMIN_ROLE)
                .orElseGet(() -> createSystemAdminGroup(logger));

        if (!userService.findUser("admin").isPresent()) {
            doTry(
                    "Create administrator user.",
                    () -> createAdministratorUser(directory, new GroupImpl[]{installers}, adminPassword),
                    logger
            );
        }
    }

    private Group createAdministratorGroup(Logger logger) {

        return doTry(
                "Create " + UserService.DEFAULT_ADMIN_ROLE + " user group.",
                () -> userService.createGroup(UserService.DEFAULT_ADMIN_ROLE, UserService.DEFAULT_ADMIN_ROLE_DESCRIPTION),
                logger
        );
    }

    private Group createInstallerGroup(Logger logger) {

        return doTry(
                "Create " + UserService.DEFAULT_INSTALLER_ROLE + " user group.",
                () -> userService.createGroup(UserService.DEFAULT_INSTALLER_ROLE, UserService.DEFAULT_INSTALLER_ROLE_DESCRIPTION),
                logger
        );
    }

    private Group createSystemAdminGroup(Logger logger) {
        return doTry(
                "Create " + UserService.SYSTEM_ADMIN_ROLE + " user group.",
                () -> userService.createGroup(UserService.SYSTEM_ADMIN_ROLE, UserService.SYSTEM_ADMIN_ROLE_DESCRIPTION),
                logger
        );
    }

    private InternalDirectoryImpl createDirectory() {
        InternalDirectoryImpl directory = InternalDirectoryImpl.from(dataModel, userService.getRealm());
        directory.setDefault(true);
        directory.update();
        return directory;
    }

    private void createAdministratorUser(InternalDirectoryImpl directory, GroupImpl[] roles, String adminPassword) {
        UserImpl user = directory.newUser("admin", "System administrator", true, true);
        user.setLocale(Locale.ENGLISH);
        user.setPassword(!is(adminPassword).emptyOrOnlyWhiteSpace() ? adminPassword : UUID.randomUUID()
                .toString()
                .replaceAll("-", ""));
        user.update();
        for (GroupImpl role : roles) {
            user.join(role);
        }
    }

    private void grantSystemAdministratorPrivileges(GroupImpl group) {
        Field[] fields = Privileges.Constants.class.getFields();
        for (Field each : fields) {
            try {
                group.grant("SYS", (String) each.get(null));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void createUserPreferences(UserPreferencesService userPrefsService) {
        //en (UK)
        userPrefsService.createUserPreference(Locale.ENGLISH, PreferenceType.SHORT_DATE, "dd MMM ''yy", "d M \'y", true);
        userPrefsService.createUserPreference(Locale.ENGLISH, PreferenceType.LONG_DATE, "EEE dd MMM ''yy", "D d M \'y", true);
        userPrefsService.createUserPreference(Locale.ENGLISH, PreferenceType.SHORT_TIME, "HH:mm", "H:i", true);
        userPrefsService.createUserPreference(Locale.ENGLISH, PreferenceType.LONG_TIME, "HH:mm:ss", "H:i:s", true);
        userPrefsService.createUserPreference(Locale.ENGLISH, PreferenceType.DATETIME_SEPARATOR, "SPACE", "-", true);
        userPrefsService.createUserPreference(Locale.ENGLISH, PreferenceType.DATETIME_ORDER, "TD", "DT", true);
        userPrefsService.createUserPreference(Locale.ENGLISH, PreferenceType.DECIMAL_PRECISION, "2", "2", true);
        userPrefsService.createUserPreference(Locale.ENGLISH, PreferenceType.DECIMAL_SEPARATOR, ".", ".", true);
        userPrefsService.createUserPreference(Locale.ENGLISH, PreferenceType.THOUSANDS_SEPARATOR, ",", ",", true);
        userPrefsService.createUserPreference(Locale.ENGLISH, PreferenceType.CURRENCY, "\u00A3 {0}", "\u00A3 {0}", true);
        //en (US)
        userPrefsService.createUserPreference(Locale.US, PreferenceType.SHORT_DATE, "MMM-dd-''yy", "M-d-\'y", true);
        userPrefsService.createUserPreference(Locale.US, PreferenceType.LONG_DATE, "EEE, MMM-dd-''yy", "D, M-d-\'y", true);
        userPrefsService.createUserPreference(Locale.US, PreferenceType.SHORT_TIME, "hh:mm a", "h:i a", true);
        userPrefsService.createUserPreference(Locale.US, PreferenceType.LONG_TIME, "hh:mm:ss a", "h:i:s a", true);
        userPrefsService.createUserPreference(Locale.US, PreferenceType.DATETIME_SEPARATOR, "SPACE", "-", true);
        userPrefsService.createUserPreference(Locale.US, PreferenceType.DATETIME_ORDER, "TD", "DT", true);
        userPrefsService.createUserPreference(Locale.US, PreferenceType.DECIMAL_PRECISION, "2", "2", true);
        userPrefsService.createUserPreference(Locale.US, PreferenceType.DECIMAL_SEPARATOR, ".", ".", true);
        userPrefsService.createUserPreference(Locale.US, PreferenceType.THOUSANDS_SEPARATOR, ",", ",", true);
        userPrefsService.createUserPreference(Locale.US, PreferenceType.CURRENCY, "{0} $", "{0} $", true);
    }

}
