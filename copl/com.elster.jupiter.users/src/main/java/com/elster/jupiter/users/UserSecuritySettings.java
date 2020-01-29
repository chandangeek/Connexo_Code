package com.elster.jupiter.users;

import com.elster.jupiter.util.HasName;

import java.security.Principal;

public interface UserSecuritySettings extends Principal, HasName {

    long getId();

    boolean isLockAccountActive();

    int getFailedLoginAttempts();

    int getLockOutMinutes();

    void setLockAccountActive(boolean lockAccountActive);

    void setFailedLoginAttempts(int failedLoginAttempts);

    void setLockOutMinutes(int lockOutMinutes);

    void update();

    void save();
}