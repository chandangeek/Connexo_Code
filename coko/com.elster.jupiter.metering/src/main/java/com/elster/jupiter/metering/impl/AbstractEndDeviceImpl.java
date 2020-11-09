/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.EndDeviceLifeCycleStatus;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventRecordBuilder;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.associations.Temporals;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.geo.SpatialCoordinates;
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
import java.util.UUID;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

abstract class AbstractEndDeviceImpl<S extends AbstractEndDeviceImpl<S>> implements ServerEndDevice {
    static final Map<String, Class<? extends EndDevice>> IMPLEMENTERS = ImmutableMap.of(EndDevice.TYPE_IDENTIFIER, EndDeviceImpl.class, Meter.TYPE_IDENTIFIER, MeterImpl.class);
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
    private Instant manufacturedDate;
    private Instant purchasedDate;
    private Instant receivedDate;
    private Instant installedDate;
    private Instant removedDate;
    private Instant retiredDate;
    private Instant createTime;
    private Instant obsoleteTime;
    private Instant modTime;
    private SpatialCoordinates spatialCoordinates;
    private String manufacturer;
    private String modelNbr;
    private String modelVersion;

    private static final String EVENT_TYPE_OTHER = "0.0.0.0";

    @SuppressWarnings("unused")
    private String userName;

    // associations
    private AmrSystem amrSystem;
    private Reference<FiniteStateMachine> stateMachine = ValueReference.absent();
    private TemporalReference<EndDeviceLifeCycleStatus> status = Temporals.absent();
    private StateManager stateManager = new NoDeviceLifeCycle();
    private final Reference<Location> location = ValueReference.absent();


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

    S init(AmrSystem system, String amrId, String name, UUID mRID) {
        this.amrSystemId = system.getId();
        this.amrSystem = system;
        this.amrId = amrId;
        this.name = name;
        this.mRID = (mRID == null ? UUID.randomUUID() : mRID).toString();
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
        return name;
    }

    @Override
    public void update() {
        doSave();
    }

    void doSave() {
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
    public Optional<HeadEndInterface> getHeadEndInterface() {
        return dataModel.getInstance(MeteringService.class).getHeadEndInterface(getAmrSystem().getName());
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
    public void changeState(State newState, Instant effective) {
        this.validateState(newState);
        this.closeCurrentState(effective);
        this.createNewState(effective, newState);
        this.touch();
    }

    @Override
    public void changeStateMachine(FiniteStateMachine newStateMachine, Instant effective) {
        String stateName = this.getState().orElseThrow(this::unmanagedStateException).getName();
        this.stateMachine.set(newStateMachine);
        this.closeCurrentState(effective);
        this.createNewState(
                effective,
                newStateMachine
                        .getState(stateName)
                        .orElseThrow(() -> new StateNoLongerExistsException(stateName)));
        this.dataModel.update(this, "stateMachine");
    }

    public void touch() {
        if (this.id != 0) {
            this.dataModel.touch(this);
        }
    }

    private void validateState(State state) {
        if (this.isDeviceLifeCycleManaged()) {
            if (state.getFiniteStateMachine().getId() != this.getFiniteStateMachine().get().getId()) {
                throw new IllegalArgumentException("Changing the state of an EndDevice to a state that is not part of its state machine is not allowed");
            }
        } else {
            throw unmanagedStateException();
        }
    }

    private UnsupportedOperationException unmanagedStateException() {
        return new UnsupportedOperationException("Changing the state of an EndDevice whose state is not managed is not possible");
    }

    private void closeCurrentState(Instant now) {
        EndDeviceLifeCycleStatus status = this.status.effective(now).get();
        status.close(now);
        this.dataModel.update(status);
    }

    private void createNewState(Instant effective, State state) {
        Interval stateEffectivityInterval = Interval.of(Range.atLeast(effective));
        EndDeviceLifeCycleStatusImpl deviceLifeCycleStatus = this.dataModel
                .getInstance(EndDeviceLifeCycleStatusImpl.class)
                .initialize(stateEffectivityInterval, this, state);
        this.status.add(deviceLifeCycleStatus);
    }

    @Override
    public Optional<State> getState() {
        if (this.isDeviceLifeCycleManaged()) {
            return this.status.effective(this.clock.instant()).map(EndDeviceLifeCycleStatus::getState);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<State> getState(Instant instant) {
        if (this.isDeviceLifeCycleManaged()) {
            return this.status.effective(instant).map(EndDeviceLifeCycleStatus::getState);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<StateTimeline> getStateTimeline() {
        if (this.isDeviceLifeCycleManaged()) {
            return Optional.of(
                    StateTimelineImpl.from(
                            this.status
                                    .effective(Range.atLeast(Instant.EPOCH))
                                    .stream()
                                    .map(StateTimeSliceImpl::from)
                                    .collect(Collectors.toList())));
        } else {
            return Optional.empty();
        }
    }

    private boolean isDeviceLifeCycleManaged() {
        return this.stateMachine.isPresent();
    }

    @Override
    public LifecycleDates getLifecycleDates() {
        return new LifecycleDatesImpl();
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
    public EndDeviceEventRecordBuilder addEventRecord(EndDeviceEventType type, Instant date) {
        return new EndDeviceEventRecordBuilderImpl(deviceEventFactory, this, type, date);
    }

    @Override
    public List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range) {
        return dataModel.query(EndDeviceEventRecord.class).select(inRange(range), Order.ascending("createdDateTime"));
    }

    @Override
    public List<EndDeviceEventRecord> getDeviceEventsByReadTime(Range<Instant> range) {
        return dataModel.query(EndDeviceEventRecord.class).select(createdInRange(range), Order.ascending("createdDateTime"));
    }

    @Override
    public List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range, List<EndDeviceEventType> eventTypes) {
        Condition condition = inRange(range).and(where("eventType").in(eventTypes));
        return dataModel.query(EndDeviceEventRecord.class).select(condition, Order.ascending("createdDateTime"));
    }

    @Override
    public long getDeviceEventsCountByFilter(EndDeviceEventRecordFilterSpecification filter) {
        if (filter == null) {
            return 0;
        }
        return dataModel.query(EndDeviceEventRecord.class, EndDeviceEventType.class).count(filterToCondition(filter));
    }

    @Override
    public List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter) {
        return getDeviceEventsByFilter(filter, null, null);
    }

    @Override
    public List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter, Integer from, Integer to) {
        if (filter == null) {
            return Collections.emptyList();
        }
        if (from == null || to == null) {
            return dataModel.query(EndDeviceEventRecord.class, EndDeviceEventType.class).select(filterToCondition(filter), Order.descending("createdDateTime"));
        }
        return dataModel.query(EndDeviceEventRecord.class, EndDeviceEventType.class).select(filterToCondition(filter), new Order[]{Order.descending("createdDateTime")}, true, new String[0], from, to);
    }

    private Condition filterToCondition(EndDeviceEventRecordFilterSpecification filter) {
        final String anyNumberPattern = "[0-9]{1,3}";
        String regExp = "^" + anyNumberPattern + "\\." +
                (filter.domain != null ? filter.domain.getValue() : anyNumberPattern) + "\\." +
                (filter.subDomain != null ? filter.subDomain.getValue() : anyNumberPattern) + "\\." +
                (filter.eventOrAction != null ? filter.eventOrAction.getValue() : anyNumberPattern) + "$";

        Condition condition = inRange(filter.range).and(where("eventType.mRID").matches(regExp, "i"));
        if (filter.logBookId > 0) {
            condition = condition.and(where("logBookId").isEqualTo(filter.logBookId));
        }
        Condition eventTypesCondition = Condition.FALSE;
        boolean appendEventTypesCondition = false;
        for (String mrid : filter.eventCodes) {
            if (!EVENT_TYPE_OTHER.equals(mrid)) {
                eventTypesCondition = eventTypesCondition.or(where("eventType.mRID").matches(mrid, ""));
                appendEventTypesCondition = true;
            }
        }
        if (!filter.deviceEventCodes.isEmpty()) {
            eventTypesCondition = eventTypesCondition.or(where("eventType.mRID").isEqualTo(EVENT_TYPE_OTHER).and(where("deviceEventType").in(filter.deviceEventCodes)));
            appendEventTypesCondition = true;
        }
        return appendEventTypesCondition ? condition.and(eventTypesCondition) : condition;
    }

    @Override
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    private Condition inRange(Range<Instant> range) {
        return where("endDevice").isEqualTo(this).and(where("createdDateTime").in(range));
    }

    private Condition createdInRange(Range<Instant> range) {
        return where("endDevice").isEqualTo(this).and(where("createTime").in(range));
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

    @Override
    public boolean isObsolete() {
        return this.obsoleteTime != null;
    }

    @Override
    public void makeObsolete() {
        this.obsoleteTime = this.clock.instant();
        this.dataModel.update(this, "obsoleteTime");
        eventService.postEvent(EventType.METER_DELETED.topic(), this);
    }

    @Override
    public Optional<Instant> getObsoleteTime() {
        return Optional.ofNullable(this.obsoleteTime);
    }

    @Override
    public String getManufacturer() {
        return manufacturer == null ? "" : manufacturer;
    }

    @Override
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    @Override
    public String getModelNumber() {
        return modelNbr == null ? "" : modelNbr;
    }

    @Override
    public void setModelNumber(String modelNumber) {
        this.modelNbr = modelNumber;
    }

    @Override
    public String getModelVersion() {
        return modelVersion == null ? "" : modelVersion;
    }

    @Override
    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
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
            createNewState(getInitialStateStartTime(), getFiniteStateMachine().get().getInitialState());
            return new UpdateStateNotSupportedYet();
        }

        /**
         * If we create device with shipment date in the past -> use the shipment date,
         * If shipment date is in the future -> use creation time, otherwise we will introduce case when device exists without any state
         */
        private Instant getInitialStateStartTime() {
            if (AbstractEndDeviceImpl.this.receivedDate != null &&
                    !AbstractEndDeviceImpl.this.receivedDate.isAfter(AbstractEndDeviceImpl.this.clock.instant())) {
                return AbstractEndDeviceImpl.this.receivedDate;
            }
            return getCreateTime();
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

    private class LifecycleDatesImpl implements LifecycleDates {
        @Override
        public Optional<Instant> getManufacturedDate() {
            return Optional.ofNullable(manufacturedDate);
        }

        @Override
        public void setManufacturedDate(Instant manufacturedDate) {
            AbstractEndDeviceImpl.this.manufacturedDate = manufacturedDate;
        }

        @Override
        public Optional<Instant> getPurchasedDate() {
            return Optional.ofNullable(purchasedDate);
        }

        @Override
        public void setPurchasedDate(Instant purchasedDate) {
            AbstractEndDeviceImpl.this.purchasedDate = purchasedDate;
        }

        @Override
        public Optional<Instant> getReceivedDate() {
            return Optional.ofNullable(receivedDate);
        }

        @Override
        public void setReceivedDate(Instant receivedDate) {
            AbstractEndDeviceImpl.this.receivedDate = receivedDate;
        }

        @Override
        public Optional<Instant> getInstalledDate() {
            return Optional.ofNullable(installedDate);
        }

        @Override
        public void setInstalledDate(Instant installedDate) {
            AbstractEndDeviceImpl.this.installedDate = installedDate;
        }

        @Override
        public Optional<Instant> getRemovedDate() {
            return Optional.ofNullable(removedDate);
        }

        @Override
        public void setRemovedDate(Instant removedDate) {
            AbstractEndDeviceImpl.this.removedDate = removedDate;
        }

        @Override
        public Optional<Instant> getRetiredDate() {
            return Optional.ofNullable(retiredDate);
        }

        @Override
        public void setRetiredDate(Instant retiredDate) {
            AbstractEndDeviceImpl.this.retiredDate = retiredDate;
        }
    }

    @Override
    public void setLocation(Location location) {
        this.location.set(location);
    }

    @Override
    public Optional<Location> getLocation() {
        return location.getOptional();
    }

    @Override
    public Optional<SpatialCoordinates> getSpatialCoordinates() {
        return spatialCoordinates == null ? Optional.empty() : Optional.of(spatialCoordinates);
    }

    @Override
    public void setSpatialCoordinates(SpatialCoordinates spatialCoordinates) {
        this.spatialCoordinates = spatialCoordinates;
    }


    /**
     * Models the exceptional situation that occurs when an attempt
     * is made to switch an EndDevice to a new {@link FiniteStateMachine}
     * with the {@link State} in the new FiniteStateMachine
     * with the same name as the current State does not exist.
     * Since this was validated before, it must mean
     * that the new FiniteStateMachine was modified since
     * and that State was removed.
     */
    public static class StateNoLongerExistsException extends RuntimeException {
        private final String stateName;

        public StateNoLongerExistsException(String stateName) {
            super();
            this.stateName = stateName;
        }

        public String getStateName() {
            return stateName;
        }
    }

}
