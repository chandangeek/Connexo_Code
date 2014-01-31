package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a {@link com.energyict.mdc.device.config.RegisterSpec} with
 * a {@link ChannelSpec} that is linked to another {@link DeviceConfiguration}
 * then the DeviceConfiguration of the RegisterSpec
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/01/14
 * Time: 13:52
 */
public class InCorrectDeviceConfigOfChannelSpecException extends LocalizedException{

    public InCorrectDeviceConfigOfChannelSpecException(Thesaurus thesaurus, ChannelSpec channelSpec, DeviceConfiguration channelSpecDeviceConfig, DeviceConfiguration registerMappingDeviceConfig) {
        super(thesaurus, MessageSeeds.REGISTER_SPEC_CHANNEL_SPEC_OF_ANOTHER_DEVICE_CONFIG, channelSpec.getName(), channelSpecDeviceConfig.getName(), registerMappingDeviceConfig.getName());
        this.set("channelSpec", channelSpec);
        this.set("channelSpecDeviceConfig", channelSpecDeviceConfig);
        this.set("registerMappingDeviceConfig", registerMappingDeviceConfig);
    }
}
