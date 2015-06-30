package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Serves as the root of the class hierarchy that will implement
 * the {@link AuthorizedAction} interface hierarchy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (11:53)
 */
public abstract class AuthorizedActionImpl implements AuthorizedAction, PersistenceAware {

    public enum Fields {
        // Common fields
        DEVICE_LIFE_CYCLE("deviceLifeCycle"),
        LEVELS("levelBits"),
        // AuthorizedTransitionAction
        STATE_TRANSITION("stateTransition"),
        CHECKS("checkBits"),
        ACTIONS("actionBits"),
        // AuthorizedStandardTransitionAction
        TYPE("type"),
        // AuthorizedBusinessProcessAction
        STATE("state"),
        NAME("name"),
        PROCESS("process");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }

    }
    public static final String STANDARD_TRANSITION = "0";

    public static final String CUSTOM_TRANSITION = "1";
    public static final String CUSTOM = "2";
    public static final Map<String, Class<? extends AuthorizedAction>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends AuthorizedAction>>of(
                    STANDARD_TRANSITION, AuthorizedStandardTransitionActionImpl.class,
                    CUSTOM_TRANSITION, AuthorizedCustomTransitionActionImpl.class,
                    CUSTOM, AuthorizedBusinessProcessActionImpl.class);

    private final DataModel dataModel;

    @SuppressWarnings("unused")
    private long id;

    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", groups = { Save.Create.class, Save.Update.class })
    private Reference<DeviceLifeCycleImpl> deviceLifeCycle = ValueReference.absent();
    private int levelBits;
    private EnumSet<Level> levels = EnumSet.noneOf(Level.class);
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    protected AuthorizedActionImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    @Override
    public DeviceLifeCycleImpl getDeviceLifeCycle() {
        return deviceLifeCycle.get();
    }

    protected void setDeviceLifeCycle(DeviceLifeCycleImpl deviceLifeCycle) {
        this.deviceLifeCycle.set(deviceLifeCycle);
    }

    @Override
    public void postLoad() {
        this.postLoadLevelEnumSet();
    }

    private void postLoadLevelEnumSet() {
        int mask = 1;
        for (Level level : Level.values()) {
            if ((this.levelBits & mask) != 0) {
                // The bit corresponding to the current level is set so add it to the set.
                this.levels.add(level);
            }
            mask = mask * 2;
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreationTimestamp() {
        return createTime;
    }

    @Override
    public Instant getModifiedTimestamp() {
        return modTime;
    }

    @Override
    public Set<Level> getLevels() {
        return EnumSet.copyOf(this.levels);
    }

    void clearLevels() {
        this.levelBits = 0;
        this.levels = EnumSet.noneOf(AuthorizedAction.Level.class);
    }

    void add(Level level) {
        this.levelBits |= (1L << level.ordinal());
        this.levels.add(level);
    }

    void notifyUpdated() {
        this.deviceLifeCycle.get().updated(this);
    }

    protected void save() {
        Save.action(this.id).save(this.dataModel, this);
    }

}