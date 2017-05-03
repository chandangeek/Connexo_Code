package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceKeyAccessorType;
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
@KeyEncryptionMethodValid(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
@DurationPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
@TrustStorePresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
public class KeyAccessorTypeImpl implements DeviceKeyAccessorType, PersistenceAware {
    private long id;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String description;
    @MaxTimeDuration(max = 946080000L, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.EXCESSIVE_TIME_DURATION + "}")
    private TimeDuration duration;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String keyEncryptionMethod;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<KeyType> keyType = Reference.empty();
    private Reference<TrustStore> trustStore = Reference.empty();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<DeviceType> deviceType = Reference.empty();
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

    private final Thesaurus thesaurus;
    private final DataModel dataModel;
    private final ThreadPrincipalService threadPrincipalService;

    enum Fields {
        ID("id"),
        NAME("name"),
        DESCRIPTION("description"),
        ENCRYPTIONMETHOD("keyEncryptionMethod"),
        DURATION("duration"),
        KEYTYPE("keyType"),
        TRUSTSTORE("trustStore"),
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
    public KeyAccessorTypeImpl(DataModel dataModel, ThreadPrincipalService threadPrincipalService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.threadPrincipalService = threadPrincipalService;
        this.thesaurus = thesaurus;
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
        return duration == null || duration.getTimeUnitCode() == 0 ? Optional.empty() : Optional.of(duration);
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
    public long getVersion() {
        return this.version;
    }

    @Override
    public Optional<TrustStore> getTrustStore() {
        return trustStore.getOptional();
    }

    public void setTrustStore(TrustStore trustStore) {
        this.trustStore.set(trustStore);
    }

    @Override
    public void postLoad() {
        userActions.addAll(userActionRecords.stream().map(UserActionRecord::getUserAction).collect(toList()));
    }

    void preDelete() {
        validateDelete();
        userActionRecords.clear();
        this.save();
    }

    protected void save() {
        Save.UPDATE.save(dataModel, this);
        dataModel.touch(deviceType.get());
    }

    private void validateDelete() {
        if (getDeviceType().getConfigurations().stream().anyMatch(DeviceConfiguration::isActive)) { // TODO provide better check
            throw new KeyAccessorTypeCanNotBeDeletedException(thesaurus);
        }
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

    public DeviceType getDeviceType() {
        return deviceType.get();
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
                if (iterator.next().getUserAction().equals(userAction)) {
                    iterator.remove();
                    dataModel.touch(this);
                    break;
                }
            }
        }
    }

    @Override
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

    @Override
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
