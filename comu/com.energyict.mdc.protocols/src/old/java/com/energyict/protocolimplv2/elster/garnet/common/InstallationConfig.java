/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.common;

import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterInstallationStatusBitMaskField;

import java.util.ArrayList;
import java.util.List;

public class InstallationConfig {

    private List<List<Integer>> installationConfigs;

    public InstallationConfig(List<MeterInstallationStatusBitMaskField> installationStatuses) {
        this.installationConfigs = new ArrayList<>();
        buildInstallationConfig(installationStatuses);
    }

    private void buildInstallationConfig(List<MeterInstallationStatusBitMaskField> installationStatuses) {
        int i = 0;
        while (i < installationStatuses.size()) {
            List<Integer> config = new ArrayList<>();
            switch (installationStatuses.get(i).getInstallationStatus()) {
                case TWO_PHASE:
                    config.add(i);
                    config.add(i + 1);
                    i += 2;
                    break;
                case THREE_PHASE:
                    config.add(i);
                    config.add(i + 1);
                    config.add(i + 2);
                    i += 3;
                    break;
                default:
                    config.add(i);
                    i++;
                    break;
            }

            installationConfigs.add(config);
        }
    }

    public List<Integer> getConfigForMeter(int meterNumber) {
        for (List<Integer> config : installationConfigs) {
            if (config.contains(meterNumber)) {
                return config;
            }
        }
        return new ArrayList<>();
    }
}