/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolAdapter;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.meterdata.CollectedData;

import aQute.bnd.annotation.ProviderType;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;

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

    com.energyict.mdc.upl.MeterProtocol getUplMeterProtocol();

    LogBookReader getValidLogBook(List<LogBookReader> logBookReaders);

}