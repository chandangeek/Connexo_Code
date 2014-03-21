package com.energyict.mdc.device.config;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.protocol.api.device.Device;
import java.util.List;

/**
 * User: gde
 * Date: 5/11/12
 */
public interface DeviceConfiguration extends HasId {


    /**
     * Returns the object's unique id
     *
     * @return the id
     */
    public long getId();

    /**
     * Returns the object's name
     *
     * @return the name
     */
    public String getName();

    void setName(String name);

    /**
     * Returns a description of the receiver
     *
     * @return description
     */
    String getDescription();

    void setDescription(String description);

    /**
     * Returns the prototype device for this device config
     *
     * @return the prototype device or null
     */
    Device getPrototypeDevice();

    /**
     * Returns the ID of the prototype device
     *
     * @return the ID of the prototype device or 0
     */
    int getPrototypeId();

    /**
     * Returns the <code>DeviceType</code> this device config belongs to
     *
     * @return the <code>DeviceType</code> this device config belongs to
     */
    DeviceType getDeviceType();

    List<RegisterSpec> getRegisterSpecs();

    RegisterSpec.RegisterSpecBuilder createRegisterSpec(RegisterMapping registerMapping);

    RegisterSpec.RegisterSpecUpdater getRegisterSpecUpdaterFor(RegisterSpec registerSpec);

    void deleteRegisterSpec(RegisterSpec registerSpec);

    List<ChannelSpec> getChannelSpecs();

    ChannelSpec.ChannelSpecBuilder createChannelSpec(RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec);

    ChannelSpec.ChannelSpecBuilder createChannelSpec(RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder);

    ChannelSpec.ChannelSpecUpdater getChannelSpecUpdaterFor(ChannelSpec channelSpec);

    void deleteChannelSpec(ChannelSpec channelSpec);

    List<LoadProfileSpec> getLoadProfileSpecs();

    LoadProfileSpec.LoadProfileSpecBuilder createLoadProfileSpec(LoadProfileType loadProfileType);

    LoadProfileSpec.LoadProfileSpecUpdater getLoadProfileSpecUpdaterFor(LoadProfileSpec loadProfileSpec);

    void deleteLoadProfileSpec(LoadProfileSpec loadProfileSpec);

    List<LogBookSpec> getLogBookSpecs();

    LogBookSpec.LogBookSpecBuilder createLogBookSpec(LogBookType logBookType);

    LogBookSpec.LogBookSpecUpdater getLogBookSpecUpdaterFor(LogBookSpec logBookSpec);

    void deleteLogBookSpec(LogBookSpec logBookSpec);

    /**
     * Tests if the receiver is active.
     *
     * @return true if active, false otherwise
     */
    boolean isActive();

    void activate();

    void deactivate();

    boolean hasLogBookSpecForConfig(int logBookTypeId, int updateId);

    /**
     * Gets the details of this DeviceConfiguration that relate to communication.
     *
     * @return The DeviceCommunicationConfiguration
     */
    public DeviceCommunicationConfiguration getCommunicationConfiguration();

    void save();

    //TODO we remove 'CreateDeviceTransaction' and 'DeviceConfigurationChanges' from the API, must be included when time comes ...
}
