package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.UserSecuritySettings;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.UserSecuritySettingsInfo;

public class UpdateUserSecuritySettings  implements Transaction<UserSecuritySettings> {

    private final UserSecuritySettingsInfo info;
    private final UserService userService;

    public UpdateUserSecuritySettings(UserSecuritySettingsInfo info, UserService userService) {
        this.info = info;
        this.userService = userService;
    }

    @Override
    public UserSecuritySettings perform() {
        UserSecuritySettings userSecuritySettings = fetchUserSecuritySettings();
        if(userService.getLockingAccountSettings().isPresent()) {
            if(info.update(userSecuritySettings))
                doUpdate(userSecuritySettings);
        }
        else
            userSecuritySettings.save();
        return userSecuritySettings;
    }


    private UserSecuritySettings fetchUserSecuritySettings() {
        return userService.findOrCreateUserSecuritySettings(info.lockAccountOption, info.failedLoginAttempts, info.lockOutMinutes);
    }

    private void doUpdate(UserSecuritySettings userSecuritySettings) {
        userSecuritySettings.update();
    }

}