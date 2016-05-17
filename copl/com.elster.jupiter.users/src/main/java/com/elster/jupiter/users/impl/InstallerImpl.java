package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.FormatKey;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.security.Privileges;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.util.Checks.is;

public class InstallerImpl {
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private final Logger logger = Logger.getLogger(InstallerImpl.class.getName());

    private DataModel dataModel;
    private String defaultDomain;
    private UserService userService;

    public InstallerImpl(DataModel dataModel, UserService userService) {
        this.dataModel = dataModel;
        this.userService = userService;
    }

    public void install(String defaultDomain) {
        try {
            dataModel.install(true, true);
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }

        this.defaultDomain = defaultDomain;
    }

    public void addDefaults(String adminPassword) {
        createMasterData(adminPassword);
        createUserPreferences(userService.getUserPreferencesService());
    }

    private void createMasterData(String adminPassword) {
        try {
            InternalDirectoryImpl directory = (InternalDirectoryImpl) userService.findUserDirectory(this.defaultDomain)
                    .orElseGet(this::createDirectory);
            GroupImpl administrators = (GroupImpl) userService.findGroup(UserService.DEFAULT_ADMIN_ROLE)
                    .orElseGet(() -> userService.createGroup(UserService.DEFAULT_ADMIN_ROLE, UserService.DEFAULT_ADMIN_ROLE_DESCRIPTION));

            grantSystemAdministratorPrivileges(administrators);

            if (!userService.findUser("admin").isPresent()) {
                createAdministratorUser(directory, new GroupImpl[]{administrators}, adminPassword);
            }
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private InternalDirectoryImpl createDirectory() {
        InternalDirectoryImpl directory = InternalDirectoryImpl.from(dataModel, defaultDomain);
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
        tryCatchBlock(() -> {
            userPrefsService.createUserPreference(Locale.ENGLISH, FormatKey.SHORT_DATE, "dd MMM ''yy", "d M \'y", true);
            userPrefsService.createUserPreference(Locale.ENGLISH, FormatKey.LONG_DATE, "EEE dd MMM ''yy", "D d M \'y", true);
            userPrefsService.createUserPreference(Locale.ENGLISH, FormatKey.SHORT_TIME, "HH:mm", "H:i", true);
            userPrefsService.createUserPreference(Locale.ENGLISH, FormatKey.LONG_TIME, "HH:mm:ss", "H:i:s", true);
            userPrefsService.createUserPreference(Locale.ENGLISH, FormatKey.SHORT_DATETIME, "HH:mm dd MMM ''yy", "d M \'y - H:i", true);
            userPrefsService.createUserPreference(Locale.ENGLISH, FormatKey.LONG_DATETIME, "HH:mm:ss EEE dd MMM ''yy", "D d M \'y - H:i:s", true);
            userPrefsService.createUserPreference(Locale.ENGLISH, FormatKey.DECIMAL_PRECISION, "2", "2", true);
            userPrefsService.createUserPreference(Locale.ENGLISH, FormatKey.DECIMAL_SEPARATOR, ".", ".", true);
            userPrefsService.createUserPreference(Locale.ENGLISH, FormatKey.THOUSANDS_SEPARATOR, ",", ",", true);
            userPrefsService.createUserPreference(Locale.ENGLISH, FormatKey.CURRENCY, "\u00A3 {0}", "\u00A3 {0}", true);
        });
        //en (US)
        tryCatchBlock(() -> {
            userPrefsService.createUserPreference(Locale.US, FormatKey.SHORT_DATE, "MMM-dd-''yy", "M-d-\'y", true);
            userPrefsService.createUserPreference(Locale.US, FormatKey.LONG_DATE, "EEE, MMM-dd-''yy", "D, M-d-\'y", true);
            userPrefsService.createUserPreference(Locale.US, FormatKey.SHORT_TIME, "hh:mm a", "h:i a", true);
            userPrefsService.createUserPreference(Locale.US, FormatKey.LONG_TIME, "hh:mm:ss a", "h:i:s a", true);
            userPrefsService.createUserPreference(Locale.US, FormatKey.SHORT_DATETIME, "hh:mm a MMM-dd-''yy", "M-d-'y - h:i a", true);
            userPrefsService.createUserPreference(Locale.US, FormatKey.LONG_DATETIME, "hh:mm:ss a EEE, MMM-dd-''yy", "D, M-d-'y - h:i:s a", true);
            userPrefsService.createUserPreference(Locale.US, FormatKey.DECIMAL_PRECISION, "2", "2", true);
            userPrefsService.createUserPreference(Locale.US, FormatKey.DECIMAL_SEPARATOR, ".", ".", true);
            userPrefsService.createUserPreference(Locale.US, FormatKey.THOUSANDS_SEPARATOR, ",", ",", true);
            userPrefsService.createUserPreference(Locale.US, FormatKey.CURRENCY, "{0} $", "{0} $", true);
        });
    }

    private void tryCatchBlock(Runnable block) {
        try {
            block.run();
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
