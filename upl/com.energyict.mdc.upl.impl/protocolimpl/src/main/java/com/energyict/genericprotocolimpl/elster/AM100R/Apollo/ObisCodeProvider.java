package com.energyict.genericprotocolimpl.elster.AM100R.Apollo;

import com.energyict.genericprotocolimpl.common.CommonObisCodeProvider;
import com.energyict.obis.ObisCode;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Straightforward summary of the possible ObisCodes of the Apollo meter
 * </p>
 * <p>
 * Copyrights EnergyICT<br/>
 * Date: 23-nov-2010<br/>
 * Time: 16:32:14<br/>
 * </p>
 */
public class ObisCodeProvider implements CommonObisCodeProvider {

    public static final ObisCode ClockObisCode = ObisCode.fromString("0.0.1.0.0.255");
    public static final ObisCode ClockSynchronizationObisCode = ObisCode.fromString("0.0.96.2.12.255");
    public static final ObisCode SerialNumberObisCode = ObisCode.fromString("0.0.96.1.0.255");
    public static final ObisCode LoadProfileP1 = ObisCode.fromString("1.0.99.1.0.255");
    public static final ObisCode LoadProfileDaily = ObisCode.fromString("0.0.98.2.1.255");
    public static final ObisCode LoadProfileMonthly = ObisCode.fromString("0.0.98.1.1.255");
    public static final ObisCode AssociationLnCurrentClient = ObisCode.fromString("0.0.40.0.0.255");
    public static final ObisCode AssociationLnPublicClient = ObisCode.fromString("0.0.40.0.1.255");
    public static final ObisCode AssociationLnReadingClient = ObisCode.fromString("0.0.40.0.2.255");
    public static final ObisCode AssociationLnManagementClient = ObisCode.fromString("0.0.40.0.3.255");
    public static final ObisCode AssociationLnFirmwareClient = ObisCode.fromString("0.0.40.0.4.255");
    public static final ObisCode FirmwareVersionObisCode = ObisCode.fromString("1.0.0.2.0.255");
    public static final ObisCode ActiveLongFirmwareIdentifierACOR = ObisCode.fromString("1.0.0.2.0.1");
    public static final ObisCode ActiveLongFirmwareIdentifierMCOR = ObisCode.fromString("1.0.0.2.0.2");

    public static final ObisCode StandardEventLogObisCode = ObisCode.fromString("0.0.99.98.0.255");
    public static final ObisCode PowerQualityFinishedEventLogObisCode = ObisCode.fromString("0.0.99.98.9.255");
    public static final ObisCode PowerQualityNotFinishedEventLogObisCode = ObisCode.fromString("0.0.99.98.5.255");
    public static final ObisCode FraudDetectionEventLogObisCode = ObisCode.fromString("0.0.99.98.1.255");
    public static final ObisCode DemandManagementEventLogObisCode = ObisCode.fromString("0.0.99.98.6.255");
    public static final ObisCode CommonEventLogObisCode = ObisCode.fromString("0.0.99.98.7.255");
    public static final ObisCode PowerContractEventLogObisCode = ObisCode.fromString("0.0.99.98.3.255");
    public static final ObisCode FirmwareEventLogObisCode = ObisCode.fromString("0.0.99.98.4.255");
    public static final ObisCode ObjectSynchronizationEventLogObisCode = ObisCode.fromString("0.0.99.98.8.255");

    public static final ObisCode InstantaneousEnergyValuesObisCode = ObisCode.fromString("0.0.21.0.6.255");
    public static final ObisCode RefVoltagePQObisCode = ObisCode.fromString("1.0.0.6.4.255");
    public static final ObisCode NrOfVoltageSagsAvgVoltageObisCode = ObisCode.fromString("1.0.94.34.90.255");
    public static final ObisCode DurationVoltageSagsAvgVoltageObisCode = ObisCode.fromString("1.0.93.34.91.255");
    public static final ObisCode NrOfVoltageSwellsAvgVoltageObisCode = ObisCode.fromString("1.0.94.34.92.255");
    public static final ObisCode DurationVoltageSwellsAvgVoltageObisCode = ObisCode.fromString("1.0.93.34.93.255");
    public static final ObisCode ActiveQuadrantObisCode = ObisCode.fromString("1.1.94.34.100.255");
    public static final ObisCode ActiveQuadrantL1ObisCode = ObisCode.fromString("1.1.94.34.101.255");
    public static final ObisCode ActiveQuadrantL2ObisCode = ObisCode.fromString("1.1.94.34.102.255");
    public static final ObisCode ActiveQuadrantL3ObisCode = ObisCode.fromString("1.1.94.34.103.255");
    public static final ObisCode PhasePrecense = ObisCode.fromString("1.1.94.34.104.255");
    public static final ObisCode TransformerRatioCurrentNumObisCode = ObisCode.fromString("1.0.0.4.2.255");
    public static final ObisCode TransformerRatioVoltageNumObisCode = ObisCode.fromString("1.0.0.4.3.255");
    public static final ObisCode TransformerRatioCurrentDenObisCode = ObisCode.fromString("1.0.0.4.5.255");
    public static final ObisCode TransformerRatioVoltageDenObisCode = ObisCode.fromString("1.0.0.4.6.255");

    public static final ObisCode ActivityCalendarContract1ObisCode = ObisCode.fromString("0.0.13.0.1.255");
    public static final ObisCode ActiveSpecialDayContract1ObisCode = ObisCode.fromString("0.0.11.0.1.255");
    public static final ObisCode PassiveSpecialDayContract1ObisCode = ObisCode.fromString("0.0.11.0.4.255");
    public static final ObisCode CurrentActiveRateContract1ObisCode = ObisCode.fromString("0.0.96.14.1.255");
    public static final ObisCode ActiveCalendarNameObisCode = ObisCode.fromString("0.0.13.0.1.255");
    public static final ObisCode PassiveCalendarNameObisCode = ObisCode.fromString("0.0.13.0.2.255");

    /**
     * @return the {@link #ClockObisCode} for the {@link com.energyict.dlms.cosem.Clock}
     */
    public ObisCode getClockObisCode() {
        return ClockObisCode;
    }

    /**
     * @return the obisCode for the <i>default</i> {@link com.energyict.dlms.cosem.ProfileGeneric}
     */
    public ObisCode getDefaultLoadProfileObisCode() {
        return LoadProfileP1;
    }

    /**
     * @return the obisCode fro the <i>Daily</i> LoadProfile
     */
    public ObisCode getDailyLoadProfileObisCode() {
        return LoadProfileDaily;
    }

    /**
     * @return the {@link #ClockSynchronizationObisCode} for the ClockSynchronization
     */
    public ObisCode getClockSynchronization() {
        return ClockSynchronizationObisCode;
    }

    /**
     * @return the {@link #SerialNumberObisCode} for the SerialNumber
     */
    public ObisCode getSerialNumberObisCode() {
        return SerialNumberObisCode;
    }

    /**
     * We return the Association according to the Management Client (clientId = 1)
     *
     * @return the obisCode for the AssociationLn object
     */
    public ObisCode getAssociationLnObisCode() {
        return getAssociationLnObisCode(1);
    }

    /**
     * @param clientId the used clientId for this association
     * @return the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ObisCodeProvider.AssociationLnObisCodes#obiscode} for the {@link com.energyict.dlms.cosem.AssociationLN} object
     */
    public ObisCode getAssociationLnObisCode(int clientId) {
        return AssociationLnObisCodes.forClient(clientId);
    }

    /**
     * @return the {@link #FirmwareVersionObisCode} of this Firmware Version
     */
    public ObisCode getFirmwareVersion() {
        return FirmwareVersionObisCode;
    }

    /**
     * @return the {@link #InstantaneousEnergyValuesObisCode} of the Instantaneous Values Profile
     */
    public ObisCode getInstantaneousEnergyValueObisCode(){
        return InstantaneousEnergyValuesObisCode;
    }

    /**
     * @return the {@link #StandardEventLogObisCode} of the Standard Event Profile
     */
    public ObisCode getStandardEventLogObisCode() {
        return StandardEventLogObisCode;
    }

    /**
     * @return the {@link #PowerQualityFinishedEventLogObisCode} for the PowerQuality Event Profile
     */
    public ObisCode getPowerQualityEventLogObisCode() {
        return PowerQualityFinishedEventLogObisCode;
    }

    /**
     * @return the {@link #FraudDetectionEventLogObisCode} for the FraudDetection Event Profile
     */
    public ObisCode getFraudDetectionEventLogObisCode() {
        return FraudDetectionEventLogObisCode;
    }

    /**
     * @return the {@link #DemandManagementEventLogObisCode} for the Demand management Event Profile
     */
    public ObisCode getDemandManagementEventLog() {
        return DemandManagementEventLogObisCode;
    }

    /**
     * @return the {@link #CommonEventLogObisCode} for the Common Event Profile
     */
    public ObisCode getCommonEventLog() {
        return CommonEventLogObisCode;
    }

    /**
     * @return the {@link #PowerContractEventLogObisCode} for the PowerContract Event Profile
     */
    public ObisCode getPowerContractEventLog() {
        return PowerContractEventLogObisCode;
    }

    /**
     * @return the {@link #FirmwareEventLogObisCode} for the Firmware Event Profile
     */
    public ObisCode getFirmwareEventLog() {
        return FirmwareEventLogObisCode;
    }

    /**
     * @return the {@link #ObjectSynchronizationEventLogObisCode} for the Object Synchronization Event Profile
     */
    public ObisCode getObjectSynchronizationEventLog() {
        return ObjectSynchronizationEventLogObisCode;
    }

    /**
     * @return the {@link #ActivityCalendarContract1ObisCode} for the ActivityCalendar
     */
    public ObisCode getActivityCalendarContract1ObisCode() {
        return ActivityCalendarContract1ObisCode;
    }

    /**
     * @return the {@link #ActiveSpecialDayContract1ObisCode} for the Active SpecialDay Table
     */
    public ObisCode getActiveSpecialDayContract1ObisCode() {
        return ActiveSpecialDayContract1ObisCode;
    }

    /**
     * @return the {@link #PassiveSpecialDayContract1ObisCode} for the Passive SpecialDay Table
     */
    public ObisCode getPassiveSpecialDayContract1ObisCode() {
        return PassiveSpecialDayContract1ObisCode;
    }

    /**
     * @return the {@link #CurrentActiveRateContract1ObisCode} for the currently active Rate on the ActivityCalendar
     */
    public ObisCode getCurrentActiveRateContract1ObisCode() {
        return CurrentActiveRateContract1ObisCode;
    }

    public ObisCode getActiveCalendarNameObisCode(){
        return ActiveCalendarNameObisCode;
    }

    public ObisCode getActiveLongFirmwareIdentifierACOR() {
        return ActiveLongFirmwareIdentifierACOR;
    }

    public ObisCode getActiveLongFirmwareIdentifierMCOR() {
        return ActiveLongFirmwareIdentifierMCOR;
    }

    /**
     * Enumeration containing the possible {@link com.energyict.dlms.cosem.AssociationLN} Obiscodes.
     * Each clientID has a different AssociationObject.
     */
    private enum AssociationLnObisCodes {

        /**
         * The current_client
         */
        CURRENT_CLIENT(0, AssociationLnCurrentClient),
        /**
         * The management client has all access
         */
        MANAGEMENT_CLIENT(1, AssociationLnManagementClient),
        /**
         * The public client has limited read access
         */
        PUBLIC_CLIENT(16, AssociationLnPublicClient),
        /**
         * The reading client has reading access
         */
        READING_CLIENT(2, AssociationLnReadingClient),
        /**
         * The firmware client has access to firmware related objects (ex. FirmwareUpgrade)
         */
        FIRMWARE_CLIENT(3, AssociationLnFirmwareClient);

        /**
         * The clientId for this association
         */
        private final int clientId;
        /**
         * The obiscode for the {@link com.energyict.dlms.cosem.AssociationLN} object
         */
        private final ObisCode obiscode;
        /**
         * Map containig all the possible instances
         */
        private static Map<Integer, ObisCode> instances;

        /**
         * Private constructor
         *
         * @param clientId                      the ID of the client
         * @param associationLnManagementClient the ObisCode of this clients' associationLN object
         */
        private AssociationLnObisCodes(int clientId, ObisCode associationLnManagementClient) {
            this.clientId = clientId;
            this.obiscode = associationLnManagementClient;
            getInstances().put(this.clientId, this.obiscode);
        }

        /**
         * Return the obisCode of the {@link com.energyict.dlms.cosem.AssociationLN} object for the given {@link #clientId}
         *
         * @param clientId the ID of the client
         * @return the requested ObisCode
         */
        public static ObisCode forClient(int clientId) {
            ObisCode oc = getInstances().get(clientId);
            return oc != null ? oc : null;
        }

        /**
         * @return an instance of all the clients
         */
        private static Map<Integer, ObisCode> getInstances() {
            if (instances == null) {
                instances = new HashMap<Integer, ObisCode>(5);
            }
            return instances;
        }
    }
}
