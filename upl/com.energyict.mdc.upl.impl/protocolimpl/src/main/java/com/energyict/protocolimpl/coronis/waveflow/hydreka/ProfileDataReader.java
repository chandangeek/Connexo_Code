package com.energyict.protocolimpl.coronis.waveflow.hydreka;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 18/12/12
 * Time: 10:59
 * Author: khe
 */
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