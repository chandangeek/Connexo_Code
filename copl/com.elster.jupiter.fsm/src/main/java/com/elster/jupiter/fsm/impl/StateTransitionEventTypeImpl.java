package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionEventTypeStillInUseException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.List;
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
    private final Thesaurus thesaurus;
    private final ServerFiniteStateMachineService stateMachineService;

    @SuppressWarnings("unused")
    private long id;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    protected StateTransitionEventTypeImpl(DataModel dataModel, Thesaurus thesaurus, ServerFiniteStateMachineService stateMachineService) {
        super();
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.stateMachineService = stateMachineService;
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
        List<FiniteStateMachine> stateMachines = this.stateMachineService.findFiniteStateMachinesUsing(this);
        if (!stateMachines.isEmpty()) {
            throw new StateTransitionEventTypeStillInUseException(this.thesaurus, this, stateMachines);
        }
    }

}