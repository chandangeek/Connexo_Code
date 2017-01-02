package com.energyict.mdc.firmware;

import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import java.util.Set;

public interface FirmwareManagementOptions {

    void setOptions(Set<ProtocolSupportedFirmwareOptions> allowedOptions);

    Set<ProtocolSupportedFirmwareOptions> getOptions();

    void save();

    void delete();

    long getVersion();
}
