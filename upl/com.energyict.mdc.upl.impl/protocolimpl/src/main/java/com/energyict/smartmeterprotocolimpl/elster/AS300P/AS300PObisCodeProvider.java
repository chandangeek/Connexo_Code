package com.energyict.smartmeterprotocolimpl.elster.AS300P;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.generic.CommonObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.MultipleClientRelatedObisCodes;

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
public class AS300PObisCodeProvider implements CommonObisCodeProvider {

    public static final ObisCode ClockObisCode = ObisCode.fromString("0.0.1.0.0.255");
    public static final ObisCode ClockSynchronizationObisCode = ObisCode.fromString("0.0.96.2.12.255");
    public static final ObisCode SerialNumberObisCode = ObisCode.fromString("0.0.96.1.0.255");
    public static final ObisCode LoadProfileP1 = ObisCode.fromString("1.0.99.1.0.255");
    public static final ObisCode LoadProfileP2 = ObisCode.fromString("1.0.99.1.1.255");
    public static final ObisCode LoadProfileDaily = ObisCode.fromString("0.0.98.2.0.255");
    public static final ObisCode LoadProfileMonthly = ObisCode.fromString("0.0.98.1.0.255");
    public static final ObisCode LoadProfileBlockDaily = ObisCode.fromString("0.0.98.2.1.255");       //Block profile data   ==> E = 1
    public static final ObisCode LoadProfileBlockMonthly = ObisCode.fromString("0.0.98.1.1.255");     //Block profile data   ==> E = 1
    public static final ObisCode AssociationLnCurrentClient = ObisCode.fromString("0.0.40.0.0.255");
    public static final ObisCode AssociationLnPublicClient = ObisCode.fromString("0.0.40.0.1.255");
    public static final ObisCode AssociationLnDataCollectionClient = ObisCode.fromString("0.0.40.0.2.255");
    public static final ObisCode AssociationLnExtDataCollectionClient = ObisCode.fromString("0.0.40.0.3.255");
    public static final ObisCode AssociationLnManagementClient = ObisCode.fromString("0.0.40.0.4.255");
    public static final ObisCode AssociationLnFirmwareClient = ObisCode.fromString("0.0.40.0.5.255");
    public static final ObisCode AssociationLnManufactureClient = ObisCode.fromString("0.0.40.0.0.255");
    public static final ObisCode FirmwareVersionObisCode = ObisCode.fromString("1.0.0.2.0.255");
    public static final ObisCode MIDCheckSumObisCode = ObisCode.fromString("1.0.0.2.8.255");
    public static final ObisCode FormerFirmwareVersionObisCode = ObisCode.fromString("1.0.0.1.0.255");
    public static final ObisCode E_OperationalFirmwareVersionObisCode = ObisCode.fromString("1.3.0.2.0.255");
    public static final ObisCode ActiveLongFirmwareIdentifierACOR = ObisCode.fromString("1.2.0.2.0.255");
    public static final ObisCode ActiveLongFirmwareIdentifierMCOR = ObisCode.fromString("1.1.0.2.0.255");

    public static final ObisCode ReferenceTime = ObisCode.fromString("0.128.3.6.0.255");
    public static final ObisCode FrameCounterPublicClient = ObisCode.fromString("0.0.43.1.1.255");
    public static final ObisCode FrameCounterDataCollectionClient = ObisCode.fromString("0.0.43.1.2.255");
    public static final ObisCode FrameCounterExtDataCollectionClient = ObisCode.fromString("0.0.43.1.3.255");
    public static final ObisCode FrameCounterManagementClient = ObisCode.fromString("0.0.43.1.4.255");
    public static final ObisCode FrameCounterFirmwareClient = ObisCode.fromString("0.0.43.1.5.255");
    public static final ObisCode FrameCounterManufactureClient = ObisCode.fromString("0.0.43.1.255.255");

    public static final ObisCode clockShiftEventLimit = ObisCode.fromString("1.0.0.9.11.255");
    public static final ObisCode clockShiftInvalidLimit = ObisCode.fromString("1.1.94.34.1.255");
    public static final ObisCode clockSyncWindow = ObisCode.fromString("1.0.0.9.9.255");

    public static final ObisCode LastBillingResetTimeStamp = ObisCode.fromString("0.0.0.1.11.255");
    public static final ObisCode BillingResetLockoutTime = ObisCode.fromString("0.1.94.34.40.255");
    public static final ObisCode DaysSinceBillingReset = ObisCode.fromString("1.0.0.9.0.255");

    public static final ObisCode ErrorRegister = ObisCode.fromString("0.0.97.97.0.255");
    public static final ObisCode ErrorFilter = ObisCode.fromString("0.0.97.97.10.255");
    public static final ObisCode AlarmRegister = ObisCode.fromString("0.0.97.98.0.255");
    public static final ObisCode AlarmFilter = ObisCode.fromString("0.0.97.98.10.255");

    public static final ObisCode InstantaneousEnergyValuesObisCode = ObisCode.fromString("0.0.21.0.6.255");
    public static final ObisCode RefVoltagePQObisCode = ObisCode.fromString("1.0.0.6.4.255");
    public static final ObisCode NrOfVoltageSagsAvgVoltageObisCode = ObisCode.fromString("1.0.94.34.90.255");
    public static final ObisCode DurationVoltageSagsAvgVoltageObisCode = ObisCode.fromString("1.0.93.34.91.255");
    public static final ObisCode NrOfVoltageSwellsAvgVoltageObisCode = ObisCode.fromString("1.0.94.34.92.255");
    public static final ObisCode DurationVoltageSwellsAvgVoltageObisCode = ObisCode.fromString("1.0.93.34.93.255");

    public static final ObisCode ActivityCalendarObisCode = ObisCode.fromString("0.0.13.0.0.255");
    public static final ObisCode ActiveSpecialDayObisCode = ObisCode.fromString("0.0.11.0.0.255");
    public static final ObisCode PassiveSpecialDayObisCode = ObisCode.fromString("0.1.11.0.0.255");
    public static final ObisCode ActiveScriptTableObisCode = ObisCode.fromString("0.0.10.0.100.255");
    public static final ObisCode PassiveScriptTableObisCode = ObisCode.fromString("0.0.10.1.100.255");
    public static final ObisCode PassiveEmergencyScriptObisCode = ObisCode.fromString("0.1.93.44.10.255");
    public static final ObisCode TariffInformationImportEnergy = ObisCode.fromString("0.0.63.1.1.255");
    public static final ObisCode TariffInformationExportEnergy = ObisCode.fromString("0.0.63.1.2.255");
    public static final ObisCode TariffRateLabelImportEnergy = ObisCode.fromString("0.0.63.0.1.255");
    public static final ObisCode TariffRateLabelExportEnergy = ObisCode.fromString("0.0.63.0.2.255");
    public static final ObisCode RegisterActivationForEnergyType = ObisCode.fromString("0.0.14.0.255.255"); // E-field obisCode is wildcard!

    public static final ObisCode BlockTariffConfiguration = ObisCode.fromString("0.0.13.1.0.255");
    public static final ObisCode BlockScriptTable = ObisCode.fromString("0.0.10.255.100.255");          // D-field obisCode is wildcard!
    public static final ObisCode BlockRegisterActivation = ObisCode.fromString("0.1.14.0.255.255");     // E-field obisCode is wildcard !
    public static final ObisCode RegisterActivationBlockMonitors = ObisCode.fromString("0.1.14.0.1.255");
    public static final ObisCode PassiveBlockMonitorsPassive = ObisCode.fromString("0.0.16.1.255.255"); // E-field obisCode is wildcard!

    public static final ObisCode ChangeOfTenancy = ObisCode.fromString("0.0.65.0.0.255");
    public static final ObisCode ChangeOfSupplierImportEnergy = ObisCode.fromString("0.0.65.1.1.255");
    public static final ObisCode ChangeOfSupplierExportEnergy = ObisCode.fromString("0.0.65.1.2.255");
    public static final ObisCode COTSPredefinedScriptTable = ObisCode.fromString("0.0.10.0.64.255");

    public static final ObisCode LoadProfileStatusP1 = ObisCode.fromString("0.0.96.10.1.255");
    public static final ObisCode LoadProfileStatusP2 = ObisCode.fromString("0.0.96.10.2.255");

    public static final ObisCode Currency = ObisCode.fromString("0.0.61.1.0.255");
    public static final ObisCode StandingCharge = ObisCode.fromString("0.0.61.2.0.255");

    public static final ObisCode EngineerMenuPIN = ObisCode.fromString("0.128.13.0.0.255");

    public static final ObisCode ImageActivationScheduler = ObisCode.fromString("0.0.15.0.2.255");
    public static final ObisCode DisconnectControl = ObisCode.fromString("0.0.96.3.10.255");

    public static final ObisCode PriceMatrixImportActiveEnergy = ObisCode.fromString("0.0.61.0.1.255");
    public static final ObisCode PriceMatrixExportActiveEnergy = ObisCode.fromString("0.0.61.0.2.255");

    public static final ObisCode StandardEventLogObisCode = ObisCode.fromString("0.0.99.98.0.255");
    public static final ObisCode FraudDetectionEventLogObisCode = ObisCode.fromString("0.0.99.98.1.255");
    public static final ObisCode DisconnectControlLogObisCode = ObisCode.fromString("0.0.99.98.2.255");
    public static final ObisCode FirmwareEventLogObisCode = ObisCode.fromString("0.0.99.98.3.255");
    public static final ObisCode PowerQualityEventLogObisCode = ObisCode.fromString("0.0.99.98.4.255");
    public static final ObisCode PowerFailureEventLogObisCode = ObisCode.fromString("0.0.99.97.0.255");
    public static final ObisCode CommunicationsEventLogObisCode = ObisCode.fromString("0.0.99.98.5.255");
    public static final ObisCode TariffUpdateEventLogObisCode = ObisCode.fromString("0.0.99.98.31.255");
    public static final ObisCode SynchronisationEventLogObisCode = ObisCode.fromString("0.0.99.98.30.255");
    public static final ObisCode PrepaymentEventLogObisCode = ObisCode.fromString("0.0.99.98.6.255");
    public static final ObisCode COTSEventLogObisCode = ObisCode.fromString("0.0.99.98.11.255");

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
        return getAssociationLnObisCode(MultipleClientRelatedObisCodes.MANAGEMENT_CLIENT.getClientId());
    }

    /**
     * @param clientId the used clientId for this association
     * @return the {@link com.energyict.smartmeterprotocolimpl.eict.ukhub.common.MultipleClientRelatedObisCodes#frameCounterObisCode}
     */
    public ObisCode getFrameCounterObisCode(int clientId){
        return MultipleClientRelatedObisCodes.frameCounterForClient(clientId);
    }

    /**
     * @param clientId the used clientId for this association
     * @return the {@link com.energyict.smartmeterprotocolimpl.eict.ukhub.common.MultipleClientRelatedObisCodes#associationObisCode} for the {@link com.energyict.dlms.cosem.AssociationLN} object
     */
    public ObisCode getAssociationLnObisCode(int clientId) {
        return MultipleClientRelatedObisCodes.associationLNForClient(clientId);
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
     * @return the {@link #ActivityCalendarObisCode} for the ActivityCalendar
     */
    public ObisCode getActivityCalendarObisCode() {
        return ActivityCalendarObisCode;
    }

    /**
     * @return the {@link #ActiveSpecialDayObisCode} for the Active SpecialDay Table
     */
    public ObisCode getActiveSpecialDayObisCode() {
        return ActiveSpecialDayObisCode;
    }

    /**
     * @return the {@link #PassiveSpecialDayObisCode} for the Passive SpecialDay Table
     */
    public ObisCode getPassiveSpecialDayObisCode() {
        return PassiveSpecialDayObisCode;
    }

    public ObisCode getScriptTablePassiveObisCode() {
        return PassiveScriptTableObisCode;
    }

    public ObisCode getScriptTableObisCode() {
        return ActiveScriptTableObisCode;
    }

    public ObisCode getActiveLongFirmwareIdentifierACOR() {
        return ActiveLongFirmwareIdentifierACOR;
    }

    public ObisCode getActiveLongFirmwareIdentifierMCOR() {
        return ActiveLongFirmwareIdentifierMCOR;
    }

    public ObisCode getStandardEventLogObisCode() {
        return StandardEventLogObisCode;
    }

    public ObisCode getPowerQualityEventLogObisCode() {
        return PowerQualityEventLogObisCode;
    }

    public ObisCode getPowerFailureEventLogObisCode() {
        return PowerFailureEventLogObisCode;
    }

    public ObisCode getFraudDetectionEventLogObisCode() {
        return FraudDetectionEventLogObisCode;
    }

    public ObisCode getCommonEventLogObisCode() {
        return CommunicationsEventLogObisCode;
    }

    public ObisCode getFirmwareEventLogObisCode() {
        return FirmwareEventLogObisCode;
    }

    public ObisCode getDisconnectControlLogObisCode() {
        return DisconnectControlLogObisCode;
    }
}