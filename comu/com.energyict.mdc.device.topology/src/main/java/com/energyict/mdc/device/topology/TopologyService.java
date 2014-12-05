package com.energyict.mdc.device.topology;

import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.api.device.BaseChannel;

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

    public Optional<Device> getPhysicalGateway(Device slave, Instant timestamp);

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

    public List<Device> getPhysicalConnectedDevices(Device gateway);

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
     * Gets the {@link Device}'s {@link Channel}s AND the Channels of all
     * <i>physical</i> slave devices belonging to {@link LoadProfile}s of the same type.
     *
     * @return a <CODE>List</CODE> of <CODE>Channel</CODE> objects
     */
    public List<Channel> getAllChannels(LoadProfile loadProfile);

}