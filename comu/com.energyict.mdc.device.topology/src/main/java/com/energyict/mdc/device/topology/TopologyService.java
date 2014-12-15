package com.energyict.mdc.device.topology;

import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.CommunicationErrorType;

import com.elster.jupiter.util.time.Interval;
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
     * Adds a {@link G3CommunicationPathSegment} from the source to the target
     * {@link Device}, using the specified intermediate Device.
     * It is allowed that the intermediate Device is null or
     * the same as the  target Device, in that case,
     * the added segment will be a direct or final segment.
     *
     * @param source
     * @param target
     * @param intermediateHop
     * @param timeToLive
     * @param cost
     * @return
     */
    public G3CommunicationPathSegment addCommunicationSegment(Device source, Device target, Device intermediateHop, Duration timeToLive, int cost);

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

}