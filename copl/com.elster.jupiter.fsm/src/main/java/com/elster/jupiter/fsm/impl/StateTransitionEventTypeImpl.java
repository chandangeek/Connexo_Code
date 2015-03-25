package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Map;

/**
 * Provides an implementation for the {@link StateTransitionEventType} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:03)
 */
public abstract class StateTransitionEventTypeImpl implements StateTransitionEventType {

    public enum Fields {
        SYMBOL("symbol"),
        EVENT_TYPE("eventType");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }

    }

    public static final String CUSTOM = "0";
    public static final String STANDARD = "1";
    public static final Map<String, Class<? extends StateTransitionEventType>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends StateTransitionEventType>>of(
                    CUSTOM, CustomStateTransitionEventTypeImpl.class,
                    STANDARD, StandardStateTransitionEventTypeImpl.class);

    private final DataModel dataModel;

    private long id;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    public StateTransitionEventTypeImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    @Override
    public long getId() {
        return id;
    }

    protected DataModel getDataModel() {
        return dataModel;
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
    public void save() {
        Save.action(this.id).save(this.dataModel, this);
    }

    @Override
    public void delete() {
        this.validateDelete();
        this.dataModel.remove(this);
    }

    private void validateDelete() {
        // TODO: check of this EventType is not used in any StateTransition
    }

}