package com.elster.jupiter.demo.impl;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-29 (10:06)
 */
public class ConsoleUser implements User {
    @Override
    public String getName() {
        return "console";
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public String getDescription() {
        return "@see DemoServiceImpl";
    }

    @Override
    public long getVersion() {
        return 1;
    }

    @Override
    public long getUserDirectoryId() {
        return 1;
    }

    @Override
    public boolean hasPrivilege(String applicationName, String privilege) {
        return true;
    }

    @Override
    public boolean hasPrivilege(String applicationName, Privilege privilege) {
        return true;
    }

    @Override
    public boolean isMemberOf(String groupName) {
        return false;
    }

    @Override
    public void setDescription(String description) {

    }

    @Override
    public void update() {
        throw new UnsupportedOperationException(this.getClass().getName() + " is not designed to be saved");
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException(this.getClass().getName() + " is not designed to be deleted");
    }

    @Override
    public boolean join(Group group) {
        throw new UnsupportedOperationException(this.getClass().getName() + " is not designed to be join groups");
    }

    @Override
    public boolean leave(Group group) {
        throw new UnsupportedOperationException(this.getClass().getName() + " is not designed to be leave groups");
    }

    @Override
    public boolean isMemberOf(Group group) {
        return false;
    }

    @Override
    public List<Group> getGroups() {
        return Collections.emptyList();
    }

    @Override
    public List<WorkGroup> getWorkGroups() {
        return Collections.emptyList();
    }

    @Override
    public String getDigestHa1() {
        return null;
    }

    @Override
    public void setPassword(String password) {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support changing password");
    }

    @Override
    public boolean check(String password) {
        return true;
    }

    @Override
    public Optional<Locale> getLocale() {
        return Optional.of(Locale.getDefault());
    }

    @Override
    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support setting Locale");
    }

    @Override
    public Set<Privilege> getPrivileges() {
        Set<Privilege> privileges = new HashSet<>();
        privileges.addAll(
                Stream
                    .of(EditPrivilege.values())
                    .map(CustomPropertySetPrivilege::from)
                    .collect(Collectors.toList()));
        privileges.addAll(
                Stream
                    .of(ViewPrivilege.values())
                    .map(CustomPropertySetPrivilege::from)
                    .collect(Collectors.toList()));
        return privileges;
    }

    @Override
    public Map<String, List<Privilege>> getApplicationPrivileges() {
        return Collections.emptyMap();
    }

    @Override
    public Set<Privilege> getPrivileges(String applicationName) {
        return Collections.emptySet();
    }

    @Override
    public String getDomain() {
        return "Demo";
    }

    @Override
    public boolean getStatus() {
        return true;
    }

    @Override
    public void setStatus(boolean status) {
    }

    @Override
    public String getLanguage() {
        return "EN";
    }

    @Override
    public Instant getCreationDate() {
        return Instant.now();
    }

    @Override
    public Instant getModifiedDate() {
        return Instant.now();
    }

    @Override
    public Instant getLastSuccessfulLogin() {
        return Instant.now();
    }

    @Override
    public void setLastSuccessfulLogin(Instant lastLogin) {

    }

    @Override
    public Instant getLastUnSuccessfulLogin() {
        return null;
    }

    @Override
    public void setLastUnSuccessfulLogin(Instant lastLoginFail) {

    }

    private static class CustomPropertySetPrivilege implements Privilege {
        private final String privilege;

        private CustomPropertySetPrivilege(String privilege) {
            super();
            this.privilege = privilege;
        }

        static CustomPropertySetPrivilege from(EditPrivilege editPrivilege) {
            return new CustomPropertySetPrivilege(editPrivilege.getPrivilege());
        }

        static CustomPropertySetPrivilege from(ViewPrivilege viewPrivilege) {
            return new CustomPropertySetPrivilege(viewPrivilege.getPrivilege());
        }

        @Override
        public void delete() {
            throw new UnsupportedOperationException("Delete is not supported");
        }

        @Override
        public PrivilegeCategory getCategory() {
            return null;
        }

        @Override
        public String getName() {
            return this.privilege;
        }
    }

}