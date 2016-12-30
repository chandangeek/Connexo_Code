package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.meterdata.LoadProfile;

import java.util.Collections;
import java.util.List;

/**
 * Provides an dummy implementation for the {@link Extractor} interface
 * that returns empty values in all of its methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-28 (09:49)
 */
final class DummyLoadProfileExtractor implements LoadProfileExtractor {
    @Override
    public String id(LoadProfile loadProfile) {
        return "";
    }

    @Override
    public String specDeviceObisCode(LoadProfile loadProfile) {
        return "";
    }

    @Override
    public String deviceSerialNumber(LoadProfile loadProfile) {
        return "";
    }

    @Override
    public List<Channel> channels(LoadProfile loadProfile) {
        return Collections.emptyList();
    }

    @Override
    public List<Register> registers(LoadProfile loadProfile) {
        return Collections.emptyList();
    }
}