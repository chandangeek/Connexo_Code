/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.DeviceTypePurpose;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.CommunicationErrorType;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides services that relate to the topology of {@link Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-04 (15:34)
 */
@ProviderType
public interface TopologyService {

    String COMPONENT_NAME = "DTL";

    Optional<Device> getPhysicalGateway(Device slave);

    Optional<Device> getPhysicalGateway(Device slave, Instant when);

    Map<Device, Device> getPhysicalGateways(List<Device> deviceList);

    /**
     * Sets the physical gateway of the slave {@link Device} to the specified Device.
     *
     * @param slave The slave Device
     * @param gateway The physical gateway Device
     */
    void setPhysicalGateway(Device slave, Device gateway);

    /**
     * Clears the physical gateway of the slave {@link Device},
     * i.e. removes to physical gateway on the specified Device.
     * We use the current timestamp to remove the gateway, but round it down to the previous minute.
     *
     * @param slave The slave Device
     */
    void clearPhysicalGateway(Device slave);

    /**
     * Clears the old communication segment of the gateway{@link Device},
     * i.e. removes all communication paths that has as source the specified device.
     * We use the current timestamp to remove the communication path, but round it down to the previous minute.
     *
     * @param gateway, the source device
     */
    void clearOldCommunicationPathSegments(Device gateway, Instant now);

    /**
     * Finds the {@link Device}s that are physically connected to the specified Device.
     *
     * @param device the 'master' device
     * @return The List of physically connected devices
     */
    List<Device> findPhysicalConnectedDevices(Device device);

    /**
     * Gets the {@link DeviceTopology physical topology} for this Device
     * during the specified Interval, organized (or sorted) along the timeline.
     *
     * @param root The Device for which the physical topology should be built
     * @param period The period in time during which the devices were physically linked to this Device
     * @return The DeviceTopology
     */
    DeviceTopology getPhysicalTopology(Device root, Range<Instant> period);

    /**
     * Gets the {@link TopologyTimeline} of the devices that are
     * directly physically connected to the specified gateway.
     *
     * @param device The gateway
     * @return The TopologyTimeline
     * @see #getPhysicalGateway(Device)
     */
    TopologyTimeline getPhysicalTopologyTimeline(Device device);

    /**
     * Gets the {@link PhysicalGatewayReference} for a specified gateway for a certain range
     *
     * @param device The gateway
     * @param range The range
     * @return a list of PhysicalGataweyReference
     */
    List<PhysicalGatewayReference> getPhysicalGatewayReferencesFor(Device device, Range<Instant> range);

    /**
     * Gets the most recent additions to the {@link TopologyTimeline}
     * of the devices that are directly physically connected to the specified gateway.
     *
     * @param device The gateway
     * @param maximumNumberOfAdditions The maximum number of additions to the timeline
     * @return The TopologyTimeline
     * @see #getPhysicalGateway(Device)
     */
    TopologyTimeline getPhysicalTopologyTimelineAdditions(Device device, int maximumNumberOfAdditions);

    /**
     * Gets the {@link G3CommunicationPath} that was used to communication
     * from the source to the target {@link Device}.
     *
     * @param source The source Device
     * @param target The target Device
     * @return The G3CommunicationPath
     */
    G3CommunicationPath getCommunicationPath(Device source, Device target);


    /**
     * Starts the process to add {@link G3CommunicationPathSegment}s
     * from the source to multiple target {@link Device}s.
     *
     * @return The G3CommunicationPathSegmentBuilder
     */
    G3CommunicationPathSegmentBuilder addCommunicationSegments();

    /**
     * Return the current stream of communication path segments to all gateway slaves
     *
     * @param gateway to get the segments to its slaves
     * @return a Stream of segments
     */
    Stream<G3CommunicationPathSegment> getUniqueG3CommunicationPathSegments(Device gateway);

    /**
     * Counts the number of communication errors that have occurred in the specified
     * {@link Interval} within the topology that starts from the speified Device.
     *
     * @param interval The Interval during which the communication errors have occurred
     * @return The number of communication errors
     */
    int countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType errorType, Device device, Interval interval);

    /**
     * Gets the {@link Device}'s {@link Channel}s AND the Channels of all
     * <i>physical</i> slave devices belonging to {@link LoadProfile}s of the same type.
     *
     * @return a <CODE>List</CODE> of <CODE>Channel</CODE> objects
     */
    List<Channel> getAllChannels(LoadProfile loadProfile);

    /**
     * Finds all {@link ConnectionTask}s across the full topology of the specified Device.
     * All ConnectionTasks of the device itself <b>and</b> of the gateway level
     * (if the Device has a gateway of course) will be included.
     *
     * @param device The Device for which we need to search all ConnectionTasks of the full topology
     * @return The connection tasks for the Device
     */
    List<ConnectionTask<?, ?>> findAllConnectionTasksForTopology(Device device);

    /**
     * Finds the default {@link ConnectionTask} for the specified Device.
     * The search will start at the Device but if none is found there,
     * it will continue to the gateway level (if the Device has a gateway of course).
     *
     * @param device The Device for which we need to search the default ConnectionTask
     * @return The default ConnectionTask for the given Device if one exists
     */
    Optional<ConnectionTask> findDefaultConnectionTaskForTopology(Device device);

    /**
     * Finds the {@link ConnectionTask} for the specified Device that has the given ConnectionFunction.
     * The search will start at the Device but if none is found there,
     * it will continue to the gateway level (if the Device has a gateway of course).
     *
     * @param device The Device for which we need to search the specific ConnectionTask
     * @return The ConnectionTask having the specified ConnectionFunction for the given Device if one exists
     */
    Optional<ConnectionTask> findConnectionTaskWithConnectionFunctionForTopology(Device device, ConnectionFunction connectionFunction);

    /**
     * Starts the building process of the specified {@link Device}'s neighborhood,
     * i.e. all the Devices that appear as neighboring devices
     * for the G3/PLC communication technology.
     *
     * @param device The Device whose neighborhood will be built
     * @return The G3NeighborhoodBuilder
     */
    G3NeighborhoodBuilder buildG3Neighborhood(Device device);

    /**
     * Find the neighboring {@link Device}s,
     * for the G3/PLC communication technology.
     *
     * @param device The Device whose neighborhood will be returned
     * @return The List of Device
     */
    List<Device> findDevicesInG3Neighborhood(Device device);

    /**
     * Find the {@link G3Neighbor}s of the specified {@link Device},
     * for the G3/PLC communication technology.
     *
     * @param device The Device whose neighbors will be returned
     * @return The List of G3Neighbor
     */
    List<G3Neighbor> findG3Neighbors(Device device);

    /**
     * Find the neighboring {@link Device}s,
     * for the G3/PLC communication technology.
     *
     * @param device The Device whose neighborhood will be returned
     * @param when The timestamp on which the Devices were effectively in the Device's neighborhood
     * @return The List of Device
     */
    List<Device> findDevicesInG3Neighborhood(Device device, Instant when);

    /**
     * Find the {@link G3Neighbor}s of the specified {@link Device},
     * for the G3/PLC communication technology.
     *
     * @param device The Device whose neighbors will be returned
     * @param when The timestamp on which the G3Neighbor were effectively part of the Device's neighborhood
     * @return The List of G3Neighbor
     */
    List<G3Neighbor> findG3Neighbors(Device device, Instant when);

    /**
     * Gets the current {@link G3DeviceAddressInformation} for the specified {@link Device}.
     *
     * @param device The Device
     * @return The G3DeviceAddressInformation
     */
    Optional<G3DeviceAddressInformation> getG3DeviceAddressInformation(Device device);

    /**
     * Gets the {@link G3DeviceAddressInformation} for the specified {@link Device}
     * that was effective on the specified timestamp.
     *
     * @param device The Device
     * @param when The timestamp on which the G3DeviceAddressInformation should be effective
     * @return The G3DeviceAddressInformation
     */
    Optional<G3DeviceAddressInformation> getG3DeviceAddressInformation(Device device, Instant when);

    /**
     * Sets the {@link G3DeviceAddressInformation} for the specified {@link Device}.
     * Overrules the G3DeviceAddressInformation that is currently effective unless
     * the information has not changed, in which case the current information
     * remains effective.
     *
     * @param device The Device
     * @param ipv6Address The IPv6 address
     * @param ipv6ShortAddress The short version of the IPv6 address
     * @param logicalDeviceId The logical device identifier
     * @return The newly created G3DeviceAddressInformation or the existing one if nothing was changed
     */
    G3DeviceAddressInformation setG3DeviceAddressInformation(Device device, String ipv6Address, int ipv6ShortAddress, int logicalDeviceId);

    /**
     * Link the slave device to the data logger device
     *
     * @param datalogger which can have slave devices
     * @param slave device to link to the datalogger
     * @param linkingDate datetime at which the logger-slave relation becomes effective
     * @param slaveDataLoggerChannelMap mapping of data logger (mdc) channels to slave (mdc) channels
     * @param slaveDataLoggerRegisterMap mapping of data logger registers to slave registers
     * <p>
     * The datalogger's {@link DeviceType} purpose must be REGULAR{@link DeviceTypePurpose} and its actual
     * {@link DeviceConfiguration} must be set as datalogger enabled.
     * The slave device's DeviceType must have the DATALOGGER_SLAVE {@link DeviceTypePurpose}
     * <p>
     * Technically the link is persisted as a {@link PhysicalGatewayReference} object.
     * This PhysicalGateWayReference holds a List of DataLoggerChannelUsage linking the slave (pulse) channel to the datalogger (pulse) channel
     */
    void setDataLogger(Device slave, Device datalogger, Instant linkingDate, Map<Channel, Channel> slaveDataLoggerChannelMap, Map<Register, Register> slaveDataLoggerRegisterMap);

    /**
     * Unlink the slave device from its data logger device
     *
     * @param slave to remove from its logger device;
     */
    void clearDataLogger(Device slave, Instant when);

    /**
     * Finds the {@link Device}s that are data logger slave devices using to the specified data logger Device.
     *
     * @param dataLogger the 'data logger' device
     * @return The List of data logger slaves using the data logger device
     */
    List<Device> findDataLoggerSlaves(Device dataLogger);

    /**
     * Creates a new G3 neighbor link
     * @param device the fist device in the link
     * @param neighbor the second device in the link
     * @param modulationScheme modulation scheme of the link
     * @param modulation modulation of the link
     * @param phaseInfo phase info shift of the link
     * @param g3NodeState state of the link
     * @return a G3 object with the information above
     */
    G3Neighbor newG3Neighbor(Device device, Device neighbor, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState);

    /**
     * Creates a new G3 links from an existing link, but reversing device<->neighbor.
     * This is handy because in DB the links are persisted in one way, while in GUI and DLMS they are the other way around.
     *
     * @param original the existing G3 link
     * @return clone of the original with the reversed device<->neighbor
     */
    G3Neighbor reverseCloneG3Neighbor(G3Neighbor original);

    /**
     * @return true if the device is a data logger slave device which is not linked to a data logger
     */
    boolean isDataLoggerSlaveCandidate(Device device);

    /**
     * Returns the data logger device the slave is linked with at given time
     *
     * @param slave for which to retrieve its data logger
     * @param when time at which the link is effective
     * @return the data logger device
     */
    Optional<Device> getDataLogger(Device slave, Instant when);


    /**
     * Finds the dataloggerReference which is effective at the given timestamp.
     * If no reference was active, an empty optional will be returned
     *
     * @param dataloggerSlaveDevice the datalogger slave device which has potentially a dataloggerReference for the given timestamp
     * @param effective the timeStamp at which you want the reference
     * @return the datalogger reference which was active at the given timestamp
     */
    Optional<DataLoggerReference> findDataloggerReference(Device dataloggerSlaveDevice, Instant effective);

    /**
     * Finds the last dataloggerReference for the given dataloggerSlaveDevice
     *
     * @param dataloggerSlaveDevice the datalogger slave device which was potentially linked to a datalogger
     * @return the last reference to a datalogger for the given slave, if any
     */
    Optional<DataLoggerReference> findLastDataloggerReference(Device dataloggerSlaveDevice);

    /**
     * @return all data logger data slave devices which at this moment are effectively linked to a datalogger
     */
    Finder<? extends DataLoggerReference> findAllEffectiveDataLoggerSlaveDevices();

    /**
     * @param dataLoggerChannel the channel of the datalogger
     * @return an Optional channel of the slave device to which the data logger channel is linked now. Optional<empty> if the $
     * dataLogger channel is not linked
     */
    Optional<Channel> getSlaveChannel(Channel dataLoggerChannel);

    /**
     * @param dataLoggerChannel the channel of the datalogger
     * @param when Time at which the link should be effective
     * @return an Optional channel of the slave device to which the data logger channel is linked. Optional<empty> if the $
     * dataLogger channel is not linked
     */
    Optional<Channel> getSlaveChannel(Channel dataLoggerChannel, Instant when);

    /**
     * Checks whether a (datalogger Channel) is referenced
     *
     * @param dataLoggerChannel channel to inspect
     * @return true if the channel is already present as gateway channel in one or more DataLoggerChannelUsages
     * false if not DataloggerChannelUsages were found having the given ('pulse') channel as gateway channel;
     */
    boolean isReferenced(Channel dataLoggerChannel);

    /**
     * Returns the date for which the (data logger) channel is free to be used to link with any slave channel
     *
     * @param dataLoggerChannel to check the 'availability' of
     * @return (Optional.of) time at which the data logger channel is free to use (is available for linking). When the data logger channel was never linked (Optional.of) Instant.EPOCH is returned.
     * If the data logger channel is linked yet Optional.empty() will be returned
     */
    Optional<Instant> availabilityDate(Channel dataLoggerChannel);

    /**
     * Returns the date for which the (data logger) channel is free to be used to link with any slave channel
     *
     * @param dataLoggerRegister to check the 'availability' of
     * @return (Optional.of) time at which the data logger channel is free to use (is available for linking). When the data logger channel was never linked (Optional.of) Instant.EPOCH is returned.
     * If the data logger channel is linked yet Optional.empty() will be returned
     */
    Optional<Instant> availabilityDate(Register dataLoggerRegister);

    /**
     * Retrieve all DataLoggerChannelUsages for given dataLoggerChannel for given period
     *
     * @param dataLoggerChannel channel to inspect
     * @param referencePeriod period to inspect
     * @return a list of DataLoggerChannelUsages for the given channel
     */
    List<DataLoggerChannelUsage> findDataLoggerChannelUsagesForChannels(Channel dataLoggerChannel, Range<Instant> referencePeriod);

    /**
     * Retrieve all DataLoggerChannelUsages for given dataLoggerRegister for given period
     *
     * @param dataLoggerRegister register to inspect
     * @param referencePeriod period to inspect
     * @return a list of DataLoggerChannelUsages for the given register
     */
    List<DataLoggerChannelUsage> findDataLoggerChannelUsagesForRegisters(Register<?, ?> dataLoggerRegister, Range<Instant> referencePeriod);

    /**
     * @param dataLoggerRegister the register of the datalogger
     * @param when Time at which the link should be effective
     * @return an Optional channel of the slave device to which the data logger register is linked. Optional<empty> if the $
     * dataLogger register is not linked
     */
    Optional<Register> getSlaveRegister(Register dataLoggerRegister, Instant when);

    /**
     * Checks whether a (datalogger Register) is referenced
     *
     * @param dataLoggerRegister register to inspect
     * @return true if the channel is already present as gateway channel in one or more DataLoggerChannelUsages
     * false if no DataloggerChannelUsages were found having the given register (pulse channel) as gateway channel;
     */
    boolean isReferenced(Register dataLoggerRegister);

    /**
     * Provides an ordered list which contains pairs of channels (of different devices) vs ranges in which they should contain data
     * for the requested range.
     *
     * @param channel the Datalogger channel
     * @param range the range in which we want to collect the data
     * @return the requested list, ordered descending according to interval.start
     */
    List<Pair<Channel, Range<Instant>>> getDataLoggerChannelTimeLine(Channel channel, Range<Instant> range);

    List<Pair<Register, Range<Instant>>> getDataLoggerRegisterTimeLine(Register register, Range<Instant> intervalReg);

    /**
     * Returns the x last physical gateways of a certain device
     *
     * @param slave the device to find the gateways for
     * @param numberOfDevices number of gateways that we need
     * @return a Stream of physical gateway references
     */
    Stream<PhysicalGatewayReference> getLastPhysicalGateways(Device slave, int numberOfDevices);

    List<Device> getSlaveDevices(Device device);

    List<G3Neighbor> getSlaveDevices(Device gateway, long pageStart);

    interface G3CommunicationPathSegmentBuilder {

        /**
         * Adds a {@link G3CommunicationPathSegment} from the source to the target
         * {@link Device}, using the specified intermediate Device.
         * <strike>The source Device is the one that was specified in the
         * {@link #addCommunicationSegments(Device)} method
         * that returned this Builder in the first place.</strike>
         * It is allowed that the intermediate Device is null or
         * the same as the  target Device, in that case,
         * the added segment will be a direct or final segment.
         *
         * @param source The source Device
         * @param target The target Device
         * @param intermediateHop The intermediate Device
         * @param timeToLive The time to live
         * @param cost The segment's cost
         */
        G3CommunicationPathSegmentBuilder add(Device source, Device target, Device intermediateHop, Duration timeToLive, int cost);

        /**
         * Completes the building process, effectively creating all segments
         * and making sure they all have the same effective timestamp.
         *
         * @return The newly created segments
         */
        List<G3CommunicationPathSegment> complete();

    }

    /**
     * Build all the neighbors of one {@link Device}.
     * Device's whose neighborhood has been built before can be updated
     * with the same builder. Devices that were not revisited, i.e.
     * the {@link #addNeighbor(Device, ModulationScheme, Modulation, PhaseInfo, G3NodeState)}
     * was not called will be deleted upon completion.
     */
    interface G3NeighborhoodBuilder {

        /**
         * Adds the specified {@link Device} to the neighborhood
         * that is currently being built. When the neighborhood
         * of the Device is actually being updated, Devices that
         * were already part of the neighborhood before must be added
         * again or they will be deleted upon completion.
         *
         * @param neighbor The new neighbor
         * @param modulationScheme The ModulationScheme
         * @param modulation The Modulation
         * @param phaseInfo The PhaseInfo
         * @param g3NodeState The G3NodeState
         * @return The G3NeighborBuilder that allows you to specify the optional neighboring information
         */
        G3NeighborBuilder addNeighbor(Device neighbor, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState);

        /**
         * Completes the building process and returns
         * the {@link G3Neighbor}s that were added.
         * Note that this builder is now completely useless
         * and will throw IllegalStateExceptions when
         * attempting to complete again.
         */
        List<G3Neighbor> complete();
    }

    Subquery IsLinkedToMaster(Device device);

    interface G3NeighborBuilder {
        G3NeighborBuilder txGain(int txGain);

        G3NeighborBuilder txResolution(int txResolution);

        G3NeighborBuilder txCoefficient(int txCoefficient);

        G3NeighborBuilder linkQualityIndicator(int linkQualityIndicator);

        G3NeighborBuilder timeToLiveSeconds(int seconds);

        G3NeighborBuilder toneMap(long toneMap);

        G3NeighborBuilder toneMapTimeToLiveSeconds(int seconds);

        G3NeighborBuilder macPANId(long macPANId);

        G3NeighborBuilder nodeAddress(String nodeAddress);

        G3NeighborBuilder shortAddress(int shortAddress);

        G3NeighborBuilder lastUpdate(Instant lastUpdate);

        G3NeighborBuilder lastPathRequest(Instant lastPathRequest);

        G3NeighborBuilder roundTrip(long roundTrip);

        G3NeighborBuilder linkCost(int linkCost);
    }

}