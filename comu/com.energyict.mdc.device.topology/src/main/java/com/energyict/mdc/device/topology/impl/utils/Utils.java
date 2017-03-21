package com.energyict.mdc.device.topology.impl.utils;

import com.elster.jupiter.metering.MeterActivation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 16/03/2017
 * Time: 10:49
 */
public class Utils {

    /**
     * We truncate the link and unlink dates of a datalogger to 0 seconds.
     * This way we prevent unintentional misconfiguration of unlinking before linking (within the same minute)
     *
     * @param when the date to generalize
     * @return the truncated date
     */
    public static Instant generalizeLinkingDate(Instant when) {
        return when.truncatedTo(ChronoUnit.MINUTES);
    }

    /**
     * Returns the overlapping meter activations of the master device with the meteractivation of the slave device
     * @param slaveMeterActivation to get the matching master device meter activations
     * @param masterDeviceMeterActivations the master device meter activations
     * @return the list filtered list of master device meter activations
     */
    public static List<MeterActivation> getOverLappingDataLoggerMeterActivations(MeterActivation slaveMeterActivation, List<MeterActivation> masterDeviceMeterActivations) {
        return masterDeviceMeterActivations.stream()
                .filter(dataLoggerMeterActivation -> slaveMeterActivation.getRange().isConnected(dataLoggerMeterActivation.getRange()))
                .collect(Collectors.toList());
    }
}
