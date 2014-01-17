package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.CollectedData;

import java.util.List;

/**
 * Defines the interface of a component that will adapt
 * the legacy {@link com.energyict.mdc.protocol.api.legacy.MeterProtocol}
 * to the current {@link DeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (17:41)
 */
public interface MeterProtocolAdapter extends DeviceProtocol {

    public List<CollectedData> getLoadProfileLogBooksData(List<LoadProfileReader> loadProfiles, List<LogBookReader> logBookReaders);

    public DeviceProtocolCache getDeviceCache();

    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache);

}