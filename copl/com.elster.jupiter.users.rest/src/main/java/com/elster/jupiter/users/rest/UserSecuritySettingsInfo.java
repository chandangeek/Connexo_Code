package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.UserSecuritySettings;

public class UserSecuritySettingsInfo {

    public long id;
    public boolean lockAccountOption = false;
    public int failedLoginAttempts = 3;
    public int lockOutMinutes = 120;

    public UserSecuritySettingsInfo() {
    }


    public UserSecuritySettingsInfo(UserSecuritySettings userSecuritySettings){
        id = userSecuritySettings.getId();
        lockAccountOption = userSecuritySettings.isLockAccountActive();
        failedLoginAttempts = userSecuritySettings.getFailedLoginAttempts();
        lockOutMinutes = userSecuritySettings.getLockOutMinutes();
    }


    public boolean update(UserSecuritySettings userSecuritySettings) {
        return updateActivateLocking(userSecuritySettings) | updateFailedLoginAttepmts(userSecuritySettings) | updateLockOutMinutes(userSecuritySettings);
    }

    private boolean updateActivateLocking(UserSecuritySettings userSecuritySettings) {
        if (lockAccountOption != userSecuritySettings.isLockAccountActive()) {
            userSecuritySettings.setLockAccountActive(this.lockAccountOption);
            return true;
        }
        return false;
    }

    private boolean updateFailedLoginAttepmts(UserSecuritySettings userSecuritySettings) {
        if (failedLoginAttempts != userSecuritySettings.getFailedLoginAttempts()) {
            userSecuritySettings.setFailedLoginAttempts(this.failedLoginAttempts);
            return true;
        }
        return false;
    }

    private boolean updateLockOutMinutes(UserSecuritySettings userSecuritySettings) {
        if (lockOutMinutes != userSecuritySettings.getLockOutMinutes()) {
            userSecuritySettings.setLockOutMinutes(this.lockOutMinutes);
            return true;
        }
        return false;
    }
}