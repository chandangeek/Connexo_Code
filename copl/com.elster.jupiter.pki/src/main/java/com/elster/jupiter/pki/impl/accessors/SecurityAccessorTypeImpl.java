/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.accessors;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.hsm.model.keys.HsmJssKeyType;
import com.elster.jupiter.hsm.model.keys.HsmKeyType;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityAccessorTypeUpdater;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.impl.EventType;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.ProtocolKeyTypes;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.ShouldHaveUniqueName;
import com.elster.jupiter.util.UniqueName;

import com.google.inject.Inject;

import javax.validation.constraints.Size;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_UNIQUE + "}")
@KeyEncryptionMethodValid(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
@DurationPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
@TrustStorePresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
public class SecurityAccessorTypeImpl implements SecurityAccessorType, PersistenceAware, ShouldHaveUniqueName {
    private final DataModel dataModel;
    private final ThreadPrincipalService threadPrincipalService;
    private final EventService eventService;
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
    private Set<SecurityAccessorUserAction> userActions = EnumSet.noneOf(SecurityAccessorUserAction.class);
    private List<UserActionRecord> userActionRecords = new ArrayList<>();
    private boolean managedCentrally;
    private Purpose purpose;
    private HsmJssKeyType hsmJssKeyType;
    private String label;
    private SessionKeyCapability importCapability;
    private SessionKeyCapability renewCapability;
    private int keySize;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    public SecurityAccessorTypeImpl(DataModel dataModel, ThreadPrincipalService threadPrincipalService, EventService eventService) {
        this.dataModel = dataModel;
        this.threadPrincipalService = threadPrincipalService;
        this.eventService = eventService;
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
    public Set<SecurityAccessorUserAction> getUserActions() {
        return Collections.unmodifiableSet(userActions);
    }

    @Override
    public boolean isCurrentUserAllowedToEditProperties(String application) {
        Principal principal = threadPrincipalService.getPrincipal();
        if (principal instanceof User) {
            User user = (User) principal;
            for (SecurityAccessorUserAction securityAccessorUserAction : userActions) {
                if (editingIsAuthorizedFor(application, securityAccessorUserAction, user)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isCurrentUserAllowedToViewProperties(String application) {
        Principal principal = threadPrincipalService.getPrincipal();
        if (principal instanceof User) {
            User user = (User) principal;
            for (SecurityAccessorUserAction securityAccessorUserAction : userActions) {
                if (viewingIsAuthorizedFor(application, securityAccessorUserAction, user)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public SecurityAccessorTypeUpdater startUpdate() {
        return new SecurityAccessorTypeUpdaterImpl();
    }

    @Override
    public void delete() {
        eventService.postEvent(EventType.SECURITY_ACCESSOR_TYPE_VALIDATE_DELETE.topic(), this);
        userActionRecords.clear();
        dataModel.remove(this);
        eventService.postEvent(EventType.SECURITY_ACCESSOR_TYPE_DELETED.topic(), this);
    }

    @Override
    public boolean isManagedCentrally() {
        return managedCentrally;
    }

    protected void setManagedCentrally(boolean managedCentrally) {
        this.managedCentrally = managedCentrally;
    }

    @Override
    public Purpose getPurpose() {
        return purpose;
    }

    protected void setPurpose(Purpose purpose) {
        this.purpose = purpose;
    }

    @Override
    public HsmKeyType getHsmKeyType() {
        return new HsmKeyType(hsmJssKeyType, label, importCapability, renewCapability, keySize);
    }

    @Override
    public boolean keyTypeIsHSM() {
        return ProtocolKeyTypes.HSM.getName().equals(this.getKeyType().getName());
    }

    public void setKeyEncryptionMethod(String keyEncryptionMethod) {
        this.keyEncryptionMethod = keyEncryptionMethod;
    }

    protected void setDuration(TimeDuration duration) {
        this.duration = duration;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean hasUniqueName() {
        return !dataModel.mapper(getClass()).getUnique(Fields.NAME.fieldName(), getName())
                .filter(kat -> kat.getId() != getId())
                .isPresent();
    }

    @Override
    public void postLoad() {
        userActions.addAll(userActionRecords.stream().map(UserActionRecord::getUserAction).collect(toList()));
    }

    protected void save() {
        Save.UPDATE.save(dataModel, this);
    }

    protected void setLabel(String label) {
        this.label = label;
    }

    protected void setHsmJssKeyType(HsmJssKeyType hsmJssKeyType) {
        this.hsmJssKeyType = hsmJssKeyType;
    }

    protected void setImportCapability(SessionKeyCapability importCapability) {
        this.importCapability = importCapability;
    }

    protected void setRenewCapability(SessionKeyCapability renewCapability) {
        this.renewCapability = renewCapability;
    }

    protected void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    protected void addUserAction(SecurityAccessorUserAction userAction) {
        boolean changed = userActions.add(userAction);
        if (changed) {
            userActionRecords.add(new UserActionRecord(this, userAction));
        }
    }

    private void removeUserAction(SecurityAccessorUserAction userAction) {
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

    private boolean viewingIsAuthorizedFor(String application, SecurityAccessorUserAction action, User user) {
        return action.isViewing() && isAuthorized(application, action, user);
    }

    private boolean isAuthorized(String application, SecurityAccessorUserAction action, User user) {
        return user.hasPrivilege(application, action.getPrivilege());
    }

    private boolean editingIsAuthorizedFor(String application, SecurityAccessorUserAction action, User user) {
        return action.isEditing() && isAuthorized(application, action, user);
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || o instanceof SecurityAccessorTypeImpl
                && id == ((SecurityAccessorTypeImpl) o).id;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + name;
    }


    public enum Fields {
        ID("id"),
        NAME("name"),
        DESCRIPTION("description"),
        ENCRYPTIONMETHOD("keyEncryptionMethod"),
        DURATION("duration"),
        KEYTYPE("keyType"),
        TRUSTSTORE("trustStore"),
        MANAGED_CENTRALLY("managedCentrally"),
        PURPOSE("purpose"),
        HSM_JSS_KEY_TYPE("hsmJssKeyType"),
        LABEL("label"),
        IMPORT_CAPABILITY("importCapability"),
        RENEW_CAPABILITY("renewCapability"),
        KEY_SIZE("keySize");


        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    protected class SecurityAccessorTypeUpdaterImpl implements SecurityAccessorTypeUpdater {
        private SecurityAccessorTypeUpdaterImpl() {
        }

        @Override
        public SecurityAccessorType.Updater name(String name) {
            SecurityAccessorTypeImpl.this.setName(name);
            return this;
        }

        @Override
        public SecurityAccessorType.Updater description(String description) {
            SecurityAccessorTypeImpl.this.setDescription(description);
            return this;
        }

        @Override
        public SecurityAccessorType.Updater duration(TimeDuration duration) {
            SecurityAccessorTypeImpl.this.setDuration(duration);
            return this;
        }

        @Override
        public Updater jssKeyType(HsmJssKeyType hsmJssKeyType) {
            SecurityAccessorTypeImpl.this.setHsmJssKeyType(hsmJssKeyType);
            return this;
        }

        @Override
        public SecurityAccessorType.Updater label(String label) {
            SecurityAccessorTypeImpl.this.setLabel(label);
            return this;
        }

        @Override
        public Updater importCapabilty(SessionKeyCapability importCapabilty) {
            SecurityAccessorTypeImpl.this.setImportCapability(importCapabilty);
            return this;
        }

        @Override
        public Updater renewCapability(SessionKeyCapability renewCapability) {
            SecurityAccessorTypeImpl.this.setRenewCapability(renewCapability);
            return this;
        }

        @Override
        public Updater keySize(int keySize) {
            SecurityAccessorTypeImpl.this.setKeySize(keySize);
            return this;
        }

        @Override
        public SecurityAccessorType complete() {
            SecurityAccessorTypeImpl.this.save();
            return SecurityAccessorTypeImpl.this;
        }

        @Override
        public SecurityAccessorTypeUpdater addUserAction(SecurityAccessorUserAction userAction) {
            SecurityAccessorTypeImpl.this.addUserAction(userAction);
            return this;
        }

        @Override
        public SecurityAccessorTypeUpdater removeUserAction(SecurityAccessorUserAction userAction) {
            SecurityAccessorTypeImpl.this.removeUserAction(userAction);
            return this;
        }


    }
}
