/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.cim;

/**
 * Created by IntelliJ IDEA.
 * User: jbr
 * Date: 16-aug-2011
 * Time: 16:25:05
 */
public enum EndDeviceSubdomain  implements CimMnemonicProvider {

    NOT_APPLICABLE(0, "NotApplicable"),
    ACCESS(1, "Access"),
    ALLOCATION(2, "Allocation"),
    AUTDPROCESS(3, "AUTDProcess"),
    FREQUENCY(4, "Frequency"),
    AUTOREGISTRATION(5, "AutoRegistration"),
    CURRENT(6, "Current"),
    AUTOTIME(7, "AutoTime"),
    CREDIT(8, "Credit"),
    PRICING(9, "Pricing"),
    IDENTITY(10, "Identity"),
    MAINTMODE(11, "MaintMode"),
    METERINGMODE(12, "MeteringMode"),
    BTU(13, "BTU"),
    BUFFER(14, "Buffer"),
    CABLE(15, "Cable"),
    CALCULATION(16, "Calculation"),
    STATUS(17, "Status"),
    CALIBRATION(18, "Calibration"),
    TESTMODE(19, "TestMode"),
    CERTIFICATE(21, "Certificate"),
    CHARGE(22, "Charge"),
    CONSTANTS(23, "Constants"),
    PASSWORD(24, "Password"),
    PHASE(25, "Phase"),
    CONTROLPOINT(26, "ControlPoint"),
    POWERFACTOR(27, "PowerFactor"),
    POWERQUALITY(28, "PowerQuality"),
    COVER(29, "Cover"),
    CRC(30, "CRC"),
    DATA(31, "Data"),
    SECURITYKEY(32, "SecurityKey"),
    DATALOG(33, "DataLog"),
    DATE(34, "Date"),
    DAY(35, "Day"),
    DECRYPTION(36, "Decryption"),
    VOLTAGE(38, "Voltage"),
    CONCENTRATION(39, "Concentration"),
    ENCODER(40, "Encoder"),
    ENCODERREGISTER(41, "EncoderRegister"),
    EPROM(42, "EPROM"),
    EVENT(43, "Event"),
    EVENTLOG(44, "EventLog"),
    EWM(45, "EWM"),
    FEATURE(46, "Feature"),
    FIRMWARERESET(47, "FirmwareReset"),
    FLOW(48, "Flow"),
    FPV(49, "FPV"),
    FRAMES(50, "Frames"),
    GCANALYZER(51, "GCAnalyzer"),
    HEADENDSYSTEM(52, "HeadEndSystem"),
    HISTORYLOG(53, "HistoryLog"),
    HMAC(54, "HMAC"),
    INPUT(55, "Input"),
    DAYLIGHTSAVINGSTIME(56, "DaylightSavingsTime"),
    INSTALLDATE(57, "InstallDate"),
    INTELLIGENTREGISTER(58, "IntelligentRegister"),
    INTERVAL(59, "Interval"),
    IO(60, "IO"),
    LANADDRESS(61, "LANAddress"),
    LASTREAD(62, "LastRead"),
    LIST(63, "List"),
    LISTPOINTERS(64, "ListPointers"),
    LOGIN(65, "Login"),
    MAGNETICSWITCH(66, "MagneticSwitch"),
    MEASUREMENT(67, "Measurement"),
    MESH(68, "Mesh"),
    MOBILE(69, "Mobile"),
    MOLPCT(70, "MOL%"),
    NETWORKID(71, "NetworkId"),
    NVRAM(72, "NVRAM"),
    WINDOW(73, "Window"),
    ASSOCIATION(74, "Association"),
    PARAMETER(75, "Parameter"),
    PARENTDEVICE(76, "ParentDevice"),
    PARITY(77, "Parity"),
    PHASESEQUENCE(78, "PhaseSequence"),
    PHASEVOLTAGE(79, "PhaseVoltage"),
    POWERQUALITYRECORDING(80, "PowerQualityRecording"),
    PREPAYMENTCREDIT(81, "PrepaymentCredit"),
    PROCESSOR(82, "Processor"),
    PROGRAM(83, "Program"),
    PULSE(84, "Pulse"),
    RAM(85, "RAM"),
    RATE(86, "Rate"),
    READINGS(87, "Readings"),
    RECOVERY(88, "Recovery"),
    REGISTER(89, "Register"),
    REGISTRATION(90, "Registration"),
    RELAY(91, "Relay"),
    ROM(92, "ROM"),
    ROTATION(93, "Rotation"),
    RTP(94, "RTP"),
    SCHEDULE(95, "Schedule"),
    SECONDARYCREDIT(96, "SecondaryCredit"),
    HOLIDAY(97, "Holiday"),
    SECUREDREGISTER(98, "SecuredRegister"),
    SECUREDTABLE(99, "SecuredTable"),
    SELFTEST(100, "SelfTest"),
    SETPOINT(101, "SetPoint"),
    SIGMATICMESSAGE(102, "SigmaticMessage"),
    SIGNATURE(103, "Signature"),
    SIGNATURELENGTH(104, "SignatureLength"),
    SIGNATURETIMESTAMP(105, "SignatureTimestamp"),
    SIGNATUREUSAGE(106, "SignatureUsage"),
    STANDARDTIME(107, "StandardTime"),
    STANDBYMODE(108, "StandbyMode"),
    STORAGE(109, "Storage"),
    TABLE(110, "Table"),
    TEST(111, "Test"),
    TEXTMESSAGE(112, "TextMessage"),
    TIER(113, "Tier"),
    TIME(114, "Time"),
    TIMERESET(115, "TimeReset"),
    TIMESYNC(116, "TimeSync"),
    TIMEVARIANCE(117, "TimeVariance"),
    TIMEZONE(118, "TimeZone"),
    SECURITYKEYLENGTH(119, "SecurityKeyLength"),
    SECURITYKEYVERSION(120, "SecurityKeyVersion"),
    TOU(121, "TOU"),
    TRANCEIVER(122, "Tranceiver"),
    USAGE(123, "Usage"),
    VERSION(124, "Version"),
    TIMEOUT(125, "Timeout"),
    PHASEAVOLTAGEPOTENTIAL(126, "PhaseAVoltagePotential"),
    IPADDRESS(127, "IPAddress"),
    DOOR(128, "Door"),
    SESSION(129, "Session"),
    PHASEANGLE(130, "PhaseAngle"),
    PHASEAVOLTAGE(131, "PhaseAVoltage"),
    PHASEBVOLTAGE(132, "PhaseBVoltage"),
    PHASECVOLTAGE(133, "PhaseCVoltage"),
    PHASEBVOLTAGEPOTENTIAL(134, "PhaseBVoltagePotential"),
    PHASECVOLTAGEPOTENTIAL(135, "PhaseCVoltagePotential"),
    RADIO(136, "Radio"),
    NEUTRALCURRENT(137, "NeutralCurrent"),
    EMERGENCYSUPPLYCAPACITYLIMIT(138, "EmergencySupplyCapacityLimit"),
    SUPPLYCAPACITYLIMIT(139, "SupplyCapacityLimit"),
    TARIFF(140, "Tariff"),
    ENCLOSURE(141,"Enclosure"),
    ADCONVERTER(142, "ADConverter"),
    DISPLAY(143, "Display"),
    SENSOR(144, "Sensor"),
    LOWSPEEDBUS(145, "LowSpeedBus"),
    OPTIONBOARD(146, "OptionBoard"),
    METERBUS(147, "MeterBus"),
    ALLEVENTS(148, "AllEvents"),
    QUEUE(197, "Queue"),
    READACCESS(202, "ReadAccess"),
    REMOTEACCESS(211, "RemoteAccess"),
    SEASON(228, "Season"),
    SELFREAD(231, "SelfRead"),
    SPECIFICGRAVITY(240, "SpecificGravity"),
    THRESHOLD(261, "Threshold"),
    WRITEACCESS(282, "WriteAccess"),
    ACTIVATION(283,"Activation"),
    CHECKSUM(284,"Checksum"),
    ALARMTABLE(285,"AlarmTable"),
    MEASUREMENTTYPE(286,"MeasurementType"),
    PHASEACURRENT(287,"PhaseACurrent"),
    PHASEBCURRENT(288,"PhaseBCurrent"),
    PHASECCURRENT(289,"PhaseCCurrent"),
    APPARENTPOWER(290,"ApparentPower"),
    PHASEAAPPARENTPOWER(291,"PhaseAApparentPower"),
    PHASEBAPPARENTPOWER(292,"PhaseBApparentPower"),
    PhaseCApparentPower(293,"PhaseCApparentPower"),
    REACTIVEPOWER(294,"ReactivePower"),
    PHASEAREACTIVEPOWER(295,"PhaseAReactivePower"),
    PHASEBREACTIVEPOWER(296,"PhaseBReactivePower"),
    PHASECREACTIVEPOWER(297,"PhaseCReactivePower"),
    INITIALISATION(298,"Initialisation"),
    DAYLIMIT(299,"DayLimit"),
    RECODER(300,"Recoder"),
    ELSTER_DEFINED_SETUP1(301, "Setup1"),
    ELSTER_DEFINED_SETUP2(302, "Setup2"),
    ELSTER_DEFINED_SETUP3(303, "Setup3"),
    ELSTER_DEFINED_SETUP4(304, "Setup4"),
    ELSTER_DEFINED_PIB(311, "Pib"),
    ELSTER_DEFINED_MIB(312, "Mib"),
    ELSTER_DEFINED_PM1_AFFILATION(313, "Pm1Affiliation"),
    ELSTER_DEFINED_VALVE_PGV(314, "ValvePgv"),
    ELSTER_DEFINED_PDR(315, "Pdr"),
    ELSTER_DEFINED_LOAD_PROFILE_1(321, "LoadProfile1"),
    ELSTER_DEFINED_LOAD_PROFILE_2(322, "LoadProfile2");

    private int value;
    private String mnemonic;

    private EndDeviceSubdomain(int value, String mnemonic) {
        this.value = value;
        this.mnemonic = mnemonic;
    }

    public int getValue() {
        return value;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public String getTranslationKey() {
        return "sd" + mnemonic;
    }

    public static EndDeviceSubdomain fromValue(int value) {
        for (EndDeviceSubdomain sd : EndDeviceSubdomain.values()) {
            if (sd.getValue() == value) {
                return sd;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getMnemonic();
    }

}
