package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.WorkGroup;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;
import static com.elster.jupiter.util.Checks.is;

public final class UserImpl implements User {

    private static final int MINIMAL_PASSWORD_STRENGTH = 4;
    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    private String authenticationName;
    @Size(max = SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_256 + "}")
    private String description;
    @Size(max = 65, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_65 + "}")
    private String ha1;
    private int salt;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    private boolean status;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    private Instant lastSuccessfulLogin;
    private Instant lastUnSuccessfulLogin;
    @Size(max = 64, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_64 + "}")
    private String languageTag;
    private Reference<UserDirectory> userDirectory = ValueReference.absent();

    // transient
    private List<UserInGroup> memberships;

    private final DataModel dataModel;
    private final Publisher publisher;

    @Inject
    UserImpl(DataModel dataModel, Publisher publisher) {
        this.dataModel = dataModel;
        this.publisher = publisher;
    }

    static UserImpl from(DataModel dataModel, UserDirectory userDirectory, String authenticationName, boolean allowPwdChange, boolean status) {
        return from(dataModel, userDirectory, authenticationName, null, allowPwdChange, status);
    }

    static UserImpl from(DataModel dataModel, UserDirectory userDirectory, String authenticationName, String description, boolean allowPwdChange, boolean status) {
        return dataModel
                .getInstance(UserImpl.class)
                .init(userDirectory, authenticationName, description, allowPwdChange, status);
    }

    UserImpl init(UserDirectory userDirectory, String authenticationName, String description, boolean allowPwdChange, boolean status) {
        validateAuthenticationName(authenticationName);
        this.status = status;
        this.userDirectory.set(userDirectory);
        this.authenticationName = authenticationName;
        this.description = description;
        return this;
    }

    private void validateAuthenticationName(String authenticationName) {
        if (is(authenticationName).emptyOrOnlyWhiteSpace()) {
            throw new IllegalArgumentException("User's authentication name cannot be empty.");
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean hasPrivilege(String applicationName, String privilegeName) {
        for (Group each : getGroups()) {
            if (each.hasPrivilege(applicationName, privilegeName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasPrivilege(String applicationName, Privilege privilege) {
        for (Group each : getGroups()) {
            if (each.hasPrivilege(applicationName, privilege)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return authenticationName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;

    }

    @Override
    public boolean join(Group group) {
        if (isMemberOf(group)) {
            return false;
        }
        UserInGroup membership = UserInGroup.from(dataModel, this, group);
        if (memberships != null) {
            memberships.add(membership);
        }
        membership.persist();
        publisher.publish(this, group, true);
        return true;
    }

    @Override
    public boolean isMemberOf(Group group) {
        return getGroups().contains(group);
    }

    @Override
    public boolean leave(Group group) {
        for (UserInGroup userInGroup : getMemberships()) {
            if (group.equals(userInGroup.getGroup())) {
                userInGroup.delete();
                if (memberships != null) {
                    memberships.remove(userInGroup);
                }
                publisher.publish(this, group, false);
                return true;
            }
        }
        return false;
    }

    private List<UserInGroup> getMemberships() {
        if (memberships == null) {
            return dataModel.mapper(UserInGroup.class).find("user", this);
        }
        return memberships;
    }

    @Override
    public List<Group> getGroups() {
        return userDirectory.get().getGroups(this);
    }

    List<Group> doGetGroups() {
        List<UserInGroup> userInGroups = getMemberships();
        ImmutableList.Builder<Group> builder = ImmutableList.builder();
        for (UserInGroup each : userInGroups) {
            builder.add(each.getGroup());
        }
        return builder.build();
    }

    @Override
    public long getUserDirectoryId() {
        return userDirectory.get().getId();
    }

    public void update() {
        if (id == 0) {
            dataModel.mapper(User.class).persist(this);
        } else {
            dataModel.mapper(User.class).update(this);
        }
    }

    public Instant getCreateDate() {
        return createTime;
    }

    public Instant getModificationDate() {
        return modTime;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return " User " + authenticationName;
    }

    @Override
    public boolean isMemberOf(String groupName) {
        for (Group each : getGroups()) {
            if (each.getName().equals(groupName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void delete() {
        this.deleteMemberships();
        dataModel.mapper(User.class).remove(this);
    }

    private void deleteMemberships() {
        memberships = null;
        getMemberships()
                .stream()
                .forEach(UserInGroup::delete);
        dataModel.mapper(UsersInWorkGroup.class).find("userId", this.getId())
                .stream()
                .forEach(UsersInWorkGroup::delete);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }

        User user = (User) o;

        return id == user.getId();

    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String getDigestHa1() {
        throw new UnsupportedOperationException("Deprecated since 2.0");
    }

    @Override
    public void setPassword(String password) {
        password = Objects.requireNonNull(password);
        if (password.length() < MINIMAL_PASSWORD_STRENGTH) {
            throw new IllegalArgumentException("Password too weak");
        }
        salt = new SecureRandom().nextInt();
        ha1 = createHa1(password, salt);
    }

    private String createHa1(String password, int salt) {
        return new HashingUtil().createHash(password, salt);
    }

    @Override
    public boolean check(String password) {
        return !is(password).empty() && createHa1(password, salt).equals(ha1);
    }

    @Override
    public Optional<Locale> getLocale() {
        if (languageTag == null) {
            return Optional.empty();
        }
        return Optional.of(Locale.forLanguageTag(languageTag));
    }

    @Override
    public void setLocale(Locale locale) {
        languageTag = locale == null ? null : locale.toLanguageTag();
    }

    @Override
    public Set<Privilege> getPrivileges() {
        return getPrivileges(null);
    }

    public Map<String, List<Privilege>> getApplicationPrivileges() {
        Map<String, List<Privilege>> privileges = new HashMap<String, List<Privilege>>();
        List<Group> groups = getGroups();
        for (Group group : groups) {
            Map<String, List<Privilege>> groupPrivileges = group.getPrivileges();

            privileges = Stream.of(groupPrivileges, privileges)
                    .map(Map::entrySet)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
                        List<Privilege> both = new ArrayList<>(a);
                        both.addAll(b);
                        return both;
                    }));
        }
        return privileges;
    }

    @Override
    public Set<Privilege> getPrivileges(String applicationName) {
        Set<Privilege> privileges = new HashSet<>();
        List<Group> groups = getGroups();
        for (Group group : groups) {
            privileges.addAll(group.getPrivileges(applicationName));
        }

        return privileges;
    }

    @Override
    public String getDomain() {
        return userDirectory.get().getDomain();
    }

    @Override
    public boolean getStatus() {
        return status;
    }

    @Override
    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String getLanguage() {
        return languageTag;
    }

    @Override
    public Instant getCreationDate() {
        return createTime;
    }

    @Override
    public Instant getModifiedDate() {
        return modTime;
    }

    public Instant getLastSuccessfulLogin() {
        return lastSuccessfulLogin;
    }

    public void setLastSuccessfulLogin(Instant lastSuccessfulLogin) {
        this.lastSuccessfulLogin = lastSuccessfulLogin;
        this.dataModel.update(this, "lastSuccessfulLogin");
    }

    public Instant getLastUnSuccessfulLogin() {
        return lastUnSuccessfulLogin;
    }

    public void setLastUnSuccessfulLogin(Instant lastUnSuccessfulLogin) {
        this.lastUnSuccessfulLogin = lastUnSuccessfulLogin;
        this.dataModel.update(this, "lastUnSuccessfulLogin");
    }

    @Override
    public List<WorkGroup> getWorkGroups() {
        return dataModel.mapper(UsersInWorkGroup.class).find("userId", this.getId())
                .stream().map(UsersInWorkGroup::getWorkGroup).collect(Collectors.toList());
    }

}