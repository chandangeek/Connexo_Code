package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;
import com.elster.jupiter.fsm.impl.constraints.Unique;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.Map;

/**
 * Provides an implementation for the {@link CustomStateTransitionEventType} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-05 (10:05)
 */
@Unique(message = MessageSeeds.Keys.UNIQUE_EVENT_TYPE_SYMBOL, groups = { Save.Create.class, Save.Update.class })
public class CustomStateTransitionEventTypeImpl extends StateTransitionEventTypeImpl implements CustomStateTransitionEventType {

    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String symbol;

    @Inject
    public CustomStateTransitionEventTypeImpl(DataModel dataModel) {
        super(dataModel);
    }

    public CustomStateTransitionEventTypeImpl initialize(String symbol) {
        this.symbol = symbol;
        return this;
    }

    @Override
    public StateTransitionTriggerEvent newInstance(FinateStateMachine finateStateMachine, String sourceId, String sourceCurrentStateName, Map<String, Object> properties) {
        return this.getDataModel().getInstance(StateTransitionTriggerEventImpl.class).initialize(this, finateStateMachine, sourceId, properties, sourceCurrentStateName);
    }

    @Override
    public String getSymbol() {
        return this.symbol;
    }

}