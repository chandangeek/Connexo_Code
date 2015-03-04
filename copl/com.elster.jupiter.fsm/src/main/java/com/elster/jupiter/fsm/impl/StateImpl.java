package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.impl.constraints.UniqueName;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.google.common.collect.ImmutableMap;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link State} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (15:20)
 */
@UniqueName(message = MessageSeeds.Keys.UNIQUE_STATE_NAME, groups = { Save.Create.class, Save.Update.class })
public abstract class StateImpl implements State {

    public static final String CUSTOM = "0";
    public static final String DEVICE_LIFE_CYCLE = "1";

    public static final Map<String, Class<? extends State>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends State>>of(
                    CUSTOM, CustomStateImpl.class,
                    DEVICE_LIFE_CYCLE, DeviceLifeCycleStateImpl.class);

    public enum Fields {
        NAME("name"),
        FINATE_STATE_MACHINE("finateStateMachine"),
        PROCESS_REFERENCES("processReferences");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;

    private long id;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String name;
    @IsPresent
    private Reference<FinateStateMachine> finateStateMachine = Reference.empty();
    @Valid
    private List<ProcessReferenceImpl> processReferences = new ArrayList<>();
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    protected StateImpl(DataModel dataModel) {
        this.dataModel = dataModel;
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
    public String getName() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public FinateStateMachine getFinateStateMachine() {
        return finateStateMachine.get();
    }

    void setFinateStateMachine(FinateStateMachine finateStateMachine) {
        this.finateStateMachine.set(finateStateMachine);
    }

    @Override
    public List<StateTransition> getOutgoingStateTransitions() {
        return this.getFinateStateMachine()
                .getTransitions()
                .stream()
                .filter(t -> t.getFrom().getId() == this.getId())
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessReference> getOnEntryProcesses() {
        return this.getProcessReferences(ProcessReferenceImpl::isOnEntry);
    }

    @Override
    public List<ProcessReference> getOnExitProcesses() {
        return this.getProcessReferences(ProcessReferenceImpl::isOnExit);
    }

    private List<ProcessReference> getProcessReferences(Predicate<? super ProcessReferenceImpl> predicate) {
        return this.processReferences
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    void addOnEntry(String deploymentId, String processId) {
        this.processReferences.add(this.dataModel.getInstance(ProcessReferenceImpl.class).onEntry(this, deploymentId, processId));
    }

    void addOnExit(String deploymentId, String processId) {
        this.processReferences.add(this.dataModel.getInstance(ProcessReferenceImpl.class).onExit(this, deploymentId, processId));
    }

}