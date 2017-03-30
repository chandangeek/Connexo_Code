/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public enum PrimeEvents {

    /**
     * ************************************ Group 1 Events **************************************
     */
    EventLogCleared(255, MeterEvent.CLEAR_DATA, 0, "Event log cleared", "Event log has been cleared"),
    RebootWithLossOfData(1, MeterEvent.OTHER, 1, "Boots and Power Fails", "Reboot with loss of data"),
    RebootWithoutLossOfData(2, MeterEvent.OTHER, 1, "Boots and Power Fails", "Reboot without loss of data"),
    PowerFail(3, MeterEvent.POWERDOWN, 1, "Boots and Power Fails", "Power Fail"),
    PowerFailPhase1(4, MeterEvent.PHASE_FAILURE, 1, "Boots and Power Fails", "Power fail. Phase 1"),
    PowerFailPhase2(5, MeterEvent.PHASE_FAILURE, 1, "Boots and Power Fails", "Power fail. Phase 2"),
    PowerFailPhase3(6, MeterEvent.PHASE_FAILURE, 1, "Boots and Power Fails", "Power fail. Phase 3"),
    NeutralLoss(7, MeterEvent.PHASE_FAILURE, 1, "Boots and Power Fails", "Neutral loss"),
    LowBattery(8, MeterEvent.BATTERY_VOLTAGE_LOW, 1, "Boots and Power Fails", "Low Battery"),
    CriticalInternalError(9, MeterEvent.FATAL_ERROR, 1, "Boots and Power Fails", "Critical internal Error"),
    EndOfPowerFailPhase1(21, MeterEvent.POWERUP, 1, "Boots and Power Fails", "End of power fail. Phase 1"),
    EndOfPowerFailPhase2(22, MeterEvent.POWERUP, 1, "Boots and Power Fails", "End of power fail. Phase 2"),
    EndOfPowerFailPhase3(23, MeterEvent.POWERUP, 1, "Boots and Power Fails", "End of power fail. Phase 3"),

    ChangeWinterSummer(24, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, 1, "", "Official change local time Winter to Summer"),
    ChangeSummerWinter(25, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, 1, "", "Official change local time Summer to Winter"),

    RegisterParametersChange(30, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Register parameters change"),
    CommPortParametersChange(31, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Communication ports parameters change"),
    ReadingPasswordParametersChange(32, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Reading password change"),
    ParametrizationPasswordParametersChange(33, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Parametrization password change"),
    FirmwarePasswordParametersChange(34, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Firmware password change"),
    BatteryClearedParametersChange(35, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Battery cleared change"),
    AutomaticDSTParametersChange(36, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Automatic DST change"),
    MinTimeBillingEndParametersChange(37, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Minimum time between billing end change"),
    LoadProfileCapturePeriodParametersChange(38, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Load Profile capture period change"),
    TransformerRatioParametersChange(39, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Transform ratio change"),
    ClockSyncModeParametersChange(40, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Clock synchronization change"),
    ProgramLabelParametersChange(41, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Program label  change"),
    ManualClosuresActivationStatusParametersChange(42, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Manual closures activation status change"),
    OutputMagnitudeParametersChange(43, MeterEvent.CONFIGURATIONCHANGE, 1, "Change of Parameters", "Output magnitude  change"),

    ClosureCommandContract1(44, MeterEvent.OTHER, 1, "Contract 1", "Closure command prompted contract 1"),
    ParameterContract1Changed(45, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 1", "Parameters contract 1 changed"),
    PowerContract1Modified(46, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 1", "Power contract 1 modified"),
    SpecialDayTableContract1PassiveChanged(47, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 1", "Special days table contract 1 passive change"),
    SeasonsTableContract1PassiveChanged(48, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 1", "Seasons table contract 1 passive change"),
    Contract1PassiveCleared(49, MeterEvent.CLEAR_DATA, 1, "Contract 1", "Contract 1 passive cleared"),
    AutomaticBillingEndContract1PassiveChanged(50, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 1", "Automatic billing end contract 1 passive change"),
    ActivationDateContract1PassiveChanged(51, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 1", "Activation date contract 1 passive change"),

    ClosureCommandContract2(52, MeterEvent.OTHER, 1, "Contract 2", "Closure command prompted contract 2"),
    ParameterContract2Changed(53, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 2", "Parameters contract 2 changed"),
//    PowerContract2Modified(46, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 2", "Power contract 2 modified"),
    SpecialDayTableContract2PassiveChanged(54, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 2", "Special days table contract 2 passive change"),
    SeasonsTableContract2PassiveChanged(55, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 2", "Seasons table contract 2 passive change"),
    Contract2PassiveCleared(56, MeterEvent.CLEAR_DATA, 1, "Contract 2", "Contract 2 passive cleared"),
    AutomaticBillingEndContract2PassiveChanged(57, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 2", "Automatic billing end contract 2 passive change"),
    ActivationDateContract2PassiveChanged(58, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 2", "Activation date contract 2 passive change"),

    ClosureCommandContract3(59, MeterEvent.OTHER, 1, "Contract 3", "Closure command prompted contract 3"),
    ParameterContract3Changed(60, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 3", "Parameters contract 3 changed"),
//    PowerContract3Modified(46, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 3", "Power contract 3 modified"),
    SpecialDayTableContract3PassiveChanged(61, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 3", "Special days table contract 3 passive change"),
    SeasonsTableContract3PassiveChanged(62, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 3", "Seasons table contract 3 passive change"),
    Contract3PassiveCleared(63, MeterEvent.CLEAR_DATA, 1, "Contract 3", "Contract 3 passive cleared"),
    AutomaticBillingEndContract3PassiveChanged(64, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 3", "Automatic billing end contract 3 passive change"),
    ActivationDateContract3PassiveChanged(65, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract 3", "Activation date contract 3 passive change"),

    ManualBillingEndB(66, MeterEvent.BILLING_ACTION, 1, "Contract 1, 2 and 3", "Manual billing end (button)"),

    TimeThresholdVoltageSagsSwellsChanged(90, MeterEvent.CONFIGURATIONCHANGE, 1, "Quality parameters change", "Time threshold for voltage sags and swells changed"),
    TimeThresholdLongPowerFailuresChanged(91, MeterEvent.CONFIGURATIONCHANGE, 1, "Quality parameters change", "Time threshold for long power failures (T') changed"),
    NominalVoltageChanged(92, MeterEvent.CONFIGURATIONCHANGE, 1, "Quality parameters change", "Nominal voltage (Vn) changed"),
    MaxVoltageLevelChanged(93, MeterEvent.CONFIGURATIONCHANGE, 1, "Quality parameters change", "Max. voltage level changed (+V)"),
    MinVoltageLevelChanged(94, MeterEvent.CONFIGURATIONCHANGE, 1, "Quality parameters change", "Min. voltage level changed (-V)"),
    DifMinAndNoVoltageChanged(95, MeterEvent.CONFIGURATIONCHANGE, 1, "Quality parameters change", "Difference between min voltage and no voltage changed"),

    ContractPowerChanged(96, MeterEvent.CONFIGURATIONCHANGE, 1, "Contract Power", "Contract Power changed"),
    FirmwareChanged(97, MeterEvent.CONFIGURATIONCHANGE, 1, "Firmware", "Firmware changed"),
    ClockSynchronization(98, MeterEvent.SETCLOCK, 1, "Synchronization", "Clock synchronization"),

    /**
     * ************************************ Group 2 Events **************************************
     */
    ManualConnection(1, MeterEvent.MANUAL_CONNECTION, 2, "Disconnector control", "Manual connection"),
    RemoteDisconnection(2, MeterEvent.REMOTE_DISCONNECTION, 2, "Disconnector control", "Remote disconnection (command)"),
    RemoteConnection(3, MeterEvent.REMOTE_CONNECTION, 2, "Disconnector control", "Remote connection (command)"),
    PowerContractControlDisconnection(4, MeterEvent.LOCAL_DISCONNECTION, 2, "Disconnector control", "Power contract control disconnection"),
    PowerControlConnection(5, MeterEvent.REMOTE_CONNECTION, 2, "Disconnector control", "Power control connection"),
    NoTripCurrentExceededByBlockade(6, MeterEvent.OTHER, 2, "Disconnector control", "No trip current exceeded by blockade"),
    DisconnectorEnabled(7, MeterEvent.OTHER, 2, "Disconnector control", "Disconnector enabled"),
    DisconnectorDisabled(8, MeterEvent.OTHER, 2, "Disconnector control", "Disconnector disabled"),
    ResidualPowerControlDisconnection(9, MeterEvent.LOCAL_DISCONNECTION, 2, "Disconnector control", "Residual power control disconnection"),
    ResidualPowerDeactivationControlConnection(10, MeterEvent.OTHER, 2, "Disconnector control", "Residual power deactivation control connection"),
    ResidualPowerControlConnection(11, MeterEvent.OTHER, 2, "Disconnector control", "Residual power control connection"),

    /**
     * ************************************ Group 3 Events **************************************
     */
    UnderLimitVoltageBetweenPhasesAverage(1, MeterEvent.VOLTAGE_SAG, 3, "Quality non finished events", "Under limit voltage between phases average"),
    UnderLimitVoltageL1(2, MeterEvent.VOLTAGE_SAG, 3, "Quality non finished events", "Under limit voltage L1"),
    UnderLimitVoltageL2(3, MeterEvent.VOLTAGE_SAG, 3, "Quality non finished events", "Under limit voltage L2"),
    UnderLimitVoltageL3(4, MeterEvent.VOLTAGE_SAG, 3, "Quality non finished events", "Under limit voltage L3"),
    OverLimitVoltageBetweenPhasesAverage(5, MeterEvent.VOLTAGE_SWELL, 3, "Quality non finished events", "Over limit voltage between phases average"),
    OverLimitVoltageL1(6, MeterEvent.VOLTAGE_SWELL, 3, "Quality non finished events", "Over limit voltage L1"),
    OverLimitVoltageL2(7, MeterEvent.VOLTAGE_SWELL, 3, "Quality non finished events", "Over limit voltage L2"),
    OverLimitVoltageL3(8, MeterEvent.VOLTAGE_SWELL, 3, "Quality non finished events", "Over limit voltage L3"),
    LongPowerFailureAllPhases(9, MeterEvent.PHASE_FAILURE, 3, "Quality non finished events", "Long power failure for all phases"),
    LongPowerFailureL1(10, MeterEvent.PHASE_FAILURE, 3, "Quality non finished events", "Long power failure L1"),
    LongPowerFailureL2(11, MeterEvent.PHASE_FAILURE, 3, "Quality non finished events", "Long power failure L2"),
    LongPowerFailureL3(12, MeterEvent.PHASE_FAILURE, 3, "Quality non finished events", "Long power failure L3"),
    UnderLimitVoltageBetweenPhasesAverage_2(13, MeterEvent.VOLTAGE_SAG, 3, "Quality finished events", "Under limit voltage between phases average"),
    UnderLimitVoltageL1_2(14, MeterEvent.VOLTAGE_SAG, 3, "Quality finished events", "Under limit voltage L1"),
    UnderLimitVoltageL2_2(15, MeterEvent.VOLTAGE_SAG, 3, "Quality finished events", "Under limit voltage L2"),
    UnderLimitVoltageL3_2(16, MeterEvent.VOLTAGE_SAG, 3, "Quality finished events", "Under limit voltage L3"),
    OverLimitVoltageBetweenPhasesAverage_2(17, MeterEvent.VOLTAGE_SWELL, 3, "Quality finished events", "Over limit voltage between phases average"),
    OverLimitVoltageL1_2(18, MeterEvent.VOLTAGE_SWELL, 3, "Quality finished events", "Over limit voltage L1"),
    OverLimitVoltageL2_2(19, MeterEvent.VOLTAGE_SWELL, 3, "Quality finished events", "Over limit voltage L2"),
    OverLimitVoltageL3_2(20, MeterEvent.VOLTAGE_SWELL, 3, "Quality finished events", "Over limit voltage L3"),
    LongPowerFailureAllPhases_2(21, MeterEvent.PHASE_FAILURE, 3, "Quality finished events", "Long power failure for all phases"),
    LongPowerFailureL1_2(22, MeterEvent.PHASE_FAILURE, 3, "Quality finished events", "Long power failure L1"),
    LongPowerFailureL2_2(23, MeterEvent.PHASE_FAILURE, 3, "Quality finished events", "Long power failure L2"),
    LongPowerFailureL3_2(24, MeterEvent.PHASE_FAILURE, 3, "Quality finished events", "Long power failure L3"),

    /**
     * ************************************ Group 4 Events **************************************
     */
    CoverOpened(1, MeterEvent.COVER_OPENED, 4, "Fraud", "Cover opened"),
    CoverClosed(2, MeterEvent.TAMPER, 4, "Fraud", "Cover closed"),
    StrongDCFieldDetected(3, MeterEvent.STRONG_DC_FIELD_DETECTED, 4, "Fraud", "Strong DC field detected"),
    NoStrongDCFieldAnymore(4, MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, 4, "Fraud", "No more strong DC field anymore"),
    CurrentWithoutVoltage(5, MeterEvent.OTHER, 4, "Fraud", "Current without voltage"),
    IntrusionDetection(6, MeterEvent.TAMPER, 4, "Fraud", "Instrusion detection"),

    /**
     * ************************************ Group 5 Events **************************************
     */
    ReceptionOrderManagementCriticResidualDemand(1, MeterEvent.OTHER, 5, "Demand management", "Reception order management critic residual demand"),
    ReceptionOrderManagementCriticPrcDecreaseDemand(2, MeterEvent.OTHER, 5, "Demand management", "Reception order management critic % decrease demand"),
    ReceptionOrderManagementDemandAbsoluteValueCriticDemand(3, MeterEvent.OTHER, 5, "Demand management", "Reception order management demand absolute value critic demand"),
    ReceptionOrderManagementNoCriticResidualDemand(4, MeterEvent.OTHER, 5, "Demand management", "Reception order management no critic residual demand"),
    ClientAcceptenceManagementNoCriticResidualDemand(5, MeterEvent.OTHER, 5, "Demand management", "Client acceptance management no critic residual demand"),
    ClientRejectionManagementNoCriticResidualDemand(6, MeterEvent.OTHER, 5, "Demand management", "Client rejection management no critical residual demand"),
    ReceptionOrderManagementNoCriticDemandPrcDecrease(7, MeterEvent.OTHER, 5, "Demand management", "Reception order management no critic demand % decrease"),
    ClientAcceptanceManagementNoCriticDemandPrcDecrease(8, MeterEvent.OTHER, 5, "Demand management", "Client acceptance management no critic demand % decrease"),
    RejectionManagementNoCriticDemandPrcDecrease(9, MeterEvent.OTHER, 5, "Demand management", "Rejection management no critic demand % decrease"),
    ReceptionOrderManagementNoCriticiAbsoluteDemand(10, MeterEvent.OTHER, 5, "Demand management", "Reception order management no critic absolute demand"),
    ClientAcceptanceManagementNoCriticAbsoluteDemand(11, MeterEvent.OTHER, 5, "Demand management", "Client acceptance management no critic absolute demand"),
    ClientRejectionManagementNoCriticAbsoluteDemand(12, MeterEvent.OTHER, 5, "Demand management", "Client rejection management no critic absolute demand"),
    SubscribedResidualDemandChanged(13, MeterEvent.OTHER, 5, "Demand management", "Subscribed residual demand changed"),
    BeginResidualDemand(14, MeterEvent.OTHER, 5, "Demand management", "Begin residual demand"),
    EndResidualDemand(15, MeterEvent.OTHER, 5, "Demand management", "End residual demand"),
    BeginDecreasePrcSubscribedDemand(16, MeterEvent.OTHER, 5, "Demand management", "Begin decrease % subscribed demand"),
    EndDecreasePrcSubscribedDemand(17, MeterEvent.OTHER, 5, "Demand management", "End decrease % subscribed demand"),
    BeginReductionAbsolutePower(18, MeterEvent.OTHER, 5, "Demand management", "Begin reduction absolute power"),
    EndReductionAbsolutePower(19, MeterEvent.OTHER, 5, "Demand management", "End reduction absolute power"),
    DemandCloseToContractPower(20, MeterEvent.OTHER, 5, "Demand management", "DemandCloseToContractPower"),

    /**
     * ************************************ Group 6 Events **************************************
     */
    BeginCommunicationPLCPort(1, MeterEvent.OTHER, 6, "Frequent occurrence-Common", "Begin communication PLC Port"),
    EndCommunicationPLCPort(2, MeterEvent.OTHER, 6, "Frequent occurrence-Common", "End communication PLC Port"),
    BeginCommunicationOpticalPort(3, MeterEvent.OTHER, 6, "Frequent occurrence-Common", "Begin communication Optical Port"),
    EndCommunicationOpticalPort(4, MeterEvent.OTHER, 6, "Frequent occurrence-Common", "End communication Optical Port"),
    BeginCommunicationSerialPort(5, MeterEvent.OTHER, 6, "Frequent occurrence-Common", "Begin communication Serial Port"),
    EndCommunicationSerialPort(6, MeterEvent.OTHER, 6, "Frequent occurrence-Common", "End communication Serial Port"),

    /**
     * ************************************ Unknown Events **************************************
     */
    UnKnownEvent(0, MeterEvent.OTHER, 0, "Unknown event group", "Unknown Event");

    /**
     * The eventcode from the device
     */
    private final int eventId;
    /**
     * The EIS {@link com.energyict.protocol.MeterEvent} code
     */
    private final int eiserverCode;
    /**
     * The group of which the {@link #eventId} belongs to
     */
    private final int group;
    /**
     * The basic group description
     */
    private final String groupDescription;
    /**
     * The event description
     */
    private final String eventDescription;
    private static List<PrimeEvents> instances;

    /**
     * Constructs a static list of PrimeEvents, easily to search in
     * @return the list of available PrimeEvents
     */
    private static List<PrimeEvents> getInstances() {
        if (instances == null) {
            instances = new ArrayList<PrimeEvents>();
        }
        return instances;
    }

    /**
     * Private constructor for the enumeration
     *
     * @param eventId          the eventCode from the device
     * @param eiserverCode     the EIS {@link com.energyict.protocol.MeterEvent} code
     * @param group            the group of which the eventId belongs to
     * @param description      the groupDescription of the group according to the documentation
     * @param eventDescription the description of the event according to the documentation
     */
    private PrimeEvents(int eventId, int eiserverCode, int group, String description, String eventDescription) {
        this.eventId = eventId;
        this.eiserverCode = eiserverCode;
        this.group = group;
        this.groupDescription = description;
        this.eventDescription = eventDescription;
        getInstances().add(this);
    }

    /**
     * Find an {@link PrimeEvents} in the enumeration based on the given parameters
     *
     * @param protocolEventId the eventCode returned from the device
     * @param eventGroup      the group of which the protocolEventId belongs to
     * @return the requested ApolloEvent
     */
    public static PrimeEvents find(int protocolEventId, int eventGroup) {
        Iterator it = getInstances().iterator();
        while (it.hasNext()) {
            PrimeEvents ae = (PrimeEvents) it.next();
            if (ae.getGroup() == eventGroup && ae.getProtocolEventId() == protocolEventId) {
                return ae;
            }
        }
        return UnKnownEvent;
    }

    /**
     * @return the index of the group
     */
    public int getGroup() {
        return this.group;
    }

    /**
     * @return the event code returned from the device
     */
    public int getProtocolEventId() {
        return this.eventId;
    }

    /**
     * @return the EIS {@link com.energyict.protocol.MeterEvent} code
     */
    public int getEIServerCode() {
        return this.eiserverCode;
    }

    /**
     * @return the description of the event
     */
    public String getDescription() {
        return this.eventDescription;
    }

    /**
     * @return the groupDescription of the group
     */
    public String getGroupDescription() {
        return this.groupDescription;
    }
}
