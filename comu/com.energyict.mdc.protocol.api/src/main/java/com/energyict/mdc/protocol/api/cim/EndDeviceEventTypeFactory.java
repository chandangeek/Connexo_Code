package com.energyict.mdc.protocol.api.cim;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Factory containing static methods for the easy creation of different {@link EndDeviceEventType}s without knowing the exact code.
 *
 * @author sva
 * @since 4/06/13 - 15:42
 */
@Component(name="com.energyict.mdc.protocols.api.cim.enddeviceeventtype.factory", service = {EndDeviceEventTypeFactory.class}, property = "name=CEF")
public class EndDeviceEventTypeFactory {

    private static final Logger LOGGER = Logger.getLogger(EndDeviceEventTypeFactory.class.getName());
    private static AtomicReference<EndDeviceEventTypeFactory> current = new AtomicReference<>();

    private volatile MeteringService meteringService;

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Activate
    public void activate() {
        current.set(this);
    }

    public static EndDeviceEventType getOtherEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.0.0.0");
    }

    private EndDeviceEventType findOrCreateEndDeviceEventType(String mRID) {
        for (EndDeviceEventType endDeviceEventType : this.meteringService.getAvailableEndDeviceEventTypes()) {
            if (endDeviceEventType.getMRID().equals(mRID)) {
                return endDeviceEventType;
            }
        }
        LOGGER.severe("EndDeviceEventType missing: " + mRID);
        return null;
    }

    public static EndDeviceEventType getPowerDownEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.26.38.47");
    }

    public static EndDeviceEventType getPowerUpEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.26.38.49");
    }

    public static EndDeviceEventType getWatchdogResetEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.11.3.215");
    }

    public static EndDeviceEventType getSetClockBeforeEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.36.116.14");
    }

    public static EndDeviceEventType getSetClockAfterEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.36.116.24");
    }

    public static EndDeviceEventType getSetClockEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.36.116.13");
    }

    public static EndDeviceEventType getConfigurationChangeEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.7.31.13");
    }

    public static EndDeviceEventType getRamMemoryErrorEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.18.85.79");
    }

    public static EndDeviceEventType getProgramFlowErrorEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.11.83.79");
    }

    public static EndDeviceEventType getRegisterOverflowEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.21.89.177");
    }

    public static EndDeviceEventType getFatalErrorEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.0.43.79");
    }

    public static EndDeviceEventType getClearDataEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.18.31.28");
    }

    public static EndDeviceEventType getHardwareErrorEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.0.0.79");
    }

    public static EndDeviceEventType getMeterAlarmEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.11.46.79");
    }

    public static EndDeviceEventType getRomMemoryErrorEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.18.92.79");
    }

    public static EndDeviceEventType getMaximumDemandResetEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.8.87.215");
    }

    public static EndDeviceEventType getBillingActionEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.20.43.44");
    }

    public static EndDeviceEventType getApplicationAlertStartEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.11.43.242");
    }

    public static EndDeviceEventType getApplicationAlertStopEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.11.43.243");
    }

    public static EndDeviceEventType getPhaseFailureEventType() {
        return current.get().findOrCreateEndDeviceEventType("1.26.25.85");
    }

    public static EndDeviceEventType getVoltageSagEventType() {
        return current.get().findOrCreateEndDeviceEventType("1.26.79.223");
    }

    public static EndDeviceEventType getVoltageSwellEventType() {
        return current.get().findOrCreateEndDeviceEventType("1.26.79.248");
    }

    public static EndDeviceEventType getTamperEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.12.43.257");
    }

    public static EndDeviceEventType getCoverOpenedEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.12.29.39");
    }

    public static EndDeviceEventType getTerminalOpenedEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.12.141.39");
    }

    public static EndDeviceEventType getReverseRunEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.12.48.219");
    }

    public static EndDeviceEventType getLoadProfileClearedEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.16.87.28");
    }

    public static EndDeviceEventType getEventLogClearedEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.17.44.28");
    }

    public static EndDeviceEventType getDaylightSavingTimeEnabledOrDisabledEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.36.56.24");
    }

    public static EndDeviceEventType getClockInvalidEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.36.43.35");
    }

    public static EndDeviceEventType getReplaceBatteryEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.2.0.150");
    }

    public static EndDeviceEventType getBatteryVoltageLowEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.2.22.150");
    }

    public static EndDeviceEventType getTouActivatedEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.20.121.4");
    }

    public static EndDeviceEventType getErrorRegisterClearedEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.17.89.28");
    }

    public static EndDeviceEventType getAlarmRegisterClearedEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.17.89.28");
    }

    public static EndDeviceEventType getProgramMemoryErrorEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.18.83.79");
    }

    public static EndDeviceEventType getNvMemoryErrorEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.18.72.79");
    }

    public static EndDeviceEventType getWatchdogErrorEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.11.3.79");
    }

    public static EndDeviceEventType getMeasurementSystemErrorEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.21.67.79");
    }

    public static EndDeviceEventType getFirmwareReadyForActivationEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.11.31.25");
    }

    public static EndDeviceEventType getFirmwareActivatedEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.11.31.4");
    }

    public static EndDeviceEventType getTerminalCoverClosedEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.12.141.16");
    }

    public static EndDeviceEventType getStrongDCFieldDetectedEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.12.66.242");
    }

    public static EndDeviceEventType getNoStrongDCFieldAnymoreEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.12.66.243");
    }

    public static EndDeviceEventType getMeterCoverClosedEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.12.29.16");
    }

    public static EndDeviceEventType getNTimesWrongPasswordEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.12.24.7");
    }

    public static EndDeviceEventType getManualDisconnectionEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.31.0.68");
    }

    public static EndDeviceEventType getManualConnectionEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.31.0.42");
    }

    public static EndDeviceEventType getRemoteDisconnectionEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.31.0.68");
    }

    public static EndDeviceEventType getRemoteConnectionEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.31.0.42");
    }

    public static EndDeviceEventType getLocalDisconnectionEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.31.0.68");
    }

    public static EndDeviceEventType getLimiterThresholdExceededEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.15.261.139");
    }

    public static EndDeviceEventType getLimiterThresholdOkEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.15.261.216");
    }

    public static EndDeviceEventType getLimiterThresholdChangedEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.15.261.24");
    }

    public static EndDeviceEventType getCommunicationErrorMbusEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.1.147.79");
    }

    public static EndDeviceEventType getCommunicationOkMbusEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.1.147.216");
    }

    public static EndDeviceEventType getReplaceBatteryMbusEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.2.147.150");
    }

    public static EndDeviceEventType getFraudAttemptMbusEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.12.147.7");
    }

    public static EndDeviceEventType getClockAdjustedMbusEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.36.147.24");
    }

    public static EndDeviceEventType getManualDisconnectionMbusEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.31.147.68");
    }

    public static EndDeviceEventType getManualConnectionMbusEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.31.147.42");
    }

    public static EndDeviceEventType getRemoteDisconnectionMbusEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.31.147.68");
    }

    public static EndDeviceEventType getRemoteConnectionMbusEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.31.147.42");
    }

    public static EndDeviceEventType getValveAlarmMbusEventType() {
        return current.get().findOrCreateEndDeviceEventType("0.31.147.79");
    }

}