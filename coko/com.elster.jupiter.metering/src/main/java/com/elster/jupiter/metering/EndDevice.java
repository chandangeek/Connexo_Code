package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EndDevice extends IdentifiedObject {
	String TYPE_IDENTIFIER = "E";
	long getId();
	String getSerialNumber();
	String getUtcNumber();
	ElectronicAddress getElectronicAddress();
	AmrSystem getAmrSystem();
	String getAmrId();
	void save();
    Instant getCreateTime();
    Instant getModTime();
    long getVersion();
    void delete();

    EndDeviceEventRecord addEventRecord(EndDeviceEventType type, Instant instant);

    List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range);
    List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range, List<EndDeviceEventType> eventTypes);
    List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter);

    void setSerialNumber(String serialNumber);

    /**
     * Gets the {@link FiniteStateMachine} that determines the possible {@link State}s.
     *
     * @return The FiniteStateMachine
     */
    Optional<FiniteStateMachine> getFiniteStateMachine();

    /**
     * Gets the current {@link State} of this EndDevice
     * or an empty Optional when device life cycle management
     * is not enabled for this EndDevice.
     *
     * @return The current State
     */
    Optional<State> getState();

    /**
     * Gets the {@link State} of this EndDevice as it was
     * known at the specified point in time.
     * May return an empty optional when the point in time
     * is before the creation time of this EndDevice.
     * Always return an empty optional when device life cycle
     * management is not enabled for this EndDevice.
     *
     * @param instant The point in time
     * @return The State
     */
    Optional<State> getState(Instant instant);

    /**
     * Gets the {@link StateTimeline} for this EndDevice.
     *
     * @return The StateTimeline or empty when the life cycle of this EndDevice is not managed
     */
    Optional<StateTimeline> getStateTimeline();

    LifecycleDates getLifecycleDates();

    void setName(String name);
}
