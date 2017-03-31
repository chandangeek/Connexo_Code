package com.energyict.mdc.device.topology.multielement;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 15/03/2017
 * Time: 15:34
 *
 * Provides services that relate to multi-element devices and their multi-element slaves.
 */
@ProviderType
public interface MultiElementDeviceService {

    String COMPONENT_NAME = "MES";

    /**
     * Link the given slave device to the given multi-element device
     * @param slave device
     * @param multiElementDevice parent device
     * @param linkingDate date at which the slave device makes part of the multi-element device
     * @param slaveDataLoggerChannelMap map of channels to link with each other
     * @param slaveDataLoggerRegisterMap map of registers to link with each other
     */
    void addSlave(Device slave , Device multiElementDevice, Instant linkingDate, Map<Channel, Channel> slaveDataLoggerChannelMap, Map<Register, Register> slaveDataLoggerRegisterMap);

    /**
     * removes the slave device from its multi-element device
     * @param slave device to remove from its multi-element device
     * @param when time at which the removal needs to happen
     */
    void removeSlave(Device slave, Instant when );

    /**
     * Returns the multi-element slave device's multi-element device to which the slave belongs
     * @param slave device for which we are looking for its multi-element device
     * @param when
     * @return
     */
    Optional<Device> getMultiElementDevice(Device slave, Instant when);

    /**
     * Finds the {@link Device}s that are multi-element slaves of the specified multi-element Device.
     *
     * @param multiElementDevice the multi-element device
     * @return The List multi-element slave devices
     */
    List<Device> findMultiElementSlaves(Device multiElementDevice);

    /**
     * @return all multi-element slave devices which at this moment are effectively linked to a multi-element device
     */
    Finder<? extends MultiElementDeviceReference> findAllEffectiveMultiElementSlaveDevices();

    /**
     * Finds the MultiElementDeviceReference which is effective at the given timestamp.
     * If no reference was active, an empty optional will be returned
     *
     * @param slaveDevice the multi-element slave device which has potentially a MultiElementDeviceReference for the given timestamp
     * @param effective the timeStamp at which you want the reference
     * @return the MultiElementDeviceReference which was active at the given timestamp
     */
    Optional<MultiElementDeviceReference> findMultiElementDeviceReference(Device slaveDevice, Instant effective);

    /**
     * Provides an ordered list which contains pairs of channels (of different devices) vs ranges in which they should contain data
     * for the requested range.
     *
     * @param channel the Multi-element device channel
     * @param range the range in which we want to collect the data
     * @return the requested list, ordered descending according to interval.start
     */
    List<Pair<Channel, Range<Instant>>> getMultiElementSlaveChannelTimeLine(Channel channel, Range<Instant> range);

    List<Pair<Register, Range<Instant>>> getMultiElementSlaveRegisterTimeLine(Register register, Range<Instant> intervalReg);

}
