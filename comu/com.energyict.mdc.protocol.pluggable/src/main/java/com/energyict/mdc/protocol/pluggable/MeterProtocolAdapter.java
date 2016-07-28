package com.energyict.mdc.protocol.pluggable;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.protocol.api.*;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import java.util.List;

/**
 * Defines the interface of a component that will adapt
 * the legacy {@link com.energyict.mdc.protocol.api.legacy.MeterProtocol}
 * to the current {@link DeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (17:41)
 */
@ProviderType
public interface MeterProtocolAdapter extends DeviceProtocol, DeviceProtocolAdapter {

    List<CollectedData> getLoadProfileLogBooksData(List<LoadProfileReader> loadProfiles, List<LogBookReader> logBookReaders);

    DeviceProtocolCache getDeviceCache();

    void setDeviceCache(DeviceProtocolCache deviceProtocolCache);

    MeterProtocol getMeterProtocol();

    LogBookReader getValidLogBook(List<LogBookReader> logBookReaders);

}