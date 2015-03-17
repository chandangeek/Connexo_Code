package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public abstract class AbstractEndDeviceImpl<S extends AbstractEndDeviceImpl<S>> implements ServerEndDevice {
	static final Map<String, Class<? extends EndDevice>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends EndDevice>>of(EndDevice.TYPE_IDENTIFIER, EndDeviceImpl.class, Meter.TYPE_IDENTIFIER, MeterImpl.class);
	// persistent fields
	private long id;
	private int amrSystemId;
	private String amrId;
	private String aliasName;
	private String description;
	private String mRID;
	private String name;
	private String serialNumber;
	private String utcNumber;
	private ElectronicAddress electronicAddress;
	private long version;
	private Instant createTime;
	private Instant modTime;
	@SuppressWarnings("unused")
	private String userName;

	// associations
	private AmrSystem amrSystem;
    private Reference<FiniteStateMachine> stateMachine = ValueReference.absent();
    private StateManager stateManager = new NoDeviceLifeCycle();

    private final Clock clock;
    private final DataModel dataModel;
    private final EventService eventService;
    private final Provider<EndDeviceEventRecordImpl> deviceEventFactory;
    private final S self;

    AbstractEndDeviceImpl(Clock clock, DataModel dataModel, EventService eventService, Provider<EndDeviceEventRecordImpl> deviceEventFactory, Class<? extends S> selfType) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.deviceEventFactory = deviceEventFactory;
        this.clock = clock;
        self = selfType.cast(this);
	}

	S init(AmrSystem system, String amrId, String mRID) {
		this.amrSystemId = system.getId();
		this.amrSystem = system;
		this.amrId = amrId;
		this.mRID = mRID;
        return self;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getAliasName() {
		return aliasName == null ? "" : aliasName;
	}

	@Override
	public String getDescription() {
		return description == null ? "" : description;
	}

	@Override
	public String getMRID() {
		return mRID;
	}

	@Override
	public String getName() {
		return name ==  null ? "" : name;
	}

	@Override
	public void save() {
		if (id == 0) {
			dataModel.mapper(EndDevice.class).persist(this);
            eventService.postEvent(EventType.METER_CREATED.topic(), this);
        } else {
            dataModel.mapper(EndDevice.class).update(this);
            eventService.postEvent(EventType.METER_UPDATED.topic(), this);
		}
        this.stateManager = this.stateManager.save();
	}

    @Override
    public void delete() {
        dataModel.mapper(EndDevice.class).remove(this);
        eventService.postEvent(EventType.METER_DELETED.topic(), this);
    }

	@Override
	public String getSerialNumber() {
		return serialNumber == null ? "" : serialNumber;
	}

	@Override
	public String getUtcNumber() {
		return utcNumber == null ? "" : utcNumber;
	}

	@Override
	public ElectronicAddress getElectronicAddress() {
		return electronicAddress == null ? null : electronicAddress.copy();
	}

	@Override
	public AmrSystem getAmrSystem() {
		if (amrSystem == null) {
			amrSystem = dataModel.mapper(AmrSystem.class).getExisting(amrSystemId);
		}
		return amrSystem;
	}

    @Override
    public Optional<FiniteStateMachine> getFiniteStateMachine() {
        return this.stateMachine.getOptional();
    }

    void setFiniteStateMachine(FiniteStateMachine stateMachine) {
        this.stateMachine.set(stateMachine);
        // Assumed to be called a construction time only for now
        this.stateManager = new CreateInitialState();
    }

    @Override
    public void changeState(State newState) {
        this.validateState(newState);
        Instant now = this.clock.instant();
        this.closeCurrentState(now);
        this.createNewState(now, newState);
        this.dataModel.touch(this);
    }

    private void validateState(State state) {
        if (this.isDeviceLifeCycleManaged()) {
            if (state.getFiniteStateMachine().getId() != this.getFiniteStateMachine().get().getId()) {
                throw new IllegalArgumentException("Changing the state of an EndDevice to a state that is not part of its state machine is not allowed");
            }
        }
        else {
            throw new UnsupportedOperationException("Changing the state of an EndDevice whose state is not managed is not possible");
        }
    }

    private void closeCurrentState(Instant now) {
        this.findCurrentDeviceLifeCycleStatus().ifPresent(s -> this.closeCurrentState(s, now));
    }

    private void closeCurrentState(EndDeviceLifeCycleStatus status, Instant now) {
        status.close(now);
        this.dataModel.update(status);
    }
    private void createNewState(Instant effective, State state) {
        Interval stateEffectivityInterval = Interval.of(Range.atLeast(effective));
        EndDeviceLifeCycleStatusImpl deviceLifeCycleStatus = this.dataModel
                .getInstance(EndDeviceLifeCycleStatusImpl.class)
                .initialize(stateEffectivityInterval, this, state);
        this.dataModel.persist(deviceLifeCycleStatus);
    }

    @Override
    public Optional<State> getState() {
        if (this.isDeviceLifeCycleManaged()) {
            return this.findCurrentDeviceLifeCycleStatus().map(EndDeviceLifeCycleStatus::getState);
        }
        else {
            return Optional.empty();
        }
    }

    private Optional<EndDeviceLifeCycleStatus> findCurrentDeviceLifeCycleStatus() {
        Condition condition = where("endDevice").isEqualTo(this).and(where("interval").isEffective());
        List<EndDeviceLifeCycleStatus> candidates = this.getDataModel().mapper(EndDeviceLifeCycleStatus.class).select(condition);
        if (candidates.size() > 1) {
            throw new NotUniqueException("Expected only a single " + EndDeviceLifeCycleStatus.class.getSimpleName() + " to be effective now for end device " + this.getMRID());
        }
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        else {
            return Optional.of(candidates.get(0));
        }
    }

    @Override
    public Optional<State> getState(Instant instant) {
        if (this.isDeviceLifeCycleManaged()) {
            Condition condition = where("endDevice").isEqualTo(this).and(where("interval").isEffective(instant));
            List<EndDeviceLifeCycleStatus> candidates = this.getDataModel().mapper(EndDeviceLifeCycleStatus.class).select(condition);
            if (candidates.size() > 1) {
                throw new NotUniqueException("Expected only a single " + EndDeviceLifeCycleStatus.class.getSimpleName() + " to be effective at " + instant + " for end device " + this.getMRID());
            }
            if (candidates.isEmpty()) {
                return Optional.empty();
            }
            else {
                return Optional.of(candidates.get(0).getState());
            }
        }
        else {
            return Optional.empty();
        }
    }

    private boolean isDeviceLifeCycleManaged() {
        return this.stateMachine.isPresent();
    }

    public String getAmrId() {
		return amrId;
	}

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public EndDeviceEventRecord addEventRecord(EndDeviceEventType type, Instant date) {
        return deviceEventFactory.get().init(this, type, date);
    }

    @Override
    public List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range) {
        return dataModel.query(EndDeviceEventRecord.class).select(inRange(range),Order.ascending("createdDateTime"));
    }

    @Override
    public List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range, List<EndDeviceEventType> eventTypes) {
    	Condition condition = inRange(range).and(where("eventType").in(eventTypes));
        return dataModel.query(EndDeviceEventRecord.class).select(condition,Order.ascending("createdDateTime"));
    }

    @Override
    public List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter) {
		if (filter == null){
			return Collections.emptyList();
		}
        final String anyNumberPattern = "[0-9]{1,3}";
        StringBuilder regExp = new StringBuilder();
        regExp.append("^").append(anyNumberPattern).append("\\.");
        regExp.append(filter.domain != null ? filter.domain.getValue() : anyNumberPattern).append("\\.");
        regExp.append(filter.subDomain != null ? filter.subDomain.getValue() : anyNumberPattern).append("\\.");
        regExp.append(filter.eventOrAction != null ? filter.eventOrAction.getValue() : anyNumberPattern).append("$");

        Condition condition = inRange(filter.range).and(where("eventType.mRID").matches(regExp.toString(), "i"));
		if (filter.logBookId > 0){
			condition = condition.and(where("logBookId").isEqualTo(filter.logBookId));
		}
        return dataModel.query(EndDeviceEventRecord.class, EndDeviceEventType.class).select(condition, Order.descending("createdDateTime"));
    }

    @Override
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    private Condition inRange(Range<Instant> range) {
        return where("endDevice").isEqualTo(this).and(where("createdDateTime").in(range));
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public boolean equals(Object o) {
    	if (this == o) {
           return true;
        }
        if (o == null || getClass() != o.getClass()) {
        	return false;
        }
        return getId() == this.self.getClass().cast(o).getId();
    }

    @Override
    public int hashCode() {
    	return Objects.hashCode(id);
    }

    EventService getEventService() {
        return eventService;
    }

    /**
     * Defines the behavior of an internal component
     * that will manage the State of this EndDevice
     * and will be triggered from the save method.
     */
    private interface StateManager {
        /**
         * Save the State after the EndDevice was saved.
         *
         * @return The StateManager that will be responsible for managing the state from now on
         */
        StateManager save();
    }

    /**
     * Provides an implementation for the StateManager interface
     * when this EndDevice does not actually have a device life cycle
     * and therefore has not state to manage.
     */
    private class NoDeviceLifeCycle implements StateManager {
        @Override
        public StateManager save() {
            // Nothing to save and we keep the same manager
            return this;
        }
    }

    /**
     * Provides an implementation for the StateManager interface
     * that will set the initial {@link State} of the related
     * {@link FiniteStateMachine} after the EndDevice was saved.
     */
    private class CreateInitialState implements StateManager {
        @Override
        public StateManager save() {
            createNewState(getCreateTime(), getFiniteStateMachine().get().getInitialState());
            return new UpdateStateNotSupportedYet();
        }
    }

    /**
     * Provides an implementation for the StateManager interface
     * for EndDevices whose {@link State} is managed by a {@link FiniteStateMachine}
     * but updates to the State are not supported yet.
     */
    private class UpdateStateNotSupportedYet implements StateManager {
        @Override
        public StateManager save() {
            // Updates are not supported yet so there is nothing to save and we keep the same manager
            return this;
        }
    }

}
