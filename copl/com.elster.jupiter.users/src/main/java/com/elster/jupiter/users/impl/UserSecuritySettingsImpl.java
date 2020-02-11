package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserSecuritySettings;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;

public final class UserSecuritySettingsImpl implements UserSecuritySettings {

    private long id;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")

    private boolean lockAccountOption;
    private int failedLoginAttempts;
    private int lockOutMinutes;

    private final DataModel dataModel;

    @Inject
    UserSecuritySettingsImpl(DataModel dataModel){
        this.dataModel = dataModel;
    }

    UserSecuritySettingsImpl init(boolean lockAccountOption, int failedLoginAttempts, int lockOutMinutes) {
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockAccountOption = lockAccountOption;
        this.lockOutMinutes = lockOutMinutes;
        return this;
    }


    @Override
    public long getId(){
        return id;
    }

    @Override
    public boolean isLockAccountActive(){
        return lockAccountOption;
    }

    @Override
    public int getLockOutMinutes(){
        return lockOutMinutes;
    }

    @Override
    public int getFailedLoginAttempts(){
        return failedLoginAttempts;
    }


    @Override
    public void setLockAccountActive(boolean lockAccountOption){
        this.lockAccountOption = lockAccountOption;
    }

    @Override
    public void setLockOutMinutes(int lockOutMinutes){
        this.lockOutMinutes = lockOutMinutes;
    }

    @Override
    public void setFailedLoginAttempts(int failedLoginAttempts){
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public long getVersion(){
        return version;
    }

    @Override
    public void update() {
        Save.action(this.id).save(this.dataModel, this);
    }

    @Override
    public void save() {
        dataModel.mapper(UserSecuritySettings.class).persist(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserSecuritySettings)) {
            return false;
        }

        UserSecuritySettings userSecuritySettings = (UserSecuritySettings) o;

        return id == userSecuritySettings.getId();

    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String getName() {
        return "";
    }

}