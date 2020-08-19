package com.energyict.protocolimplv2.nta.esmr50.common.registers;


import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.DateTime;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.cosem.attributes.DefinableLoadProfileAttributes;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.GSMDiagnosticAttributes;
import com.energyict.dlms.cosem.attributes.LTEConnectionRejectionAttributes;
import com.energyict.dlms.cosem.attributes.LTEMonitoringAttributes;
import com.energyict.dlms.cosem.attributes.LTESetupAttributes;
import com.energyict.dlms.cosem.attributes.MBusDiagnosticAttributes;
import com.energyict.dlms.cosem.attributes.MBusClientAttributes;
import com.energyict.dlms.cosem.attributes.PPPSetupAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr40.registers.Dsmr40RegisterFactory;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.EMeterConfigurationObject;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.GSMDiagnosticPSStatusVersion1;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.LTEConnectionRejection;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.LTEDiagnosticAdjacentCells;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.LTEDiagnosticCSAttachement;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.LTEDiagnosticCellInfo;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.LTEDiagnosticStatus;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.LTEMonitoringWrapper;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.LTEMonitoringWrapperVersion0;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.LTEPingAddress;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.MBusConfigurationObject;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.MBusEncryptionKeyStatus;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.MBusFUAKStatus;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.MBusFWTransferStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.energyict.cbo.BaseUnit.ACTIVEENERGY;
import static com.energyict.cbo.BaseUnit.AMPERE;
import static com.energyict.cbo.BaseUnit.APPARENTENERGY;
import static com.energyict.cbo.BaseUnit.DECIBELMILLIWAT;
import static com.energyict.cbo.BaseUnit.REACTIVEENERGY;
import static com.energyict.cbo.BaseUnit.SECOND;
import static com.energyict.cbo.BaseUnit.UNITLESS;
import static com.energyict.cbo.BaseUnit.VOLT;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.WARNING;

public class ESMR50RegisterFactory extends Dsmr40RegisterFactory {
    //Firmware
    protected static final ObisCode ACTIVE_CORE_METER_FIRMWARE_VERSION_OBISCODE = ObisCode.fromString("1.1.0.2.0.255");
    public static final ObisCode ACTIVE_MODEM_FIRMWARE_VERSION_OBISCODE = ObisCode.fromString("1.2.0.2.0.255");

    protected static final ObisCode MODEM_FIRMWARE_SIGNATURE_OBISCODE = ObisCode.fromString("1.2.0.2.8.255");
    protected static final ObisCode ACTIVE_FIRMWARE_SIGNATURE_OBISCODE = ObisCode.fromString("1.0.0.2.8.255");

    public static final ObisCode AUXILIARY_FIRMWARE_VERSION = ObisCode.fromString("1.4.0.2.0.255");
    //Security
    protected static final ObisCode SECURITY_SETUP_OBISCODE = ObisCode.fromString("0.0.43.0.0.255");
    //Power
    public static final ObisCode POWER_FAILURE_STATUS = ObisCode.fromString("0.0.96.7.9.255");
    public static final ObisCode POWER_LONG_FAILURE_STATUS = ObisCode.fromString("0.0.96.7.21.255");
    //Reactive power
    protected static final ObisCode INSTANTANEOUS_REACTIVE_POWER_L1_Q_PLUS = ObisCode.fromString("1.0.23.7.0.255");
    protected static final ObisCode INSTANTANEOUS_REACTIVE_POWER_L1_Q_MINUS = ObisCode.fromString("1.0.24.7.0.255");
    protected static final ObisCode AVERAGE_ACTIVE_POWER_L2_P_PLUS = ObisCode.fromString("1.0.41.5.0.255");
    protected static final ObisCode AVERAGE_ACTIVE_POWER_L2_P_MINUS = ObisCode.fromString("1.0.42.5.0.255");
    protected static final ObisCode AVERAGE_REACTIVE_POWER_L2_Q2_PLUS = ObisCode.fromString("1.0.43.5.0.255");
    protected static final ObisCode AVERAGE_REACTIVE_POWER_L2_Q2_MINUS = ObisCode.fromString("1.0.44.5.0.255");
    protected static final ObisCode AVERAGE_CURRENT_L1 = ObisCode.fromString("1.0.52.25.0.255");
    protected static final ObisCode INSTANTANEOUS_REACTIVE_POWER_L2_Q_PLUS = ObisCode.fromString("1.0.43.7.0.255");
    protected static final ObisCode INSTANTANEOUS_REACTIVE_POWER_L2_Q_MINUS = ObisCode.fromString("1.0.44.7.0.255");
    protected static final ObisCode INSTANTANEOUS_REACTIVE_POWER_L3_Q_PLUS = ObisCode.fromString("1.0.63.7.0.255");
    protected static final ObisCode INSTANTANEOUS_REACTIVE_POWER_L3_Q_MINUS = ObisCode.fromString("1.0.64.7.0.255");
    protected static final ObisCode INSTANTANEOUS_ACTIVE_POWER_ABS = ObisCode.fromString("1.0.16.7.0.255");
    protected static final ObisCode INSTANTANEOUS_REACTIVE_POWER_Q_PLUS = ObisCode.fromString("1.0.3.7.0.255");
    protected static final ObisCode INSTANTANEOUS_REACTIVE_POWER_Q_MINUS = ObisCode.fromString("1.0.9.7.0.255");
    protected static final ObisCode INSTANTANEOUS_APPARENT_POWER_S_PLUS = ObisCode.fromString("1.0.10.7.0.255");
    protected static final ObisCode INSTANTANEOUS_APPARENT_POWER_S_MINUS = ObisCode.fromString("1.0.10.7.0.255");
    protected static final ObisCode INSTANTANEOUS_CURRENT = ObisCode.fromString("1.0.90.7.0.255");
    protected static final ObisCode AVERAGE_CURRENT_L2 = ObisCode.fromString("1.0.51.25.0.255");
    protected static final ObisCode INSTANTANEOUS_VOLTAGE_L3 = ObisCode.fromString("1.0.72.7.0.255");
    protected static final ObisCode AVERAGE_VOLTAGE_L3 = ObisCode.fromString("1.0.72.25.0.255");
    protected static final ObisCode INSTANTANEOUS_CURRENT_L3 = ObisCode.fromString("1.0.71.7.0.255");
    protected static final ObisCode INSTANTANEOUS_ACTIVE_POWER_P_PLUS = ObisCode.fromString("1.0.1.7.0.255");
    protected static final ObisCode AVERAGE_ACTIVE_POWER_P_PLUS_L3 = ObisCode.fromString("1.0.61.4.0.255");
    protected static final ObisCode AVERAGE_ACTIVE_P_MINUS_POWER_L3 = ObisCode.fromString("1.0.62.4.0.255");
    protected static final ObisCode INSTANTANEOUS_ACTIVE_POVER_P_PLUS_L3 = ObisCode.fromString("1.0.61.7.0.255");
    protected static final ObisCode INSTANTANEOUS_ACTIVE_POVER_P_MINUS_L3 = ObisCode.fromString("1.0.62.7.0.255");
    protected static final ObisCode AVERAGE_REACTIVE_POVER_Q_PLUS_L3 = ObisCode.fromString("1.0.63.4.0.255");
    protected static final ObisCode AVERAGE_REACTIVE_POVER_Q_MINUS_L3 = ObisCode.fromString("1.0.64.4.0.255");

    protected static final ObisCode AVERAGE_VOLTAGE_L1 = ObisCode.fromString("1.0.32.25.0.255");
    protected static final ObisCode AVERAGE_ACTIVE_POWER_P_PLUS_L1 = ObisCode.fromString("1.0.21.5.0.255");
    protected static final ObisCode AVERAGE_ACTIVE_POWER_P_MINUS_L1 = ObisCode.fromString("1.0.22.5.0.255");
    protected static final ObisCode AVERAGE_REACTIVE_POWER_Q_PLUS_L1 = ObisCode.fromString("1.0.23.5.0.255");
    protected static final ObisCode AVERAGE_REACTIVE_POWER_Q_MINUS_L1 = ObisCode.fromString("1.0.23.5.0.255");
    protected static final ObisCode AVERAGE_CURRENT_L3 = ObisCode.fromString("1.0.71.25.0.255");

    protected static final ObisCode POWER_QUALITY_PROFILE1 = ObisCode.fromString("1.0.99.1.1.255");
    protected static final ObisCode POWER_QUALITY_PROFILE2 = ObisCode.fromString("1.0.99.1.2.255");
    protected static final ObisCode VOLTAGE_QUALITY_EVENT_LOG = ObisCode.fromString("0.0.99.98.5.255");
    protected static final ObisCode COMMUNICATION_SESSION_EVENT_LOG = ObisCode.fromString("0.0.99.98.4.255");
    protected static final ObisCode BILLING_PERIODS = ObisCode.fromString("0.0.96.7.19.255");

    protected static final ObisCode DEVICE_ID_1 = ObisCode.fromString("0.0.96.1.0.255");
    protected static final ObisCode DEVICE_ID_2 = ObisCode.fromString("0.0.96.1.1.255");
    protected static final ObisCode DEVICE_ID_5 = ObisCode.fromString("0.0.96.1.4.255");
    public static final ObisCode BILLING_PERIOD = ObisCode.fromString("0.0.15.0.0.255");
    public static final ObisCode P1_PORT_DSMR_VERSION = ObisCode.fromString("1.3.0.2.8.255");
    public static final ObisCode EMETER_CONFIGURATION_OBJECT = ObisCode.fromString("0.1.94.31.3.255");
    public static final ObisCode EMETER_FW_UPGRADE_STATUS = ObisCode.fromString("0.0.44.0.6.255");
    //MBUS Client Object attributes
    public static final ObisCode MBUS_ENCRYPTION_STATUS = ObisCode.fromString("0.0.97.98.14.255");
    public static final ObisCode MBUS_CAPTURE_PERIOD        = ObisCode.fromString("0.x.24.1.4.255");
    public static final ObisCode MBUS_PRIMARY_ADDRESS       = ObisCode.fromString("0.x.24.1.5.255");
    public static final ObisCode MBUS_IDENTIFICATION_NUMBER = ObisCode.fromString("0.x.24.1.6.255");
    public static final ObisCode MBUS_MANUFACTURER_ID       = ObisCode.fromString("0.x.24.1.7.255");
    public static final ObisCode MBUS_VERSION       = ObisCode.fromString("0.x.24.1.8.255");
    public static final ObisCode MBUS_DEVICE_TYPE   = ObisCode.fromString("0.x.24.1.9.255");
    public static final ObisCode MBUS_ACCESS_NUMBER = ObisCode.fromString("0.x.24.1.10.255");
    public static final ObisCode MBUS_STATUS        = ObisCode.fromString("0.x.24.1.11.255");
    public static final ObisCode MBUS_ALARM         = ObisCode.fromString("0.x.24.1.12.255");
    public static final ObisCode MBUS_CONFIGURATION = ObisCode.fromString("0.x.24.1.13.255");
    public static final ObisCode MBUS_ENCRYPTION_KEY_STATUS = ObisCode.fromString("0.x.24.1.14.255");
    public static final ObisCode MBUS_FUAK_STATUS = ObisCode.fromString("0.x.24.1.15.255");
    public static final ObisCode MBUS_OPERATIONAL_FW_UPGRADE_STATUS_X = ObisCode.fromString("0.x.44.0.1.255");
    public static final ObisCode MBUS_FW_UPGRADE_STATUS_X = ObisCode.fromString("0.x.44.0.6.255");
    public static final ObisCode MBUS_EVENT_LOG = ObisCode.fromString("0.x.99.98.3.255");
    public static final ObisCode MBUS_DEVICE_CONFIGURATION = ObisCode.fromString("0.x.24.2.2.255");
    public static final ObisCode MBUS_DEVICE_CONFIGURATION_METEROLOGICAL_FIRMWARE = ObisCode.fromString("0.x.24.31.0.255");
    public static final ObisCode MBUS_DEVICE_CONFIGURATION_OPERATIONAL_FIRMWARE = ObisCode.fromString("0.x.24.32.0.255");
    public static final ObisCode MBUS_DEVICE_CONFIGURATION_ADDITIONAL_FIRMWARE = ObisCode.fromString("0.x.24.33.0.255");
    public static final ObisCode MBUS_MASTER_VALUE_5_MIN = ObisCode.fromString("0.x.24.2.1.255");
    public static final ObisCode MBUS_STATISTICS_NUMBER_MISSED_C = ObisCode.fromString("0.x.128.1.0.255");
    public static final ObisCode MBUS_STATISTICS_NUMBER_MISSED_T = ObisCode.fromString("0.x.128.2.0.255");
    public static final ObisCode MBUS_STATISTICS_AVERAGE_MISSED_C = ObisCode.fromString("0.x.129.1.0.255");
    public static final ObisCode MBUS_STATISTICS_AVERAGE_MISSED_T = ObisCode.fromString("0.x.129.2.0.255");
    //LTE Setup
    public static final ObisCode LTE_SETUP_base = ObisCode.fromString("0.1.25.4.0.255");
    public static final ObisCode LTE_SETUP_APN = ObisCode.fromString("0.1.25.4.2.255");
    public static final ObisCode LTE_SETUP_PIN = ObisCode.fromString("0.1.25.4.3.255");
    public static final ObisCode LTE_SETUP_QOS = ObisCode.fromString("0.1.25.4.4.255");
    public static final ObisCode PPP_SETUP_base = ObisCode.fromString("0.0.25.3.0.255");
    public static final ObisCode PPP_SETUP_AUTH_USERNAME = ObisCode.fromString("0.1.25.3.5.255");
    public static final ObisCode PPP_SETUP_AUTH_PASSWORD = ObisCode.fromString("0.2.25.3.5.255");

    //LTE Diagnostic
    public static final ObisCode GSM_DIAGNOSTIC_BASE = ObisCode.fromString("0.1.25.6.0.255");
    public static final ObisCode GSM_DIAGNOSTIC_OPERATOR = ObisCode.fromString("0.1.25.6.2.255");
    public static final ObisCode GSM_DIAGNOSTIC_STATUS = ObisCode.fromString("0.1.25.6.3.255");
    public static final ObisCode GSM_DIAGNOSTIC_CS_ATTACHMENT = ObisCode.fromString("0.1.25.6.4.255");
    public static final ObisCode GSM_DIAGNOSTIC_PS_STATUS = ObisCode.fromString("0.1.25.6.5.255");
    public static final ObisCode GSM_DIAGNOSTIC_CELL_INFO_BASE = ObisCode.fromString("0.1.25.6.6.255");
    public static final ObisCode GSM_DIAGNOSTIC_CELL_INFO_CELL_ID = ObisCode.fromString("0.1.25.6.61.255");
    public static final ObisCode GSM_DIAGNOSTIC_CELL_INFO_LOCATION_ID = ObisCode.fromString("0.1.25.6.62.255");
    public static final ObisCode GSM_DIAGNOSTIC_CELL_INFO_SIGNAL_QUALITY = ObisCode.fromString("0.1.25.6.63.255");
    public static final ObisCode GSM_DIAGNOSTIC_CELL_INFO_BER = ObisCode.fromString("0.1.25.6.64.255");
    public static final ObisCode GSM_DIAGNOSTIC_CELL_INFO_MCC = ObisCode.fromString("0.1.25.6.65.255");
    public static final ObisCode GSM_DIAGNOSTIC_CELL_INFO_MNC = ObisCode.fromString("0.1.25.6.66.255");
    public static final ObisCode GSM_DIAGNOSTIC_CELL_INFO_CHANNEL_NUMBER = ObisCode.fromString("0.1.25.6.67.255");
    public static final ObisCode GSM_DIAGNOSTIC_ADJACENT_CELLS_BASE = ObisCode.fromString("0.1.25.6.7.255");
    public static final ObisCode GSM_DIAGNOSTIC_ADJACENT_CELLS_CELLID = ObisCode.fromString("0.1.25.6.71.255");
    public static final ObisCode GSM_DIAGNOSTIC_ADJACENT_CELLS_SIGNAL_QUALITY = ObisCode.fromString("0.1.25.6.72.255");
    public static final ObisCode GSM_DIAGNOSTIC_ADJACENT_CELLS_CELLID_2 = ObisCode.fromString("0.1.25.6.73.255");
    public static final ObisCode GSM_DIAGNOSTIC_ADJACENT_CELLS_SIGNAL_QUALITY_2 = ObisCode.fromString("0.1.25.6.74.255");
    public static final ObisCode GSM_DIAGNOSTIC_ADJACENT_CELLS_CELLID_3 = ObisCode.fromString("0.1.25.6.75.255");
    public static final ObisCode GSM_DIAGNOSTIC_ADJACENT_CELLS_SIGNAL_QUALITY_3 = ObisCode.fromString("0.1.25.6.76.255");

    public static final ObisCode GSM_DIAGNOSTIC_CAPTURE_TIME = ObisCode.fromString("0.1.25.6.8.255");
    //PHY randomization
    public static final ObisCode PHY_RANDOMISATION = ObisCode.fromString("0.1.94.31.12.255");

    //PING Address of LTE
    public static final ObisCode LTE_PING_ADDRESS = ObisCode.fromString("0.65.44.0.2.255");

    /** Class 77
     * This object is used to store the RSSI of the last wireless M-Bus frame, received by the E-meter,
     * the RSSI capture time will be stored in attribute 9.
     * All other attributes and method are for future use.
      */
    public static final ObisCode MBUS_DIAGNOSTIC = ObisCode.fromString("0.x.24.9.1.255");

    /** Class 151
     * LTE Monitoring (Class ID: 151, Version: 0)
     */
    public static final ObisCode LTE_MONITORING_BASE = ObisCode.fromString("0.1.25.11.0.255");
    public static final ObisCode LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3402 = ObisCode.fromString("0.1.25.11.21.255");
    public static final ObisCode LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3412 = ObisCode.fromString("0.1.25.11.22.255");
    public static final ObisCode LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRQ = ObisCode.fromString("0.1.25.11.23.255");
    public static final ObisCode LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRP = ObisCode.fromString("0.1.25.11.24.255");
    public static final ObisCode LTE_MONITORING_LTE_QUALITY_OF_SERVICE_Q_RXLEV_MIN = ObisCode.fromString("0.1.25.11.25.255");

    /** Class 151
     * LTE Monitoring (Class ID: 151, Version: 1) added 6 registers on top of version: 0
     */
    public static final ObisCode LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3412ext2 = ObisCode.fromString("0.1.25.11.26.255");
    public static final ObisCode LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3324 = ObisCode.fromString("0.1.25.11.27.255");
    public static final ObisCode LTE_MONITORING_LTE_QUALITY_OF_SERVICE_TeDRX = ObisCode.fromString("0.1.25.11.28.255");
    public static final ObisCode LTE_MONITORING_LTE_QUALITY_OF_SERVICE_TPTW = ObisCode.fromString("0.1.25.11.29.255");
    public static final ObisCode LTE_MONITORING_LTE_QUALITY_OF_SERVICE_Q_RXLEV_MIN_CE_r13 = ObisCode.fromString("0.1.25.11.30.255");
    public static final ObisCode LTE_MONITORING_LTE_QUALITY_OF_SERVICE_Q_RXLEV_MIN_CE1_r13 = ObisCode.fromString("0.1.25.11.31.255");

    /**
     * LTE FW Upgrade
     **/
    public static final ObisCode LTE_FW_LOCATION = ObisCode.fromString("0.65.44.0.0.255");
    public static final ObisCode LTE_FW_DOWNLOAD_TIME = ObisCode.fromString("0.65.44.0.1.255");
    public static final ObisCode LTE_FW_UPGRADE_STATUS = ObisCode.fromString("0.5.44.0.6.255");

    /**
     * LTE Connection rejection (Class ID: 1, Version: 0)
     *
     **/
    public static final ObisCode LTE_CONNECTION_REJECTION_BASE = ObisCode.fromString("0.1.94.31.7.255");
    public static final ObisCode LTE_CONNECTION_REJECTION_LAST_REJECTED_CAUSE = ObisCode.fromString("0.1.94.31.71.255");
    public static final ObisCode LTE_CONNECTION_REJECTION_LAST_REJECTED_MCC = ObisCode.fromString("0.1.94.31.72.255");
    public static final ObisCode LTE_CONNECTION_REJECTION_LAST_REJECTED_MNC = ObisCode.fromString("0.1.94.31.73.255");
    public static final ObisCode LTE_CONNECTION_REJECTION_TIMESTAMP_LAST_REJECTION = ObisCode.fromString("0.1.94.31.74.255");

    //Threshold for voltage registers
    protected static final ObisCode THRESHOLD_SHORT_VOLTAGE_SAG1 = ObisCode.fromString("1.0.12.31.0.255");
    protected static final ObisCode TIME_THRESHOLD_VOLTAGE_SAG1 = ObisCode.fromString("1.0.12.43.0.255");

    protected static final ObisCode THRESHOLD_VOLTAGE_SWELL_SAG1 = ObisCode.fromString("1.0.12.35.0.255");
    protected static final ObisCode TIME_THRESHOLD_VOLTAGE_SWELL_SAG1 = ObisCode.fromString("1.0.12.44.0.255");
    protected static final ObisCode THRESHOLD_SHORT_VOLTAGE_SAG2 = ObisCode.fromString("1.1.12.31.0.255");
    protected static final ObisCode TIME_THRESHOLD_VOLTAGE_SAG2 = ObisCode.fromString("1.1.12.43.0.255");

    public ESMR50RegisterFactory(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected RegisterValue convertCustomAbstractObjectsToRegisterValues(final OfflineRegister register, AbstractDataType abstractDataType) throws UnsupportedException {
        try {
            ObisCode rObisCode = getCorrectedRegisterObisCode(register);

            if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_CAPTURE_TIME)) { // this one is not in ESMR specs!
                Structure structure = abstractDataType.getStructure();
                StringBuilder sb = new StringBuilder();

                sb.append(structure.getNextDataType().intValue()).append(";"); // unsigned: 0
                sb.append(structure.getNextDataType().longValue()).append(";"); // long-unsigned: 204
                sb.append(structure.getNextDataType().longValue()).append(";"); // long-unsigned: 0

                Date capturedTime = structure.getNextDataType().getOctetString().getDateTime(protocol.getDlmsSession().getTimeZone()).getValue().getTime();
                sb.append(capturedTime.toString());

                return new RegisterValue(register, null, null, null, null, capturedTime, 0, sb.toString());
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_STATISTICS_NUMBER_MISSED_C)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)));
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_STATISTICS_NUMBER_MISSED_T)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)));
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_STATISTICS_AVERAGE_MISSED_C)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)));
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_STATISTICS_AVERAGE_MISSED_T)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)));
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_MASTER_VALUE_5_MIN)) {
                return super.convertCustomAbstractObjectsToRegisterValues(register, abstractDataType);
                //return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, new String("MBus P1 5 Minute Master Value: " + Long.toString(abstractDataType.longValue())));
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_CONFIGURATION)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, "MBus Configuration: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_CAPTURE_PERIOD)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0);
            }
            else if (rObisCode.equalsIgnoreBChannel(MBUS_PRIMARY_ADDRESS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0);
            }
            else if (rObisCode.equalsIgnoreBChannel(MBUS_IDENTIFICATION_NUMBER)) {
                String bcd = ProtocolTools.getBCD(abstractDataType.longValue());
                return new RegisterValue(register, bcd);
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_MANUFACTURER_ID)) {
                return new RegisterValue(register, String.valueOf(abstractDataType.longValue()));
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_VERSION)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0);
            }else if (rObisCode.equalsIgnoreBChannel(MBUS_DEVICE_TYPE)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0);
            }else if (rObisCode.equalsIgnoreBChannel(MBUS_ACCESS_NUMBER)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0);
            }else if (rObisCode.equalsIgnoreBChannel(MBUS_STATUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0);
            }
            else if (rObisCode.equalsIgnoreBChannel(MBUS_ALARM)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, "MBus alarm: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_ENCRYPTION_KEY_STATUS)) {
                int statusId = abstractDataType.intValue();
                String ekStatus = MBusEncryptionKeyStatus.getDescriptionForId(statusId);
                return new RegisterValue(register, new Quantity(statusId, Unit.get(UNITLESS)), null, null, null, new Date(), 0, ekStatus);
            }  else if (rObisCode.equalsIgnoreBChannel(MBUS_FUAK_STATUS)) {
                int statusId = abstractDataType.intValue();
                String fuakStatus = MBusFUAKStatus.getDescriptionForId(statusId);
                return new RegisterValue(register, new Quantity(statusId, Unit.get(UNITLESS)), null, null, null, new Date(), 0, fuakStatus);
            } else if (rObisCode.equalsIgnoreBChannel(PHY_RANDOMISATION)) {
                if (abstractDataType.isStructure()) {
                    Structure structure = abstractDataType.getStructure();
                    return new RegisterValue(register, structure.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equalsIgnoreBChannel(LTE_SETUP_APN)) {
                return new RegisterValue(register, abstractDataType.getOctetString().stringValue());
            } else if (rObisCode.equalsIgnoreBChannel(LTE_SETUP_PIN)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0);
            } else if (rObisCode.equalsIgnoreBChannel(LTE_SETUP_QOS)) {
                if (abstractDataType!=null){
                    if (abstractDataType.isStructure()){
                        return new RegisterValue(register, abstractDataType.getStructure().toString());
                    } else if (abstractDataType.isOctetString()) {
                        return new RegisterValue(register, abstractDataType.getOctetString().stringValue());
                    } else {
                        return new RegisterValue(register, abstractDataType.toString());
                    }
                }
            } else if (rObisCode.equals(PPP_SETUP_AUTH_USERNAME)) {
                if (abstractDataType!=null){
                    if (abstractDataType.isStructure()){
                        return new RegisterValue(register, abstractDataType.getStructure().getDataType(0).getOctetString().stringValue());
                    } else {
                        unableToParseRegisterValueException(abstractDataType);
                    }
                }
            } else if (rObisCode.equals(PPP_SETUP_AUTH_PASSWORD)) {
                if (abstractDataType!=null){
                    if (abstractDataType.isStructure()){
                        return new RegisterValue(register, abstractDataType.getStructure().getDataType(1).getOctetString().stringValue());
                    } else {
                        unableToParseRegisterValueException(abstractDataType);
                    }
                }
            } else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_STATUS)) {
                int statusId = abstractDataType.getTypeEnum().intValue();
                String diagnosticStatus = LTEDiagnosticStatus.getDescriptionForId(statusId);
                return new RegisterValue(register, new Quantity(statusId, Unit.get(UNITLESS)), null, null, null, new Date(), 0, diagnosticStatus);
            } else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_OPERATOR)) {
                return new RegisterValue(register, abstractDataType.getVisibleString().getStr());
            } else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_CS_ATTACHMENT)) {
                int id = abstractDataType.getTypeEnum().intValue();
                String desc = LTEDiagnosticCSAttachement.getDescriptionForId(id);
                return new RegisterValue(register, new Quantity(id, Unit.get(UNITLESS)), null, null, null, new Date(), 0, desc);
            } else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_PS_STATUS)) {
                int id = abstractDataType.getTypeEnum().intValue();
                String desc = GSMDiagnosticPSStatusVersion1.getDescriptionForId(id);
                return new RegisterValue(register, new Quantity(id, Unit.get(UNITLESS)), null, null, null, new Date(), 0, desc);
            } else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_CELL_INFO_BASE)) {
                LTEDiagnosticCellInfo cellInfo = new LTEDiagnosticCellInfo(abstractDataType.getStructure());
                if (cellInfo.isDecoded()) {
                    return new RegisterValue(register, new Quantity(cellInfo.getCellId(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, cellInfo.toString());
                } else {
                    return new RegisterValue(register, null, null, null, null, new Date(), 0, abstractDataType.getStructure().toString());
                }
            }else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_CELL_INFO_CELL_ID)) {
                LTEDiagnosticCellInfo cellInfo = new LTEDiagnosticCellInfo(abstractDataType.getStructure());
                if (cellInfo.isDecoded()) {
                    return new RegisterValue(register, new Quantity(cellInfo.getCellId(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, cellInfo.toString());
                } else {
                    return new RegisterValue(register, null, null, null, null, new Date(), 0, abstractDataType.getStructure().toString());
                }
            }else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_CELL_INFO_LOCATION_ID)) {
                LTEDiagnosticCellInfo cellInfo = new LTEDiagnosticCellInfo(abstractDataType.getStructure());
                if (cellInfo.isDecoded()) {
                    return new RegisterValue(register, new Quantity(cellInfo.getLocationId(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, cellInfo.toString());
                } else {
                    return new RegisterValue(register, null, null, null, null, new Date(), 0, abstractDataType.getStructure().toString());
                }
            }else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_CELL_INFO_SIGNAL_QUALITY)) {
                LTEDiagnosticCellInfo cellInfo = new LTEDiagnosticCellInfo(abstractDataType.getStructure());
                if (cellInfo.isDecoded()) {
                    return new RegisterValue(register, new Quantity(cellInfo.getSignalQuality(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, cellInfo.toString());
                } else {
                    return new RegisterValue(register, null, null, null, null, new Date(), 0, abstractDataType.getStructure().toString());
                }
            }else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_CELL_INFO_BER)) {
                LTEDiagnosticCellInfo cellInfo = new LTEDiagnosticCellInfo(abstractDataType.getStructure());
                if (cellInfo.isDecoded()) {
                    return new RegisterValue(register, new Quantity(cellInfo.getBer(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, cellInfo.toString());
                } else {
                    return new RegisterValue(register, null, null, null, null, new Date(), 0, abstractDataType.getStructure().toString());
                }
            }else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_CELL_INFO_MCC)) {
                LTEDiagnosticCellInfo cellInfo = new LTEDiagnosticCellInfo(abstractDataType.getStructure());
                if (cellInfo.isDecoded()) {
                    return new RegisterValue(register, new Quantity(cellInfo.getMcc(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, cellInfo.toString());
                } else {
                    return new RegisterValue(register, null, null, null, null, new Date(), 0, abstractDataType.getStructure().toString());
                }
            }else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_CELL_INFO_MNC)) {
                LTEDiagnosticCellInfo cellInfo = new LTEDiagnosticCellInfo(abstractDataType.getStructure());
                if (cellInfo.isDecoded()) {
                    return new RegisterValue(register, new Quantity(cellInfo.getMnc(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, cellInfo.toString());
                } else {
                    return new RegisterValue(register, null, null, null, null, new Date(), 0, abstractDataType.getStructure().toString());
                }
            } else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_CELL_INFO_CHANNEL_NUMBER)) {
                LTEDiagnosticCellInfo cellInfo = new LTEDiagnosticCellInfo(abstractDataType.getStructure());
                if (cellInfo.isDecoded()) {
                    return new RegisterValue(register, new Quantity(cellInfo.getChannelNumber(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, cellInfo.toString());
                } else {
                    return new RegisterValue(register, null, null, null, null, new Date(), 0, abstractDataType.getStructure().toString());
                }
            } else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_ADJACENT_CELLS_BASE)) {
                LTEDiagnosticAdjacentCells adjacentCells = new LTEDiagnosticAdjacentCells(abstractDataType.getArray());
                if (adjacentCells.isDecoded()) {
                    return new RegisterValue(register, adjacentCells.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.getArray().toString());
                }
            }else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_ADJACENT_CELLS_CELLID)) {
                LTEDiagnosticAdjacentCells adjacentCells = new LTEDiagnosticAdjacentCells(abstractDataType.getArray());
                if (adjacentCells.isDecoded()) {
                    return new RegisterValue(register, new Quantity(adjacentCells.getCellId(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, adjacentCells.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.getArray().toString());
                }
            }else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_ADJACENT_CELLS_SIGNAL_QUALITY)) {
                LTEDiagnosticAdjacentCells adjacentCells = new LTEDiagnosticAdjacentCells(abstractDataType.getArray());
                if (adjacentCells.isDecoded()) {
                    return new RegisterValue(register, new Quantity(adjacentCells.getSignalQuality(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, adjacentCells.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.getArray().toString());
                }
            }else if (rObisCode.equals(LTE_MONITORING_BASE)){
                LTEMonitoringWrapper lteMonitoringWrapper = new LTEMonitoringWrapperVersion0(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()){
                    return new RegisterValue(register, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            }else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3402)){
                LTEMonitoringWrapper lteMonitoringWrapper = new LTEMonitoringWrapperVersion0(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()){
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getT3402(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            }else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3412)){
                LTEMonitoringWrapper lteMonitoringWrapper = new LTEMonitoringWrapperVersion0(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()){
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getT3412(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRQ)){
                LTEMonitoringWrapper lteMonitoringWrapper = new LTEMonitoringWrapperVersion0(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()){
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getRsrq(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            }else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRQ)){
                LTEMonitoringWrapper lteMonitoringWrapper = new LTEMonitoringWrapperVersion0(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()){
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getRsrq(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            }else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRP)){
                LTEMonitoringWrapper lteMonitoringWrapper = new LTEMonitoringWrapperVersion0(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()){
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getRsrp(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            }else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_Q_RXLEV_MIN)){
                LTEMonitoringWrapper lteMonitoringWrapper = new LTEMonitoringWrapperVersion0(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()){
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getqRxlevMin(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            }else if (rObisCode.equals(LTE_CONNECTION_REJECTION_BASE)){
                LTEConnectionRejection lteConnectionRejection = new LTEConnectionRejection(abstractDataType, protocol.getTimeZone());
                if (lteConnectionRejection.isDecoded()){
                    return new RegisterValue(register, lteConnectionRejection.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            }else if (rObisCode.equals(LTE_CONNECTION_REJECTION_LAST_REJECTED_CAUSE)){
                LTEConnectionRejection lteConnectionRejection = new LTEConnectionRejection(abstractDataType, protocol.getTimeZone());
                if (lteConnectionRejection.isDecoded()){
                    return new RegisterValue(register, new Quantity(lteConnectionRejection.getLast_reject_cause(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, lteConnectionRejection.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            }else if (rObisCode.equals(LTE_CONNECTION_REJECTION_LAST_REJECTED_MCC)){
                LTEConnectionRejection lteConnectionRejection = new LTEConnectionRejection(abstractDataType, protocol.getTimeZone());
                if (lteConnectionRejection.isDecoded()){
                    return new RegisterValue(register, new Quantity(lteConnectionRejection.getLast_rejected_mcc(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, lteConnectionRejection.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            }else if (rObisCode.equals(LTE_CONNECTION_REJECTION_LAST_REJECTED_MNC)){
                LTEConnectionRejection lteConnectionRejection = new LTEConnectionRejection(abstractDataType, protocol.getTimeZone());
                if (lteConnectionRejection.isDecoded()){
                    return new RegisterValue(register, new Quantity(lteConnectionRejection.getLast_rejected_mnc(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, lteConnectionRejection.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            }else if (rObisCode.equals(LTE_CONNECTION_REJECTION_TIMESTAMP_LAST_REJECTION)){
                LTEConnectionRejection lteConnectionRejection = new LTEConnectionRejection(abstractDataType, protocol.getTimeZone());
                if (lteConnectionRejection.isDecoded()){
                    return new RegisterValue(register, new Quantity(lteConnectionRejection.getTimestamp_last_rejection().toString(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, lteConnectionRejection.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            }
            else if (rObisCode.equals(EMETER_CONFIGURATION_OBJECT)) {
                EMeterConfigurationObject configurationObject = new EMeterConfigurationObject(abstractDataType);
                if (configurationObject.isDecoded()){
                    return new RegisterValue(register, new Quantity(configurationObject.getFlags(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, configurationObject.toString());
                } else {
                    protocol.journal(configurationObject.getErrorMessage());
                    return new RegisterValue(register, abstractDataType.toString());
                }
            }
            else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_REACTIVE_POWER_L1_Q_PLUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(REACTIVEENERGY)), null, null, null, new Date(), 0, "INSTANTANEOUS_REACTIVE_POWER_L1_Q_PLUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_REACTIVE_POWER_L1_Q_MINUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(REACTIVEENERGY)), null, null, null, new Date(), 0, "INSTANTANEOUS_REACTIVE_POWER_L1_Q_MINUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_REACTIVE_POWER_L1_Q_PLUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(REACTIVEENERGY)), null, null, null, new Date(), 0, "INSTANTANEOUS_REACTIVE_POWER_L1_Q_PLUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_ACTIVE_POWER_L2_P_PLUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(ACTIVEENERGY)), null, null, null, new Date(), 0, "AVERAGE_ACTIVE_POWER_L2_P_PLUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_ACTIVE_POWER_L2_P_MINUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(ACTIVEENERGY)), null, null, null, new Date(), 0, "AVERAGE_ACTIVE_POWER_L2_P_MINUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_REACTIVE_POWER_L2_Q2_PLUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(ACTIVEENERGY)), null, null, null, new Date(), 0, "AVERAGE_ACTIVE_POWER_L2_P_MINUS value: " + abstractDataType.longValue());
            }else if (rObisCode.equalsIgnoreBChannel(AVERAGE_REACTIVE_POWER_L2_Q2_MINUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(ACTIVEENERGY)), null, null, null, new Date(), 0, "AVERAGE_ACTIVE_POWER_L2_P_MINUS value: " + abstractDataType.longValue());
            }else if (rObisCode.equalsIgnoreBChannel(AVERAGE_CURRENT_L1)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(ACTIVEENERGY)), null, null, null, new Date(), 0, "AVERAGE_CURRENT_L1 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_VOLTAGE_L3)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(ACTIVEENERGY)), null, null, null, new Date(), 0, "AVERAGE_VOLTAGE_L3 value: " + abstractDataType.longValue());
            }else if (rObisCode.equalsIgnoreBChannel(AVERAGE_VOLTAGE_L1)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(ACTIVEENERGY)), null, null, null, new Date(), 0, "AVERAGE_VOLTAGE_L1 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_REACTIVE_POWER_L2_Q_MINUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(REACTIVEENERGY)), null, null, null, new Date(), 0, "INSTANTANEOUS_REACTIVE_POWER_L2_Q_MINUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_REACTIVE_POWER_L2_Q_PLUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(REACTIVEENERGY)), null, null, null, new Date(), 0, "INSTANTANEOUS_REACTIVE_POWER_L2_Q_PLUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_REACTIVE_POWER_L3_Q_PLUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(REACTIVEENERGY)), null, null, null, new Date(), 0, "INSTANTANEOUS_REACTIVE_POWER_L3_Q_PLUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_REACTIVE_POWER_L3_Q_MINUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(REACTIVEENERGY)), null, null, null, new Date(), 0, "INSTANTANEOUS_REACTIVE_POWER_L3_Q_MINUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_ACTIVE_POWER_ABS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(ACTIVEENERGY)), null, null, null, new Date(), 0, "INSTANTANEOUS_ACTIVE_POWER_ABS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_REACTIVE_POWER_Q_PLUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(REACTIVEENERGY)), null, null, null, new Date(), 0, "INSTANTANEOUS_REACTIVE_POWER_Q_PLUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_REACTIVE_POWER_Q_MINUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(REACTIVEENERGY)), null, null, null, new Date(), 0, "INSTANTANEOUS_REACTIVE_POWER_Q_MINUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_APPARENT_POWER_S_PLUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(APPARENTENERGY)), null, null, null, new Date(), 0, "INSTANTANEOUS_APPARENT_POWER_S_PLUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_APPARENT_POWER_S_MINUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(APPARENTENERGY)), null, null, null, new Date(), 0, "INSTANTANEOUS_APPARENT_POWER_S_MINUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_CURRENT)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "INSTANTANEOUS_CURRENT value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_CURRENT_L2)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "AVERAGE_CURRENT_L2 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_CURRENT_L3)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "AVERAGE_CURRENT_L3 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_VOLTAGE_L3)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(VOLT)), null, null, null, new Date(), 0, "INSTANTANEOUS_VOLTAGE_L3 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_CURRENT_L3)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "INSTANTANEOUS_CURRENT_L3 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_ACTIVE_POWER_P_PLUS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "INSTANTANEOUS_ACTIVE_POWER_P_PLUS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_ACTIVE_POWER_P_PLUS_L1)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "AVERAGE_ACTIVE_POWER_P_PLUS_L1 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_ACTIVE_POWER_P_MINUS_L1)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "AVERAGE_ACTIVE_POWER_P_MINUS_L1 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_REACTIVE_POWER_Q_PLUS_L1)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "AVERAGE_REACTIVE_POWER_Q_PLUS_L1 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_REACTIVE_POWER_Q_MINUS_L1)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "AVERAGE_REACTIVE_POWER_Q_MINUS_L1 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_ACTIVE_POWER_P_PLUS_L3)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "AVERAGE_ACTIVE_POWER_P_PLUS_L3 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_ACTIVE_P_MINUS_POWER_L3)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "AVERAGE_ACTIVE_P_MINUS_POWER_L3 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_ACTIVE_POVER_P_PLUS_L3)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "INSTANTANEOUS_ACTIVE_POVER_P_PLUS_L3 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(INSTANTANEOUS_ACTIVE_POVER_P_MINUS_L3)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "INSTANTANEOUS_ACTIVE_POVER_P_MINUS_L3 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_REACTIVE_POVER_Q_PLUS_L3)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "AVERAGE_REACTIVE_POVER_Q_PLUS_L3 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(AVERAGE_REACTIVE_POVER_Q_MINUS_L3)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(AMPERE)), null, null, null, new Date(), 0, "AVERAGE_REACTIVE_POVER_Q_MINUS_L3 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(DEVICE_ID_1)) {
                return new RegisterValue(register, abstractDataType.getOctetString().stringValue());
            } else if (rObisCode.equalsIgnoreBChannel(DEVICE_ID_2)) {
                return new RegisterValue(register, abstractDataType.getOctetString().stringValue());
            } else if (rObisCode.equalsIgnoreBChannel(DEVICE_ID_5)) {
                return new RegisterValue(register, abstractDataType.getOctetString().stringValue());
            } else if (rObisCode.equalsIgnoreBChannel(POWER_QUALITY_PROFILE1)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, "POWER_QUALITY_PROFILE1 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(POWER_QUALITY_PROFILE2)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, "POWER_QUALITY_PROFILE1 value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(VOLTAGE_QUALITY_EVENT_LOG)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, "VOLTAGE_QUALITY_EVENT_LOG value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(COMMUNICATION_SESSION_EVENT_LOG)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, "COMMUNICATION_SESSION_EVENT_LOG value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(BILLING_PERIODS)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, "BILLING_PERIODS value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(ERROR_REGISTER)) {
                return new RegisterValue(register, new Quantity(abstractDataType.getUnsigned64().toBigDecimal(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, "ERROR_REGISTER value: " + abstractDataType.getInteger64());
            } else if (rObisCode.equalsIgnoreBChannel(ALARM_REGISTER)) {
                return new RegisterValue(register, new Quantity(abstractDataType.getUnsigned64().toBigDecimal(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, "ALARM_REGISTER value: " + abstractDataType.getInteger64());
            } else if (rObisCode.equalsIgnoreBChannel(ALARM_FILTER)) {
                return new RegisterValue(register, new Quantity(abstractDataType.getUnsigned64().toBigDecimal(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, "ALARM_FILTER value: " + abstractDataType.getInteger64());
            } else if (rObisCode.equals(MODEM_FIRMWARE_SIGNATURE_OBISCODE)) {
                return new RegisterValue(register, abstractDataType.getOctetString().stringValue());
            } else if (rObisCode.equals(AUXILIARY_FIRMWARE_VERSION)) {
                return new RegisterValue(register, abstractDataType.getOctetString().stringValue());
            } else if (rObisCode.equals(ACTIVE_CORE_METER_FIRMWARE_VERSION_OBISCODE)) {
                return new RegisterValue(register, abstractDataType.getOctetString().stringValue());
            } else if (rObisCode.equals(ACTIVE_FIRMWARE_SIGNATURE_OBISCODE)) {
                return new RegisterValue(register, abstractDataType.getOctetString().stringValue());
            } else if(rObisCode.equalsIgnoreBChannel(P1_PORT_DSMR_VERSION)){
                return new RegisterValue(register, abstractDataType.getOctetString().stringValue());
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_EVENT_LOG)) {
                return new RegisterValue(adjustToMbusOC(rObisCode), new Quantity(abstractDataType.longValue(), Unit.get(UNITLESS)), null, null, null, new Date(), 0, "MBUS_EVENT_LOG value: " + abstractDataType.longValue());
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_DEVICE_CONFIGURATION)) {
                MBusConfigurationObject configurationObject = new MBusConfigurationObject(abstractDataType);
                if (configurationObject.isDecoded()){
                    return new RegisterValue(register, new Quantity(1, Unit.get(UNITLESS)), null, null, null, new Date(), 0, configurationObject.getContent());
                } else {
                    protocol.journal(configurationObject.getErrorMessage());
                    return new RegisterValue(register, new Quantity(1, Unit.get(UNITLESS)), null, null, null, new Date(), 0, "MBUS_DEVICE_CONFIGURATION value:" + abstractDataType.toString());
                }
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_DEVICE_CONFIGURATION_METEROLOGICAL_FIRMWARE)) {
                MBusConfigurationObject configurationObject = new MBusConfigurationObject(abstractDataType);
                if (configurationObject.isDecoded()) {
                    return new RegisterValue(register, configurationObject.getMeterologicalFirmware());
                } else {
                    protocol.journal(configurationObject.getErrorMessage());
                    return new RegisterValue(register, new Quantity(1, Unit.get(UNITLESS)), null, null, null, new Date(), 0, abstractDataType.toString());
                }
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_DEVICE_CONFIGURATION_OPERATIONAL_FIRMWARE)) {
                MBusConfigurationObject configurationObject = new MBusConfigurationObject(abstractDataType);
                if (configurationObject.isDecoded()) {
                    return new RegisterValue(register, configurationObject.getOperationalFirmware());
                } else {
                    protocol.journal(configurationObject.getErrorMessage());
                    return new RegisterValue(register, new Quantity(1, Unit.get(UNITLESS)), null, null, null, new Date(), 0, "MBUS_DEVICE_CONFIGURATION_OPERATIONAL_FIRMWARE value:" +abstractDataType.toString());
                }
            } else if (rObisCode.equalsIgnoreBChannel(MBUS_DEVICE_CONFIGURATION_ADDITIONAL_FIRMWARE)) {
                MBusConfigurationObject configurationObject = new MBusConfigurationObject(abstractDataType);
                if (configurationObject.isDecoded()) {
                    return new RegisterValue(register, configurationObject.getAdditionalFirmware());
                } else {
                    protocol.journal(configurationObject.getErrorMessage());
                    return new RegisterValue(register, new Quantity(1, Unit.get(UNITLESS)), null, null, null, new Date(), 0, "MBUS_DEVICE_CONFIGURATION_ADDITIONAL_FIRMWARE value:" +abstractDataType.toString());
                }
            }else if (rObisCode.equals(LTE_FW_UPGRADE_STATUS)) {
                int statusId = abstractDataType.getTypeEnum().intValue();
                String fwStatus = "Status:"+statusId;
                return new RegisterValue(register, new Quantity(statusId, Unit.get(UNITLESS)), null, null, null, new Date(), 0, fwStatus);
            }else if (rObisCode.equals(EMETER_FW_UPGRADE_STATUS)) {
                int statusId = abstractDataType.getTypeEnum().intValue();
                String fwStatus = "Status:"+statusId;
                return new RegisterValue(register, new Quantity(statusId, Unit.get(UNITLESS)), null, null, null, new Date(), 0, fwStatus);
            }else if (rObisCode.equalsIgnoreBChannel(MBUS_FW_UPGRADE_STATUS_X)) {
                int statusId = abstractDataType.intValue();
                String fwStatus = MBusFWTransferStatus.getDescriptionForId(statusId);
                return new RegisterValue(register, new Quantity(statusId, Unit.get(UNITLESS)), null, null, null, new Date(), 0, fwStatus);

            }else if (rObisCode.equalsIgnoreBChannel(MBUS_OPERATIONAL_FW_UPGRADE_STATUS_X)) {
                int statusId = abstractDataType.intValue();
                String fwStatus = MBusFWTransferStatus.getDescriptionForId(statusId);
                return new RegisterValue(register, new Quantity(statusId, Unit.get(UNITLESS)), null, null, null, new Date(), 0, fwStatus);

            }else if (rObisCode.equalsIgnoreBChannel(THRESHOLD_SHORT_VOLTAGE_SAG1)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(VOLT)), null, null, null, new Date(), 0, "THRESHOLD_SHORT_VOLTAGE_SAG value: " + abstractDataType.longValue());
            }else if (rObisCode.equalsIgnoreBChannel(TIME_THRESHOLD_VOLTAGE_SAG1)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(SECOND)), null, null, null, new Date(), 0, "TIME_THRESHOLD_VOLTAGE_SAG value: " + abstractDataType.longValue());
            }else if (rObisCode.equalsIgnoreBChannel(THRESHOLD_SHORT_VOLTAGE_SAG2)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(VOLT)), null, null, null, new Date(), 0, "THRESHOLD_SHORT_VOLTAGE_SAG value: " + abstractDataType.longValue());
            }else if (rObisCode.equalsIgnoreBChannel(TIME_THRESHOLD_VOLTAGE_SAG2)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(SECOND)), null, null, null, new Date(), 0, "TIME_THRESHOLD_VOLTAGE_SAG value: " + abstractDataType.longValue());
            }else if (rObisCode.equalsIgnoreBChannel(TIME_THRESHOLD_VOLTAGE_SWELL_SAG1)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(SECOND)), null, null, null, new Date(), 0, "TIME_THRESHOLD_VOLTAGE_SWELL_SAG1 value: " + abstractDataType.longValue());
            }else if (rObisCode.equalsIgnoreBChannel(THRESHOLD_VOLTAGE_SWELL_SAG1)) {
                return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(VOLT)), null, null, null, new Date(), 0, "THRESHOLD_VOLTAGE_SWELL_SAG1 value: " + abstractDataType.longValue());
            }else if(rObisCode.equals(LTE_PING_ADDRESS)){
                LTEPingAddress ltePingAddress = new LTEPingAddress(abstractDataType);
                return new RegisterValue(register, ltePingAddress.toString());
            }

            protocol.journal(" > register " + register.getObisCode() + " for " + register.getSerialNumber() + " was translated as " + rObisCode.toString() + " and could not be handled by ESMR5 register factory. Asking ancestors.");
        }catch (Exception ex){
            protocol.journal(WARNING, " Error while interpreting value for " + register.getObisCode() + ": " + ex.getMessage());
        }

        return super.convertCustomAbstractObjectsToRegisterValues(register, abstractDataType);
    }

    private void unableToParseRegisterValueException(AbstractDataType abstractDataType) throws ProtocolException {
        throw new ProtocolException("Unable to parse received register data: "+ ProtocolTools.getHexStringFromBytes(abstractDataType.getBEREncodedByteArray(), ""));
    }

    /**
     * Construct a ComposedCosemObject from a list of <CODE>Registers</CODE>.
     * If the {@link com.energyict.protocol.Register} is a DLMS {@link com.energyict.dlms.cosem.Register} or {@link com.energyict.dlms.cosem.ExtendedRegister},
     * and the ObisCode is listed in the ObjectList(see {@link com.energyict.dlms.DLMSMeterConfig#getInstance(String)}, then we define a ComposedRegister and add
     * ObjectList, then we just add it to the {@link #registerMap}. The handling of the <CODE>registerMap</CODE> should be done by the {@link #readRegisters(java.util.List)}
     * method for each <CODE>ObisCode</CODE> in specific.
     *
     * @param registers           the Registers to convert
     * @param supportsBulkRequest indicates whether a DLMS Bulk reques(getWithList) is desired
     * @return a ComposedCosemObject or null if the list was empty
     */
    @Override
    protected ComposedCosemObject constructComposedObjectFromRegisterList(final List<OfflineRegister> registers, final boolean supportsBulkRequest) {
        if (registers != null) {
            List<OfflineRegister> registersForSuper = new ArrayList<>();

            List<DLMSAttribute> dlmsAttributes = new ArrayList<>();
            for (OfflineRegister register : registers) {
                ObisCode rObisCode = getCorrectedRegisterObisCode(register);
                if (rObisCode.equalsIgnoreBChannel(MBUS_DIAGNOSTIC)){
                    DLMSAttribute valueAttribute = new DLMSAttribute(rObisCode, MBusDiagnosticAttributes.RSSI.getAttributeNumber(), DLMSClassId.MBUS_DIAGNOSTIC.getClassId());
                    DLMSAttribute captureTimeAttribute = new DLMSAttribute(rObisCode, MBusDiagnosticAttributes.CAPTURE_TIME.getAttributeNumber(), DLMSClassId.MBUS_DIAGNOSTIC);
                    ComposedRegister composedRegister = new ComposedRegister(valueAttribute, null, captureTimeAttribute);

                    dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                    dlmsAttributes.add(composedRegister.getRegisterCaptureTime());
                    protocol.journal(" - register will be handled as composed: " + register.getObisCode());
                    this.getComposedRegisterMap().put(register, composedRegister);
                }else if (rObisCode.equals(EMETER_CONFIGURATION_OBJECT)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_CAPTURE_TIME)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(MBUS_STATISTICS_NUMBER_MISSED_C)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), RegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(MBUS_STATISTICS_NUMBER_MISSED_T)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), RegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(MBUS_STATISTICS_AVERAGE_MISSED_C)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), DemandRegisterAttributes.CURRENT_AVG_VALUE.getAttributeNumber(), DLMSClassId.DEMAND_REGISTER.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(MBUS_STATISTICS_AVERAGE_MISSED_T)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), DemandRegisterAttributes.CURRENT_AVG_VALUE.getAttributeNumber(), DLMSClassId.DEMAND_REGISTER.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(MBUS_MASTER_VALUE_5_MIN)) {
                    //this.registerMap.put(register, new DLMSAttribute(rObisCode, ExtendedRegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.EXTENDED_REGISTER.getClassId()));
                    //dlmsAttributes.add(this.registerMap.get(register));
                    registersForSuper.add(register);
                } else if (rObisCode.equals(SECURITY_SETUP_OBISCODE)) {
                    this.registerMap.put(register, new DLMSAttribute(SecuritySetup.getDefaultObisCode(), 2, DLMSClassId.SECURITY_SETUP.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(LTE_FW_UPGRADE_STATUS)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), 6, DLMSClassId.IMAGE_TRANSFER));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(EMETER_FW_UPGRADE_STATUS)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), 6, DLMSClassId.IMAGE_TRANSFER));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(MBUS_FW_UPGRADE_STATUS_X)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), 6 , DLMSClassId.IMAGE_TRANSFER));
                    dlmsAttributes.add(this.registerMap.get(register));
                }  else if (rObisCode.equalsIgnoreBChannel(MBUS_OPERATIONAL_FW_UPGRADE_STATUS_X)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), -1 , DLMSClassId.IMAGE_TRANSFER));
                    dlmsAttributes.add(this.registerMap.get(register));
                }  else if (rObisCode.equalsIgnoreBChannel(MBUS_CONFIGURATION)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.CONFIGURATION.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(MBUS_CAPTURE_PERIOD)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.CAPTURE_PERIOD.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(MBUS_PRIMARY_ADDRESS)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.PRIMARY_ADDRESS.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(MBUS_IDENTIFICATION_NUMBER)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.IDENTIFICATION_NUMBER.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(MBUS_MANUFACTURER_ID)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.MANUFACTURER_ID.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(MBUS_VERSION)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.VERSION.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(MBUS_DEVICE_TYPE)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.DEVICE_TYPE.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(MBUS_ACCESS_NUMBER)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.ACCESS_NUMBER.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(MBUS_STATUS)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.STATUS.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(MBUS_ALARM)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.ALARM.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(MBUS_ENCRYPTION_KEY_STATUS)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.ENCRYPTION_KEY_STATUS.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(MBUS_FUAK_STATUS)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.FUAK_STATUS.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(DEVICE_ID_1)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(DEVICE_ID_2)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(DEVICE_ID_5)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(LTE_SETUP_APN)) {
                    ObisCode base = getLTESetupBase(rObisCode);
                    this.registerMap.put(register, new DLMSAttribute(base, LTESetupAttributes.APN.getAttributeNumber(), DLMSClassId.GPRS_SETUP));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(LTE_SETUP_PIN)) {
                    ObisCode base = getLTESetupBase(rObisCode);
                    this.registerMap.put(register, new DLMSAttribute(base, LTESetupAttributes.PIN_Code.getAttributeNumber(), DLMSClassId.GPRS_SETUP));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(LTE_SETUP_QOS)) {
                    ObisCode base = getLTESetupBase(rObisCode);
                    this.registerMap.put(register, new DLMSAttribute(base, LTESetupAttributes.QoS.getAttributeNumber(), DLMSClassId.GPRS_SETUP));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(PPP_SETUP_AUTH_USERNAME) || rObisCode.equals(PPP_SETUP_AUTH_PASSWORD)) {
                    this.registerMap.put(register, new DLMSAttribute(PPP_SETUP_base, PPPSetupAttributes.PPP_AUTHENTICATION.getAttributeNumber(), DLMSClassId.PPP_SETUP.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(BILLING_PERIOD)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.SINGLE_ACTION_SCHEDULE));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(MBUS_ENCRYPTION_STATUS)) {
                    this.registerMap.put(register, new DLMSAttribute(adjustToMbusOC(rObisCode), MBusClientAttributes.ENCRYPTION_KEY_STATUS.getAttributeNumber(), DLMSClassId.MBUS_CLIENT));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(POWER_LONG_FAILURE_STATUS)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, DLMSClassId.DATA));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(POWER_FAILURE_STATUS)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, DLMSClassId.DATA));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(PHY_RANDOMISATION)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, DLMSClassId.DATA));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(ACTIVE_CORE_METER_FIRMWARE_VERSION_OBISCODE)) {
                    this.registerMap.put(register, new DLMSAttribute(ACTIVE_CORE_METER_FIRMWARE_VERSION_OBISCODE, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(ACTIVE_MODEM_FIRMWARE_VERSION_OBISCODE)) {
                    this.registerMap.put(register, new DLMSAttribute(ACTIVE_MODEM_FIRMWARE_VERSION_OBISCODE, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(MODEM_FIRMWARE_SIGNATURE_OBISCODE)) {
                    this.registerMap.put(register, new DLMSAttribute(MODEM_FIRMWARE_SIGNATURE_OBISCODE, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(AUXILIARY_FIRMWARE_VERSION)) {
                    this.registerMap.put(register, new DLMSAttribute(AUXILIARY_FIRMWARE_VERSION, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_OPERATOR)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.OPERATOR.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_STATUS)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.STATUS.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_CS_ATTACHMENT)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.CS_ATTACHMENT.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_PS_STATUS)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.PS_STATUS.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_CELL_INFO_BASE)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.CELL_INFO_BASE.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_CELL_INFO_CELL_ID)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.CELL_INFO_CELL_ID.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_CELL_INFO_LOCATION_ID)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.CELL_INFO_LOCATION_ID.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_CELL_INFO_SIGNAL_QUALITY)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.CELL_INFO_SIGNAL_QUALITY.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_CELL_INFO_BER)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.CELL_INFO_BER.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_CELL_INFO_MCC)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.CELL_INFO_MCC.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_CELL_INFO_MNC)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.CELL_INFO_MNC.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_CELL_INFO_CHANNEL_NUMBER)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.CELL_INFO_CHANNEL_NUMBER.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_ADJACENT_CELLS_BASE)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.ADJACENT_CELLS_BASE.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_ADJACENT_CELLS_CELLID)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.ADJACENT_CELLS_CELL_ID.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(GSM_DIAGNOSTIC_ADJACENT_CELLS_SIGNAL_QUALITY)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.ADJACENT_CELLS_SIGNAL_QUALITY.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(GSM_DIAGNOSTIC_CAPTURE_TIME)) {
                    this.registerMap.put(register, new DLMSAttribute(GSM_DIAGNOSTIC_BASE, GSMDiagnosticAttributes.CAPTURE_TIME.getAttributeNumber(), DLMSClassId.GSM_DIAGNOSTICS));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(LTE_CONNECTION_REJECTION_BASE)) {
                    this.registerMap.put(register, new DLMSAttribute(LTE_CONNECTION_REJECTION_BASE, LTEConnectionRejectionAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(LTE_CONNECTION_REJECTION_LAST_REJECTED_CAUSE)) {
                    this.registerMap.put(register, new DLMSAttribute(LTE_CONNECTION_REJECTION_BASE, LTEConnectionRejectionAttributes.LAST_REJECT_CAUSE.getAttributeNumber(), DLMSClassId.DATA));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(LTE_CONNECTION_REJECTION_LAST_REJECTED_MCC)) {
                    this.registerMap.put(register, new DLMSAttribute(LTE_CONNECTION_REJECTION_BASE, LTEConnectionRejectionAttributes.LAST_REJECTED_MCC.getAttributeNumber(), DLMSClassId.DATA));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(LTE_CONNECTION_REJECTION_LAST_REJECTED_MNC)) {
                    this.registerMap.put(register, new DLMSAttribute(LTE_CONNECTION_REJECTION_BASE, LTEConnectionRejectionAttributes.LAST_REJECTED_MNC.getAttributeNumber(), DLMSClassId.DATA));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(LTE_CONNECTION_REJECTION_TIMESTAMP_LAST_REJECTION)) {
                    this.registerMap.put(register, new DLMSAttribute(LTE_CONNECTION_REJECTION_BASE, LTEConnectionRejectionAttributes.TIMESTAMP_LAST_REJECTION.getAttributeNumber(), DLMSClassId.DATA));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(MBUS_DEVICE_CONFIGURATION)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, ExtendedRegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.EXTENDED_REGISTER));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(MBUS_DEVICE_CONFIGURATION_METEROLOGICAL_FIRMWARE)) {
                    this.registerMap.put(register, new DLMSAttribute(this.protocol.getPhysicalAddressCorrectedObisCode(MBUS_DEVICE_CONFIGURATION, register.getSerialNumber()), ExtendedRegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.EXTENDED_REGISTER));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(MBUS_DEVICE_CONFIGURATION_OPERATIONAL_FIRMWARE)) {
                    this.registerMap.put(register, new DLMSAttribute(this.protocol.getPhysicalAddressCorrectedObisCode(MBUS_DEVICE_CONFIGURATION, register.getSerialNumber()), ExtendedRegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.EXTENDED_REGISTER));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(MBUS_DEVICE_CONFIGURATION_ADDITIONAL_FIRMWARE)) {
                    this.registerMap.put(register, new DLMSAttribute(this.protocol.getPhysicalAddressCorrectedObisCode(MBUS_DEVICE_CONFIGURATION, register.getSerialNumber()), ExtendedRegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.EXTENDED_REGISTER));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(LTE_MONITORING_BASE)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, LTEMonitoringAttributes.LTE_QOS.getAttributeNumber(), DLMSClassId.LTE_MONITORING));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3402)) { //TODO create LTEMonitoringAttributes.T3402.getAttributeNumber()
                    this.registerMap.put(register, new DLMSAttribute(LTE_MONITORING_BASE, 21, DLMSClassId.LTE_MONITORING));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3412)) { //TODO create LTEMonitoringAttributes.T3412.getAttributeNumber()
                    this.registerMap.put(register, new DLMSAttribute(LTE_MONITORING_BASE, 22, DLMSClassId.LTE_MONITORING));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRQ)) { //TODO create LTEMonitoringAttributes.RSRQ.getAttributeNumber()
                    this.registerMap.put(register, new DLMSAttribute(LTE_MONITORING_BASE, 23, DLMSClassId.LTE_MONITORING));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRP)) { //TODO create LTEMonitoringAttributes.RSRP.getAttributeNumber()
                    this.registerMap.put(register, new DLMSAttribute(LTE_MONITORING_BASE, 24, DLMSClassId.LTE_MONITORING));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_Q_RXLEV_MIN)) { //TODO create LTEMonitoringAttributes.QRXLEVMIN.getAttributeNumber()
                    this.registerMap.put(register, new DLMSAttribute(LTE_MONITORING_BASE, 25, DLMSClassId.LTE_MONITORING));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(DEFINABLE_LOAD_PROFILE_CAPTURE_OBJECTS)) {
                    this.registerMap.put(register, new DLMSAttribute(DEFINABLE_LOAD_PROFILE_BASE, DefinableLoadProfileAttributes.CAPTURE_OBJECTS.getAttributeNumber(), DLMSClassId.PROFILE_GENERIC));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equals(DEFINABLE_LOAD_PROFILE_CAPTURE_PERIOD)) {
                    this.registerMap.put(register, new DLMSAttribute(DEFINABLE_LOAD_PROFILE_BASE, DefinableLoadProfileAttributes.CAPTURE_PERIOD.getAttributeNumber(), DLMSClassId.PROFILE_GENERIC));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equals(LTE_PING_ADDRESS)){
                    this.registerMap.put(register, new DLMSAttribute(LTE_PING_ADDRESS, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else {
                    registersForSuper.add(register);
                }
            } // for - registers

            ComposedCosemObject sRegisterList = super.constructComposedObjectFromRegisterList(registersForSuper, supportsBulkRequest);
            if (sRegisterList != null) {
                dlmsAttributes.addAll(Arrays.asList(sRegisterList.getDlmsAttributesList()));
            }

            return new ComposedCosemObject(protocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }
        return null;
    }

    // in SagemCom ver 2.32 the obis code from the meter is inocorrect
    private ObisCode getLTESetupBase(ObisCode rObisCode) {
        return new ObisCode(
                LTE_SETUP_base.getA(),
                rObisCode.getB(), // in the meter now it's 0 instead of 1 (as in spec) - was tweaked in EIServer
                LTE_SETUP_base.getC(),
                LTE_SETUP_base.getD(),
                LTE_SETUP_base.getE(),
                LTE_SETUP_base.getF());
    }

    @Override
    protected RegisterValue readComposedRegister(ComposedCosemObject registerComposedCosemObject, OfflineRegister register) throws IOException {
        if (register.getObisCode().equalsIgnoreBChannel(MBUS_DIAGNOSTIC)){
            return readMBUSDiagnosticsComposedRegister(registerComposedCosemObject, register);
        } else {
            return super.readComposedRegister(registerComposedCosemObject, register);
        }

    }

    private RegisterValue readMBUSDiagnosticsComposedRegister(ComposedCosemObject registerComposedCosemObject, OfflineRegister register) {
        try {
            ScalerUnit su = new ScalerUnit(Unit.get(DECIBELMILLIWAT)); // dbm = 70;
            protocol.journal("Handling MBUS Diagnostic: "+register.getObisCode().toString());
            DLMSAttribute registerCaptureTime = this.getComposedRegisterMap().get(register).getRegisterCaptureTime();
            protocol.journal("  > fetching capture_time :"+registerCaptureTime.toString());
            AbstractDataType attribute = registerComposedCosemObject.getAttribute(registerCaptureTime);
            if (attribute.isStructure()) {
                Structure structure = attribute.getStructure();
                structure.getNextDataType();
                AbstractDataType captureStructure = structure.getNextDataType();
                if(isValidDateTime(captureStructure)){
                    Date captureTime = ((DateTime) captureStructure).getCalendar(protocol.getDlmsSession().getTimeZone()).getTime();
                    protocol.journal(FINEST, "  > decoded captured time = " + captureTime.toString());
                    return new RegisterValue(
                            register,
                            new Quantity(registerComposedCosemObject.getAttribute(this.getComposedRegisterMap().get(register).getRegisterValueAttribute()).toBigDecimal(),
                                    su.getEisUnit()), captureTime);
                }
                else {
                    protocol.journal("decoded capture time is not a valid date");
                    protocol.journal("Creating register without timestamp.");
                    return new RegisterValue(
                            register,
                            new Quantity(registerComposedCosemObject.getAttribute(this.getComposedRegisterMap().get(register).getRegisterValueAttribute()).toBigDecimal(),
                                    su.getEisUnit()));
                }
            } else {
                protocol.journal(WARNING, "Data in MBus Diagnostic object from attribute #"+registerCaptureTime.getAttribute()+" of "+register.getObisCode()+" is not a structure, as expected from specs. The capture time will be current date/time.");
                return new RegisterValue(register);

            }

        } catch (Exception ex){
            protocol.journal(WARNING, "Could not handle composed register MBusDiagnostic "+register.getObisCode()+": "+ex.getMessage());
            return new RegisterValue(register);
        }

    }

    private boolean isValidDateTime(AbstractDataType captureStructure) {
        return captureStructure instanceof DateTime && ((DateTime) captureStructure).isValidDate();
    }
}
