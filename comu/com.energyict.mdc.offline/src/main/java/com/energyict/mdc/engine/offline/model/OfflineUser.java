package com.energyict.mdc.engine.offline.model;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserSecuritySettings;
import com.elster.jupiter.users.WorkGroup;
import com.energyict.mdc.engine.users.OfflineUserInfo;
import com.energyict.mdc.engine.users.PrivilegesWrapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class OfflineUser implements User {

    OfflineUserInfo userInfo;

    public OfflineUser(OfflineUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public long getId() {
        return userInfo.getId();
    }

    @Override
    public boolean hasPrivilege(String applicationName, String privilege) {
        return false;
    }

    @Override
    public boolean hasPrivilege(String applicationName, Privilege privilege) {
        return false;
    }

    @Override
    public boolean isMemberOf(String groupName) {
        return false;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public long getVersion() {
        return 0;
    }

    @Override
    public long getUserDirectoryId() {
        return 0;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public void setDescription(String description) {

    }

    @Override
    public void setEmail(String email) {

    }

    @Override
    public void update() {

    }

    @Override
    public void delete() {

    }

    @Override
    public boolean join(Group group) {
        return false;
    }

    @Override
    public boolean leave(Group group) {
        return false;
    }

    @Override
    public boolean isMemberOf(Group group) {
        return false;
    }

    @Override
    public List<Group> getGroups() {
        return null;
    }

    @Override
    public Integer getSalt() {
        return userInfo.getSalt();
    }

    @Override
    public String getDigestHa1() {
        return userInfo.getHash();
    }

    @Override
    public void setPassword(String password) {

    }

    @Override
    public boolean check(String password) {
        return false;
    }

    @Override
    public Optional<Locale> getLocale() {
        return Optional.empty();
    }

    @Override
    public void setLocale(Locale locale) {

    }

    @Override
    public Set<Privilege> getPrivileges() {
        Set<Privilege> privileges = new HashSet<Privilege>();
        for (List<Privilege> list : getApplicationPrivileges().values())
            privileges.addAll(list);
        return privileges;
    }

    @Override
    public Map<String, List<Privilege>> getApplicationPrivileges() {
        Map<String, List<Privilege>> applicationPrivileges = new HashMap<String, List<Privilege>>();
        for (Map.Entry<String, PrivilegesWrapper> entry : userInfo.getApplicationPrivileges().entrySet())
            applicationPrivileges.put(entry.getKey(), entry.getValue().getList());
        return applicationPrivileges;
    }

    @Override
    public Set<Privilege> getPrivileges(String applicationName) {
        if (applicationName == null)
            return getPrivileges();
        return new HashSet<Privilege>(getApplicationPrivileges().get(applicationName));
    }

    @Override
    public String getDomain() {
        return null;
    }

    @Override
    public boolean getStatus() {
        return false;
    }

    @Override
    public void setStatus(boolean status) {

    }

    @Override
    public String getLanguage() {
        return null;
    }

    @Override
    public Instant getCreationDate() {
        return null;
    }

    @Override
    public Instant getModifiedDate() {
        return null;
    }

    @Override
    public Instant getLastSuccessfulLogin() {
        return null;
    }

    @Override
    public void setLastSuccessfulLogin(Instant lastLogin) {

    }

    @Override
    public Instant getLastUnSuccessfulLogin() {
        return null;
    }

    @Override
    public void setLastUnSuccessfulLogin(Instant lastLoginFail, Optional<UserSecuritySettings> userSecuritySettings) {

    }

    @Override
    public int getUnSuccessfulLoginCount() {
        return 0;
    }

    @Override
    public void setUnSuccessfulLoginCount(int unSuccessfulLoginCount) {
    }

    @Override
    public void setRoleModified(boolean status) {

    }

    @Override
    public boolean isRoleModified() {
        return false;
    }

    @Override
    public boolean isUserLocked(Optional<UserSecuritySettings> userSecuritySettings) {
        return false;
    }

    @Override
    public List<WorkGroup> getWorkGroups() {
        return null;
    }

    @Override
    public String getName() {
        return userInfo.getUserName();
    }

    @Override
    public String getExternalId() {
        return null;
    }
}
