package com.energyict.mdc.device.topology;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.CommunicationErrorType;
import com.google.common.collect.Range;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Provides services that relate to the topology of {@link Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-04 (15:34)
 */
@ProviderType
public interface TopologyService {

    public static final String COMPONENT_NAME = "DTL";

    public Optional<Device> getPhysicalGateway(Device slave);

    public Optional<Device> getPhysicalGateway(Device slave, Instant when);

    /**
     * Sets the physical gateway of the slave {@link Device} to the specified Device.
     *
     * @param slave The slave Device
     * @param gateway The physical gateway Device
     */
    public void setPhysicalGateway(Device slave, Device gateway);

    /**
     * Clears the physical gateway of the slave {@link Device},
     * i.e. removes to physical gateway on the specified Device.
     *
     * @param slave The slave Device
     */
    public void clearPhysicalGateway(Device slave);


    /**
     * Finds the {@link Device}s that are physically connected to the specified Device.
     *
     * @param device the 'master' device
     * @return The List of physically connected devices
     */
    public List<Device> findPhysicalConnectedDevices(Device device);

    /**
     * Gets the {@link DeviceTopology physical topology} for this Device
     * during the specified Interval, organized (or sorted) along the timeline.
     *
     * @param root The Device for which the physical topology should be built
     * @param period The period in time during which the devices were physically linked to this Device
     * @return The DeviceTopology
     */
    public DeviceTopology getPhysicalTopology(Device root, Range<Instant> period);

    /**
     * Gets the {@link TopologyTimeline} of the devices that are
     * directly physically connected to the specified gateway.
     *
     * @param device The gateway
     * @return The TopologyTimeline
     * @see #getPhysicalGateway(Device)
     */
    public TopologyTimeline getPysicalTopologyTimeline(Device device);

    /**
     * Gets the most recent additions to the {@link TopologyTimeline}
     * of the devices that are directly physically connected to the specified gateway.
     *
     * @param device The gateway
     * @param maximumNumberOfAdditions The maximum number of additions to the timeline
     * @return The TopologyTimeline
     * @see #getPhysicalGateway(Device)
     */
    public TopologyTimeline getPhysicalTopologyTimelineAdditions(Device device, int maximumNumberOfAdditions);

    /**
     * Gets the {@link G3CommunicationPath} that was used to communication
     * from the source to the target {@link Device}.
     *
     * @param source The source Device
     * @param target The target Device
     * @return The G3CommunicationPath
     */
    public G3CommunicationPath getCommunicationPath(Device source, Device target);

    /**
     * Starts the process to add {@link G3CommunicationPathSegment}s
     * from the source to multiple target {@link Device}s.
     *
     * @param source The source Device
     * @return The G3CommunicationPathSegmentBuilder
     */
    public G3CommunicationPathSegmentBuilder addCommunicationSegments(Device source);

    /**
     * Counts the number of communication errors that have occurred in the specified
     * {@link Interval} within the topology that starts from the speified Device.
     *
     * @param interval The Interval during which the communication errors have occurred
     * @return The number of communication errors
     */
    public int countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType errorType, Device device, Interval interval);

    /**
     * Gets the {@link Device}'s {@link Channel}s AND the Channels of all
     * <i>physical</i> slave devices belonging to {@link LoadProfile}s of the same type.
     *
     * @return a <CODE>List</CODE> of <CODE>Channel</CODE> objects
     */
    public List<Channel> getAllChannels(LoadProfile loadProfile);

    /**
     * Finds the default {@link ConnectionTask} for the specified Device.
     * The search will start at the Device but if none is found there,
     * it will continue to the gateway level (if the Device has a gateway of course).
     *
     * @param device The Device for which we need to search the default ConnectionTask
     * @return The default ConnectionTask for the given Device if one exists
     */
    public Optional<ConnectionTask> findDefaultConnectionTaskForTopology(Device device);

    /**
     * Starts the building process of the specified {@link Device}'s neighborhood,
     * i.e. all the Devices that appear as neighboring devices
     * for the G3/PLC communication technology.
     *
     * @param device The Device whose neighborhood will be built
     * @return The G3NeighborhoodBuilder
     */
    public G3NeighborhoodBuilder buildG3Neighborhood(Device device);

    /**
     * Find the neighboring {@link Device}s,
     * for the G3/PLC communication technology.
     *
     * @param device The Device whose neighborhood will be returned
     * @return The List of Device
     */
    public List<Device> findDevicesInG3Neighborhood(Device device);

    /**
     * Find the {@link G3Neighbor}s of the specified {@link Device},
     * for the G3/PLC communication technology.
     *
     * @param device The Device whose neighbors will be returned
     * @return The List of G3Neighbor
     */
    public List<G3Neighbor> findG3Neighbors(Device device);

    /**
     * Find the neighboring {@link Device}s,
     * for the G3/PLC communication technology.
     *
     * @param device The Device whose neighborhood will be returned
     * @param when The timestamp on which the Devices were effectively in the Device's neighborhood
     * @return The List of Device
     */
    public List<Device> findDevicesInG3Neighborhood(Device device, Instant when);

    /**
     * Find the {@link G3Neighbor}s of the specified {@link Device},
     * for the G3/PLC communication technology.
     *
     * @param device The Device whose neighbors will be returned
     * @param when The timestamp on which the G3Neighbor were effectively part of the Device's neighborhood
     * @return The List of G3Neighbor
     */
    public List<G3Neighbor> findG3Neighbors(Device device, Instant when);

    /**
     * Gets the current {@link G3DeviceAddressInformation} for the specified {@link Device}.
     *
     * @param device The Device
     * @return The G3DeviceAddressInformation
     */
    public Optional<G3DeviceAddressInformation> getG3DeviceAddressInformation(Device device);

    /**
     * Gets the {@link G3DeviceAddressInformation} for the specified {@link Device}
     * that was effective on the specified timestamp.
     *
     * @param device The Device
     * @param when The timestamp on which the G3DeviceAddressInformation should be effective
     * @return The G3DeviceAddressInformation
     */
    public Optional<G3DeviceAddressInformation> getG3DeviceAddressInformation(Device device, Instant when);

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
    public G3DeviceAddressInformation setG3DeviceAddressInformation(Device device, String ipv6Address, int ipv6ShortAddress, int logicalDeviceId);

    public interface G3CommunicationPathSegmentBuilder {

        /**
         * Adds a {@link G3CommunicationPathSegment} from the source to the target
         * {@link Device}, using the specified intermediate Device.
         * The source Device is the one that was specified in the
         * {@link #addCommunicationSegments(Device)} method
         * that returned this Builder in the first place.
         * It is allowed that the intermediate Device is null or
         * the same as the  target Device, in that case,
         * the added segment will be a direct or final segment.
         *
         * @param target The target Device
         * @param intermediateHop The intermediate Device
         * @param timeToLive The time to live
         * @param cost The segment's cost
         */
        public G3CommunicationPathSegmentBuilder add(Device target, Device intermediateHop, Duration timeToLive, int cost);

        /**
         * Completes the building process, effectively creating all segments
         * and making sure they all have the same effective timestamp.
         *
         * @return The newly created segments
         */
        public List<G3CommunicationPathSegment> complete();

    }

    /**
     * Build all the neighbors of one {@link Device}.
     * Device's whose neighborhood has been built before can be updated
     * with the same builder. Devices that were not revisited, i.e.
     * the {@link #addNeighbor(Device, ModulationScheme, Modulation, PhaseInfo)}
     * was not called will be deleted upon completion.
     */
    public interface G3NeighborhoodBuilder {

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
         * @return The G3NeighborBuilder that allows you to specify the optional neighboring information
         */
        public G3NeighborBuilder addNeighbor(Device neighbor, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo);

        /**
         * Completes the building process and returns
         * the {@link G3Neighbor}s that were added.
         * Note that this builder is now completely useless
         * and will throw IllegalStateExceptions when
         * attempting to complete again.
         */
        public List<G3Neighbor> complete();
    }

    public interface G3NeighborBuilder {
        public G3NeighborBuilder txGain(int txGain);
        public G3NeighborBuilder txResolution(int txResolution);
        public G3NeighborBuilder txCoefficient(int txCoefficient);
        public G3NeighborBuilder linkQualityIndicator(int linkQualityIndicator);
        public G3NeighborBuilder timeToLiveSeconds(int seconds);
        public G3NeighborBuilder toneMap(long toneMap);
        public G3NeighborBuilder toneMapTimeToLiveSeconds(int seconds);
    }

}