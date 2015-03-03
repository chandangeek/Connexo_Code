package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.impl.constraints.UniqueName;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * Provides an implementation for the {@link StateTransitionEventType} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:03)
 */
@UniqueName(message = MessageSeeds.Keys.UNIQUE_EVENT_TYPE_SYMBOL, groups = { Save.Create.class, Save.Update.class })
public class StateTransitionEventTypeImpl implements StateTransitionEventType {

    public enum Fields {
        SYMBOL("symbol");

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
    private String symbol;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    public StateTransitionEventTypeImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    public StateTransitionEventType initialize(String symbol) {
        this.symbol = symbol;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getSymbol() {
        return this.symbol;
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