/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventRecordBuilder;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface EndDevice extends HasId, IdentifiedObject {
    String TYPE_IDENTIFIER = "E";

    String getSerialNumber();

    String getUtcNumber();

    ElectronicAddress getElectronicAddress();

    AmrSystem getAmrSystem();

    String getAmrId();

    void update();

    Instant getCreateTime();

    Instant getModTime();

    long getVersion();

    void delete();

    Optional<HeadEndInterface> getHeadEndInterface();

    EndDeviceEventRecordBuilder addEventRecord(EndDeviceEventType type, Instant instant, long logBookId);

    List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range);

    List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range, List<EndDeviceEventType> eventTypes);

    long getDeviceEventsCountByFilter(EndDeviceEventRecordFilterSpecification filter);

    List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter);

    List<EndDeviceEventRecord> getDeviceEventsByReadTime(Range<Instant> range);

    void setSerialNumber(String serialNumber);

    void setLocation(Location location);

    Optional<Location> getLocation();

    Optional<SpatialCoordinates> getSpatialCoordinates();

    void setSpatialCoordinates(SpatialCoordinates spatialCoordinates);

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

    boolean isObsolete();

    /**
     * Makes this EndDevice obsolete.
     * This EndDevice will no longer show up in queries
     * except the one that is looking for an EndDevice by its database id.
     */
    void makeObsolete();

    /**
     * The Instant in time when this EndDevice was made obsolete.
     *
     * @return The instant in time or {@code Optional.empty()} if this EndDevice is not obsolete
     */
    Optional<Instant> getObsoleteTime();

    /**
     * Gets the manufacturer of this EndDevice
     *
     * @return the manufacturer
     */
    String getManufacturer();

    /**
     * Sets the manufacturer of this EndDevice
     *
     * @param manufacturer new value
     */
    void setManufacturer(String manufacturer);

    /**
     * Gets the model number of this EndDevice
     *
     * @return the model number as known by the manufacturer
     */
    String getModelNumber();

    /**
     * Sets the modelNumber of the device
     *
     * @param modelNumber as known by the manufacturer
     */
    void setModelNumber(String modelNumber);

    /**
     * Gets the model number of this EndDevice
     *
     * @return the model number as known by the manufacturer
     */
    String getModelVersion();

    /**
     * Sets the modelNumber of the device
     *
     * @param modelVersion as known by the manufacturer
     */
    void setModelVersion(String modelVersion);

    default List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter, Integer from, Integer to){
        return getDeviceEventsByFilter(filter);
    }

}
