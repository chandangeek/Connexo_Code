/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;

import com.energyict.protocolimpl.utils.MessagingTools;

import java.io.IOException;

/**
 * @author sva
 * @since 14/03/2016 - 15:40
 */
public class LoadLimitingController {

    private static ABBA230 meterProtocol;

    public LoadLimitingController(ABBA230 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    public void disableLoadLimiting(MessageEntry messageEntry) throws IOException {
        LoadMonitoringConfiguration loadMonitoringConfiguration = meterProtocol.getRegisterFactory().getLoadMonitoringConfiguration();
        loadMonitoringConfiguration.disable();

        meterProtocol.getRegisterFactory().setRegister(ABBA230RegisterFactory.LOAD_MONITORING_CONFIGURATION, loadMonitoringConfiguration);
    }

    public void setLoadLimitDuration(MessageEntry messageEntry) throws IOException {
        try {
            int duration = Integer.parseInt(MessagingTools.getContentOfAttribute(messageEntry, ABBA230.DURATION_ATTRIBUTE));
            LoadMonitoringConfiguration loadMonitoringConfiguration = meterProtocol.getRegisterFactory().getLoadMonitoringConfiguration();
            loadMonitoringConfiguration.enable()
                    .disableLowerContactorOpen()
                    .enableHigherContactorOpen()
                    .setHigherLevelDuration(duration);

            meterProtocol.getRegisterFactory().setRegister(ABBA230RegisterFactory.LOAD_MONITORING_CONFIGURATION, loadMonitoringConfiguration);
        } catch (NumberFormatException e) {
            throw new IOException("Failed to parse the duration: " + e.getMessage());
        }
    }

    public void setLoadLimitThreshold(MessageEntry messageEntry) throws IOException {
        try {
            Float threshold = Float.parseFloat(MessagingTools.getContentOfAttribute(messageEntry, ABBA230.THRESHOLD_ATTRIBUTE));
            String unit = MessagingTools.getContentOfAttribute(messageEntry, ABBA230.UNIT_ATTRIBUTE);

            if (threshold < 0 || threshold > 150) {
                throw new IOException("Invalid threshold value (" + threshold + "), the threshold should be in range 0 - 150 %");
            }
            if (!unit.equals("%")) {
                throw new IOException("Invalid unit (" + unit + "), the threshold should be specified as %.");
            }

            LoadMonitoringConfiguration loadMonitoringConfiguration = meterProtocol.getRegisterFactory().getLoadMonitoringConfiguration();
            loadMonitoringConfiguration.enable()
                    .disableLowerContactorOpen()
                    .enableHigherContactorOpen()
                    .setHigherLevelThreshold(((Float) (threshold * 10)).intValue());

            meterProtocol.getRegisterFactory().setRegister(ABBA230RegisterFactory.LOAD_MONITORING_CONFIGURATION, loadMonitoringConfiguration);
        } catch (NumberFormatException e) {
            throw new IOException("Failed to parse the threshold value: " + e.getMessage());
        }
    }

    public void configureLoadLimitSettings(MessageEntry messageEntry) throws IOException {
        try {
            Float threshold = Float.parseFloat(MessagingTools.getContentOfAttribute(messageEntry, ABBA230.THRESHOLD_ATTRIBUTE));
            String unit = MessagingTools.getContentOfAttribute(messageEntry, ABBA230.UNIT_ATTRIBUTE);
            int duration = Integer.parseInt(MessagingTools.getContentOfAttribute(messageEntry, ABBA230.DURATION_ATTRIBUTE));

            if (threshold < 0 || threshold > 150) {
                throw new IOException("Invalid threshold value (" + threshold + "), the threshold should be in range 0 - 150 %");
            }
            if (!unit.equals("%")) {
                throw new IOException("Invalid unit (" + unit + "), the threshold should be specified as %.");
            }

            LoadMonitoringConfiguration loadMonitoringConfiguration = meterProtocol.getRegisterFactory().getLoadMonitoringConfiguration();
            loadMonitoringConfiguration.enable()
                    .disableLowerContactorOpen()
                    .enableHigherContactorOpen()
                    .setHigherLevelThreshold(((Float) (threshold * 10)).intValue())
                    .setHigherLevelDuration(duration);

            meterProtocol.getRegisterFactory().setRegister(ABBA230RegisterFactory.LOAD_MONITORING_CONFIGURATION, loadMonitoringConfiguration);
        } catch (NumberFormatException e) {
            throw new IOException("Failed to parse the threshold value and/or duration: " + e.getMessage());
        }
    }
}