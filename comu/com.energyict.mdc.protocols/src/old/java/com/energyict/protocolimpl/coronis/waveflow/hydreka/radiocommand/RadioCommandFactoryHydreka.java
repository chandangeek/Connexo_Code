/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.hydreka.radiocommand;

import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.RadioCommandFactory;
import com.energyict.protocolimpl.coronis.waveflow.hydreka.Hydreka;

import java.io.IOException;

public class RadioCommandFactoryHydreka extends RadioCommandFactory {

    private DailyHydrekaDataReading dailyHydrekaDataReading;

    public RadioCommandFactoryHydreka(Hydreka waveFlow) {
        super(waveFlow);
    }

    public DailyHydrekaDataReading getDailyHydrekaDataReading() {
        return dailyHydrekaDataReading;
    }

    public void setDailyHydrekaDataReading(DailyHydrekaDataReading dailyHydrekaDataReading) {
        this.dailyHydrekaDataReading = dailyHydrekaDataReading;
    }

    public DailyHydrekaDataReading readDailyHydrekaDataReading() throws IOException {
        if (dailyHydrekaDataReading == null) {
            dailyHydrekaDataReading = new DailyHydrekaDataReading(waveFlow);
            dailyHydrekaDataReading.set();
        }
        ((Hydreka) waveFlow).getParameterFactory().setLeakageDetectionDate(dailyHydrekaDataReading.getLeakageDetectionDate());
        return dailyHydrekaDataReading;
    }
}
