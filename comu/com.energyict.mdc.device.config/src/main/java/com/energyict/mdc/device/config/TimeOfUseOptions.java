package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.upl.messages.ProtocolSupportedCalendarOptions;

import java.util.Set;

@ProviderType
public interface TimeOfUseOptions {

    void setOptions(Set<ProtocolSupportedCalendarOptions> allowedOptions);

    Set<ProtocolSupportedCalendarOptions> getOptions();

    void save();

    void delete();

    long getVersion();
}
