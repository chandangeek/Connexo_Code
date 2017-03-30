/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.hydreka;

import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ProfileDataReader {

    private Hydreka waveFlow;

    public ProfileDataReader(Hydreka waveFlow) {
        this.waveFlow = waveFlow;
    }

    public ProfileData readProfileData(Date lastReading, Date toDate, boolean includeEvents) throws IOException {
        ProfileData profileData = new ProfileData();
        if (includeEvents) {
            profileData.setMeterEvents(readMeterEvents());
        }
        return profileData;
    }

    private List<MeterEvent> readMeterEvents() throws IOException {
        ApplicationStatusParser parser = new ApplicationStatusParser(waveFlow, false);
        return parser.getMeterEvents(waveFlow.getParameterFactory().readApplicationStatus());
    }
}