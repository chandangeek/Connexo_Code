package com.elster.jupiter.users.impl;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.FormatKey;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.security.Privileges;

public class InstallerImpl {
    private final Logger logger = Logger.getLogger(InstallerImpl.class.getName());

    private DataModel dataModel;
    private String defaultDomain;

    public InstallerImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void install(UserServiceImpl userService, String defaultDomain) {
        try{
		    dataModel.install(true, true);
        }
        catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }

        this.defaultDomain = defaultDomain;
        createPrivileges(userService);
        createMasterData();
        createUserPreferences(userService.getUserPreferencesService());
	}
	
	private void createMasterData() {
        try{
            InternalDirectoryImpl directory = createDirectory();

            GroupImpl administrators = createRole(UserService.DEFAULT_ADMIN_ROLE, UserService.DEFAULT_ADMIN_ROLE_DESCRIPTION);

            grantSystemAdministratorPrivileges(administrators);
            createAdministratorUser(directory, new GroupImpl[]{administrators});
        }
        catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
	}

    private InternalDirectoryImpl createDirectory() {
        InternalDirectoryImpl directory = InternalDirectoryImpl.from(dataModel, defaultDomain);
        directory.setDefault(true);
        directory.save();
        return directory;
    }

    private GroupImpl createRole(String name, String description){
        GroupImpl group = GroupImpl.from(dataModel, name, description);
        group.save();
        return group;
    }
	
	private void createAdministratorUser(InternalDirectoryImpl directory, GroupImpl[] roles) {
        UserImpl user = directory.newUser("admin", "System administrator", true);

		user.setPassword("admin");
		user.setLocale(Locale.ENGLISH);
		user.save();
        for(GroupImpl role : roles){
		    user.join(role);
        }
	}

    private void createPrivileges(UserService userService) {
        userService.createResourceWithPrivileges("SYS", "userAndRole.usersAndRoles", "userAndRole.usersAndRoles.description", new String[] {Privileges.ADMINISTRATE_USER_ROLE, Privileges.VIEW_USER_ROLE});
    }

	private void grantSystemAdministratorPrivileges(GroupImpl group){
        Field[] fields = Privileges.class.getFields();
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
