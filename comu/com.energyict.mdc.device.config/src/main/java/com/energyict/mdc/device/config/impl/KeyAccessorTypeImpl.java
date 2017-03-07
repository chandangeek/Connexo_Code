package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.KeyAccessorTypeUpdater;

import com.google.inject.Inject;

import javax.validation.constraints.Size;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@HasUniqueNamePerDeviceType(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_UNIQUE + "}")
public class KeyAccessorTypeImpl implements KeyAccessorType, PersistenceAware {
    private long id;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String description;
    private TimeDuration duration;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String keyEncryptionMethod;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<KeyType> keyType = Reference.empty();
    @IsPresent
    private Reference<DeviceType> deviceType = Reference.empty();
    private DataModel dataModel;
    private final ThreadPrincipalService threadPrincipalService;
    private Set<DeviceSecurityUserAction> userActions = EnumSet.noneOf(DeviceSecurityUserAction.class);
    private List<UserActionRecord> userActionRecords = new ArrayList<>();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    enum Fields {
        ID("id"),
        NAME("name"),
        DESCRIPTION("description"),
        ENCRYPTIONMETHOD("keyEncryptionMethod"),
        DURATION("duration"),
        KEYTYPE("keyType"),
        DEVICETYPE("deviceType");

        private final String javaFieldName;
        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }

    }
    @Inject
    public KeyAccessorTypeImpl(DataModel dataModel, ThreadPrincipalService threadPrincipalService) {
        this.dataModel = dataModel;
        this.threadPrincipalService = threadPrincipalService;
    }

    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Optional<TimeDuration> getDuration() {
        return duration == null ? Optional.empty() : Optional.of(duration);
    }

    @Override
    public KeyType getKeyType() {
        return keyType.get();
    }

    public void setKeyType(KeyType keyType) {
        this.keyType.set(keyType);
    }

    @Override
    public String getKeyEncryptionMethod() {
        return keyEncryptionMethod;
    }

    @Override
    public void postLoad() {
        userActions.addAll(userActionRecords.stream().map(userActionRecord -> userActionRecord.userAction).collect(toList()));
    }

    protected void save() {
        Save.UPDATE.save(dataModel, this);
        dataModel.touch(deviceType.get());
    }

    public void setKeyEncryptionMethod(String keyEncryptionMethod) {
        this.keyEncryptionMethod = keyEncryptionMethod;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected void setDuration(TimeDuration duration) {
        this.duration = duration;
    }

    public Optional<DeviceType> getDeviceType() {
        return deviceType.getOptional();
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType.set(deviceType);
    }

    public Set<DeviceSecurityUserAction> getUserActions() {
        return this.userActions;
    }

    protected void addUserAction(DeviceSecurityUserAction userAction) {
        boolean changed = userActions.add(userAction);
        if (changed) {
            userActionRecords.add(new UserActionRecord(this, userAction));
        }
    }

    private void removeUserAction(DeviceSecurityUserAction userAction) {
        boolean changed = userActions.remove(userAction);
        if (changed) {
            for (Iterator<UserActionRecord> iterator = userActionRecords.iterator(); iterator.hasNext(); ) {
                if (iterator.next().userAction.equals(userAction)) {
                    iterator.remove();
                    dataModel.touch(this);
                    break;
                }
            }
        }
    }

    public boolean currentUserIsAllowedToEditDeviceProperties() {
        Principal principal = threadPrincipalService.getPrincipal();
        if (!(principal instanceof User)) {
            return false;
        }
        User user = (User) principal;
        Set<DeviceSecurityUserAction> deviceSecurityUserActions = this.getUserActions();
        for (DeviceSecurityUserAction deviceSecurityUserAction : deviceSecurityUserActions) {
            if (editingIsAuthorizedFor(deviceSecurityUserAction, user)) {
                return true;
            }
        }
        return false;
    }

    public boolean currentUserIsAllowedToViewDeviceProperties() {
        Principal principal = threadPrincipalService.getPrincipal();
        if (!(principal instanceof User)) {
            return false;
        }
        User user = (User) principal;
        Set<DeviceSecurityUserAction> deviceSecurityUserActions = this.getUserActions();
        for (DeviceSecurityUserAction deviceSecurityUserAction : deviceSecurityUserActions) {
            if (viewingIsAuthorizedFor(deviceSecurityUserAction, user)) {
                return true;
            }
        }
        return false;
    }

    private boolean viewingIsAuthorizedFor(DeviceSecurityUserAction action, User user) {
        return action.isViewing() && this.isAuthorized(action, user);
    }

    private boolean isAuthorized(DeviceSecurityUserAction action, User user) {
        return user.hasPrivilege("MDC", action.getPrivilege());
    }

    private boolean editingIsAuthorizedFor(DeviceSecurityUserAction action, User user) {
        return action.isEditing() && this.isAuthorized(action, user);
    }

    @Override
    public KeyAccessorTypeUpdater startUpdate() {
        return new KeyAccessorTypeUpdaterImpl();
    }

    protected boolean nameIsUnique() {
        if(deviceType.isPresent()) {
            return !deviceType.get().getKeyAccessorTypes().stream()
                    .filter(keyAccessorType -> !keyAccessorType.equals(this))
                    .filter(keyAccessorType -> keyAccessorType.getName().equals(this.getName()))
                    .findAny()
                    .isPresent();
        }
        return false;
    }

    static class UserActionRecord {
        private DeviceSecurityUserAction userAction;
        private Reference<KeyAccessorType> keyAccessorType = ValueReference.absent();
        @SuppressWarnings("unused")
        private String userName;
        @SuppressWarnings("unused")
        private long version;
        @SuppressWarnings("unused")
        private Instant createTime;
        @SuppressWarnings("unused")
        private Instant modTime;

        UserActionRecord() {
        }

        UserActionRecord(KeyAccessorType keyAccessorType, DeviceSecurityUserAction userAction) {
            this();
            this.keyAccessorType.set(keyAccessorType);
            this.userAction = userAction;
        }
    }

    protected class KeyAccessorTypeUpdaterImpl implements KeyAccessorTypeUpdater {

        private KeyAccessorTypeUpdaterImpl() {

        }

        @Override
        public Updater name(String name) {
            KeyAccessorTypeImpl.this.setName(name);
            return this;
        }

        @Override
        public Updater description(String description) {
            KeyAccessorTypeImpl.this.setDescription(description);
            return this;
        }

        @Override
        public Updater duration(TimeDuration duration) {
            KeyAccessorTypeImpl.this.setDuration(duration);
            return this;
        }

        @Override
        public KeyAccessorTypeUpdater addUserAction(DeviceSecurityUserAction userAction) {
            KeyAccessorTypeImpl.this.addUserAction(userAction);
            return this;
        }

        @Override
        public KeyAccessorTypeUpdater removeUserAction(DeviceSecurityUserAction userAction) {
            KeyAccessorTypeImpl.this.removeUserAction(userAction);
            return this;
        }

        @Override
        public KeyAccessorType complete() {
            KeyAccessorTypeImpl.this.save();
            return KeyAccessorTypeImpl.this;
        }
    }
}
