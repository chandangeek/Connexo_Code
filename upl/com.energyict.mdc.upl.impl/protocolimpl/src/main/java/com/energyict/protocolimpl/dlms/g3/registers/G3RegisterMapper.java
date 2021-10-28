package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.G3ProfileType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 21/03/12
 * Time: 15:25
 */
public class G3RegisterMapper {

    /**
     * Billing period mappings (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 2)
     */
    private static final ObisCode ASYNC_EOB_SCHEDULE_ALL = ObisCode.fromString("0.3.15.0.0.255");
    private static final ObisCode ASYNC_EOB_SCHEDULE_TYPE = ObisCode.fromString("0.3.15.0.1.255");
    private static final ObisCode ASYNC_EOB_SCHEDULE_TIME = ObisCode.fromString("0.3.15.0.2.255");
    private static final ObisCode DAILY_EOB_SCHEDULE_ALL = ObisCode.fromString("0.2.15.0.0.255");
    private static final ObisCode DAILY_EOB_SCHEDULE_TYPE = ObisCode.fromString("0.2.15.0.1.255");
    private static final ObisCode DAILY_EOB_SCHEDULE_TIME = ObisCode.fromString("0.2.15.0.2.255");
    private static final ObisCode MONTHLY_EOB_SCHEDULE_ALL = ObisCode.fromString("0.1.15.0.0.255");
    private static final ObisCode MONTHLY_EOB_SCHEDULE_TYPE = ObisCode.fromString("0.1.15.0.1.255");
    private static final ObisCode MONTHLY_EOB_SCHEDULE_TIME = ObisCode.fromString("0.1.15.0.2.255");
    /**
     * Breaker management mappings (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 3)
     */

    private static final ObisCode BREAKER_STATE = ObisCode.fromString("0.0.96.3.10.255");
    private static final ObisCode DOWNSTREAM_VOLTAGE_MONITORING = ObisCode.fromString("1.0.12.35.3.255");
    private static final ObisCode OVERHEAT_CURRENT_LIMIT = ObisCode.fromString("1.0.0.12.1.255");
    private static final ObisCode OVERHEAT_DURATION_LIMIT = ObisCode.fromString("1.0.0.12.2.255");
    /**
     * Communication (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 4)
     */

    private static final ObisCode CONSUMER_INTERFACE_SETUP = ObisCode.fromString("0.0.94.33.0.255");
    private static final ObisCode EURIDIS_PROTOCOL_SETUP = ObisCode.fromString("0.0.23.1.0.255");
    /**
     * Database mappings (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 5)
     */

    private static final ObisCode FIRMWARE_ENTRIES = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode FIRMWARE_ENTRY_1 = ObisCode.fromString("1.0.0.2.1.255");
    private static final ObisCode FIRMWARE_ENTRY_2 = ObisCode.fromString("1.0.0.2.2.255");
    private static final ObisCode FIRMWARE_ENTRY_3 = ObisCode.fromString("1.0.0.2.3.255");
    private static final ObisCode FIRMWARE_ENTRY_4 = ObisCode.fromString("1.0.0.2.4.255");
    private static final ObisCode FIRMWARE_ENTRY_5 = ObisCode.fromString("1.0.0.2.5.255");
    private static final ObisCode LOGICAL_DEVICE_NAME = ObisCode.fromString("0.0.42.0.0.255");
    private static final ObisCode PRODUCER_CONSUMER_CODE = ObisCode.fromString("1.0.96.63.11.255");
    private static final ObisCode TEST_MODE_CODE = ObisCode.fromString("1.0.96.63.12.255");
    /**
     * Event manager mappings (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 7)
     */

    private static final ObisCode ALARM_FILTER = ObisCode.fromString("0.0.97.98.10.255");
    private static final ObisCode ALARM_REGISTER = ObisCode.fromString("0.0.97.98.0.255");
    private static final ObisCode BREAKER_EVENT_CODE = ObisCode.fromString("0.3.96.11.0.255");
    private static final ObisCode BREAKER_OPENING_COUNTER = ObisCode.fromString("0.0.96.15.1.255");
    private static final ObisCode BREAKER_OPENING_COUNTER_OVER_MAX_OPENING_CURRENT = ObisCode.fromString("0.0.96.15.2.255");
    private static final ObisCode COMMUNICATION_EVENT_CODE = ObisCode.fromString("0.4.96.11.0.255");
    private static final ObisCode COVER_EVENT_CODE = ObisCode.fromString("0.2.96.11.0.255");
    private static final ObisCode PEAK_EVENT_CODE = ObisCode.fromString("0.6.96.11.0.255");
    private static final ObisCode COVER_OPENING_COUNTER = ObisCode.fromString("0.0.96.15.0.255");
    private static final ObisCode ERROR_REGISTER = ObisCode.fromString("0.0.97.97.0.255");
    private static final ObisCode IMAGE_TRANSFER_COUNTER = ObisCode.fromString("0.0.96.63.10.255");
    private static final ObisCode MAIN_EVENT_CODE = ObisCode.fromString("0.1.96.11.0.255");
    private static final ObisCode NUMBER_OF_PROGRAMMING = ObisCode.fromString("0.0.96.2.0.255");
    private static final ObisCode STATUS_REGISTER = ObisCode.fromString("1.0.96.5.1.255");
    private static final ObisCode VOLTAGE_CUT_EVENT_CODE = ObisCode.fromString("0.5.96.11.0.255");
    private static final ObisCode THR_DOWNSTREAM_IMPEDANCE = ObisCode.fromString("1.0.96.31.0.255");
    /**
     * Total energy registry mappings (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 15)
     */

    private static final ObisCode TOTAL_EXPORT_ACTIVE_ENERGY = ObisCode.fromString("1.1.2.8.0.255");
    private static final ObisCode TOTAL_IMPORT_ACTIVE_ENERGY = ObisCode.fromString("1.1.1.8.0.255");
    private static final ObisCode TOTAL_REACTIVE_Q1_ENERGY = ObisCode.fromString("1.1.5.8.0.255");
    private static final ObisCode TOTAL_REACTIVE_Q2_ENERGY = ObisCode.fromString("1.1.6.8.0.255");
    private static final ObisCode TOTAL_REACTIVE_Q3_ENERGY = ObisCode.fromString("1.1.7.8.0.255");
    private static final ObisCode TOTAL_REACTIVE_Q4_ENERGY = ObisCode.fromString("1.1.8.8.0.255");
    private static final ObisCode ACTIVE_E_CONSIST_CHK_THR = ObisCode.fromString("1.0.11.31.0.255");
    private static final ObisCode DAILY_MAX_POWER = ObisCode.fromString("1.1.9.19.0.255");
    private static final ObisCode TARIFF1_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.1.255");
    private static final ObisCode TARIFF2_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.2.255");
    private static final ObisCode TARIFF3_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.3.255");
    private static final ObisCode TARIFF4_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.4.255");
    private static final ObisCode TARIFF5_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.5.255");
    private static final ObisCode TARIFF6_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.6.255");
    private static final ObisCode TARIFF7_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.7.255");
    private static final ObisCode TARIFF8_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.8.255");
    private static final ObisCode TARIFF9_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.9.255");
    private static final ObisCode TARIFF10_IMPORT_ACTIVE_ENERGY_PROVIDER = ObisCode.fromString("1.1.1.8.10.255");
    private static final ObisCode TARIFF1_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK = ObisCode.fromString("1.2.1.8.1.255");
    private static final ObisCode TARIFF2_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK = ObisCode.fromString("1.2.1.8.2.255");
    private static final ObisCode TARIFF3_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK = ObisCode.fromString("1.2.1.8.3.255");
    private static final ObisCode TARIFF4_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK = ObisCode.fromString("1.2.1.8.4.255");
    /**
     * Voltage quality measurement mappings (ERDF-CPT-Linky-SPEC-FONC-CPT-MDD, V1.2, 16)
     */

    private static final ObisCode AVG_ABNORMAL_VOLTAGE_PH1 = ObisCode.fromString("1.1.32.5.0.255");
    private static final ObisCode AVG_ABNORMAL_VOLTAGE_PH2 = ObisCode.fromString("1.1.52.5.0.255");
    private static final ObisCode AVG_ABNORMAL_VOLTAGE_PH3 = ObisCode.fromString("1.1.72.5.0.255");
    private static final ObisCode PH_WITH_ABNORMAL_VOLTAGE = ObisCode.fromString("1.0.12.38.0.255");
    private static final ObisCode VOLTAGE_CUT_MINIMUM_DURATION = ObisCode.fromString("1.0.12.45.0.255");
    /**
     * PLC Statistics mappings, B-field indicates the attribute number
     */
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR1 = ObisCode.fromString("0.1.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR2 = ObisCode.fromString("0.2.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR3 = ObisCode.fromString("0.3.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR4 = ObisCode.fromString("0.4.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR5 = ObisCode.fromString("0.5.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR6 = ObisCode.fromString("0.6.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR7 = ObisCode.fromString("0.7.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR8 = ObisCode.fromString("0.8.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR9 = ObisCode.fromString("0.9.29.0.0.255");
    private static final ObisCode PHYS_MAC_LAYER_COUNTERS_ATTR10 = ObisCode.fromString("0.10.29.0.0.255");
    private static final ObisCode MAC_SETUP_ATTR1 = ObisCode.fromString("0.1.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR2 = ObisCode.fromString("0.2.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR3 = ObisCode.fromString("0.3.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR4 = ObisCode.fromString("0.4.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR5 = ObisCode.fromString("0.5.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR6 = ObisCode.fromString("0.6.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR7 = ObisCode.fromString("0.7.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR8 = ObisCode.fromString("0.8.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR9 = ObisCode.fromString("0.9.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR10 = ObisCode.fromString("0.10.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR11 = ObisCode.fromString("0.11.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR12 = ObisCode.fromString("0.12.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR13 = ObisCode.fromString("0.13.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR14 = ObisCode.fromString("0.14.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR15 = ObisCode.fromString("0.15.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR16 = ObisCode.fromString("0.16.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR17 = ObisCode.fromString("0.17.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR18 = ObisCode.fromString("0.18.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR19 = ObisCode.fromString("0.19.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR20 = ObisCode.fromString("0.20.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR21 = ObisCode.fromString("0.21.29.1.0.255");
    private static final ObisCode MAC_SETUP_ATTR22 = ObisCode.fromString("0.22.29.1.0.255");

    private static final ObisCode SIXLOWPAN_SETUP_ATTR1 = ObisCode.fromString("0.1.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR2 = ObisCode.fromString("0.2.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR3 = ObisCode.fromString("0.3.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR4 = ObisCode.fromString("0.4.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR5 = ObisCode.fromString("0.5.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR6 = ObisCode.fromString("0.6.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR7 = ObisCode.fromString("0.7.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR8 = ObisCode.fromString("0.8.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR9 = ObisCode.fromString("0.9.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR10 = ObisCode.fromString("0.10.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR11 = ObisCode.fromString("0.11.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR12 = ObisCode.fromString("0.12.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR13 = ObisCode.fromString("0.13.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR14 = ObisCode.fromString("0.14.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR15 = ObisCode.fromString("0.15.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR16 = ObisCode.fromString("0.16.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR17 = ObisCode.fromString("0.17.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR18 = ObisCode.fromString("0.18.29.2.0.255");
    private static final ObisCode SIXLOWPAN_SETUP_ATTR19 = ObisCode.fromString("0.19.29.2.0.255");

    private static final ObisCode EVENT_NOTIFICATION_ATTR1_LEGACY = ObisCode.fromString("0.1.128.0.12.255");
    private static final ObisCode EVENT_NOTIFICATION_ATTR2_LEGACY = ObisCode.fromString("0.2.128.0.12.255");
    private static final ObisCode EVENT_NOTIFICATION_ATTR4_LEGACY = ObisCode.fromString("0.4.128.0.12.255");

    private static final ObisCode PUSH_SETUP_ATTR1 = ObisCode.fromString("0.0.25.9.0.1");
    private static final ObisCode PUSH_SETUP_ATTR3 = ObisCode.fromString("0.0.25.9.0.3");
    private static final ObisCode PUSH_SETUP_ATTR6 = ObisCode.fromString("0.0.25.9.0.6");
    private static final ObisCode PUSH_SETUP_ATTR7 = ObisCode.fromString("0.0.25.9.0.7");
    private static final ObisCode PUSH_SETUP_ATTRc2 = ObisCode.fromString("0.0.25.9.0.254");
    private static final ObisCode PUSH_SETUP_ATTRc3 = ObisCode.fromString("0.0.25.9.0.253");
    private static final ObisCode PUSH_SETUP_ATTRc4 = ObisCode.fromString("0.0.25.9.0.252");
    private static final ObisCode PUSH_SETUP_ATTRc5 = ObisCode.fromString("0.0.25.9.0.251");

    private static final ObisCode WAN_DL_REFERENCE          = ObisCode.fromString("0.0.25.1.2.255");
    public static final ObisCode WAN_IP_ADDRESS            = ObisCode.fromString("0.0.25.1.3.255");
    private static final ObisCode WAN_MULTICAST_IP_ADDRESS  = ObisCode.fromString("0.0.25.1.4.255");
    private static final ObisCode WAN_IP_OPTIONS            = ObisCode.fromString("0.0.25.1.5.255");
    public static final ObisCode WAN_SUBNET_MASK           = ObisCode.fromString("0.0.25.1.6.255");
    private static final ObisCode WAN_GATEWAY_IP_ADDRESS    = ObisCode.fromString("0.0.25.1.7.255");
    private static final ObisCode WAN_USE_DHCP_FLAG         = ObisCode.fromString("0.0.25.1.8.255");
    public static final ObisCode WAN_PRIMARY_DNS_ADDRESS   = ObisCode.fromString("0.0.25.1.9.255");
    public static final ObisCode WAN_SECONDARY_DNS_ADDRESS = ObisCode.fromString("0.0.25.1.10.255");

    private static final ObisCode LAN_DL_REFERENCE          = ObisCode.fromString("0.1.25.1.2.255");
    private static final ObisCode LAN_IP_ADDRESS            = ObisCode.fromString("0.1.25.1.3.255");
    private static final ObisCode LAN_MULTICAST_IP_ADDRESS  = ObisCode.fromString("0.1.25.1.4.255");
    private static final ObisCode LAN_IP_OPTIONS            = ObisCode.fromString("0.1.25.1.5.255");
    private static final ObisCode LAN_SUBNET_MASK           = ObisCode.fromString("0.1.25.1.6.255");
    private static final ObisCode LAN_GATEWAY_IP_ADDRESS    = ObisCode.fromString("0.1.25.1.7.255");
    private static final ObisCode LAN_USE_DHCP_FLAG         = ObisCode.fromString("0.1.25.1.8.255");
    private static final ObisCode LAN_PRIMARY_DNS_ADDRESS   = ObisCode.fromString("0.1.25.1.9.255");
    private static final ObisCode LAN_SECONDARY_DNS_ADDRESS = ObisCode.fromString("0.1.25.1.10.255");

    public  static final ObisCode LOOPBACK_IP_ADDRESS       = ObisCode.fromString("0.2.25.1.3.255");

    private static final ObisCode WWAN_DL_REFERENCE         = ObisCode.fromString("0.3.25.1.2.255");
    public static final ObisCode WWAN_IP_ADDRESS           = ObisCode.fromString("0.3.25.1.3.255");
    private static final ObisCode WWAN_MULTICAST_IP_ADDRESS = ObisCode.fromString("0.3.25.1.4.255");
    private static final ObisCode WWAN_IP_OPTIONS           = ObisCode.fromString("0.3.25.1.5.255");
    private static final ObisCode WWAN_SUBNET_MASK          = ObisCode.fromString("0.3.25.1.6.255");
    private static final ObisCode WWAN_GATEWAY_IP_ADDRESS   = ObisCode.fromString("0.3.25.1.7.255");
    private static final ObisCode WWAN_USE_DHCP_FLAG        = ObisCode.fromString("0.3.25.1.8.255");
    public static final ObisCode WWAN_PRIMARY_DNS_ADDRESS  = ObisCode.fromString("0.3.25.1.9.255");
    public static final ObisCode WWAN_SECONDARY_DNS_ADDRESS = ObisCode.fromString("0.3.25.1.10.255");

    private static final ObisCode USB_SETUP_ATTR1 = ObisCode.fromString("0.194.96.144.0.1");
    private static final ObisCode USB_SETUP_ATTR2 = ObisCode.fromString("0.194.96.144.0.2");
    private static final ObisCode USB_SETUP_ATTR3 = ObisCode.fromString("0.194.96.144.0.3");
    private static final ObisCode USB_SETUP_ATTR4 = ObisCode.fromString("0.194.96.144.0.4");
    private static final ObisCode USB_SETUP_ATTR1_LEGACY = ObisCode.fromString("0.0.128.0.28.1");
    private static final ObisCode USB_SETUP_ATTR2_LEGACY = ObisCode.fromString("0.0.128.0.28.2");
    private static final ObisCode USB_SETUP_ATTR3_LEGACY = ObisCode.fromString("0.0.128.0.28.3");
    private static final ObisCode USB_SETUP_ATTR4_LEGACY = ObisCode.fromString("0.0.128.0.28.4");

    public static final ObisCode DISCONNECT_CONTROL_ATTR1 = ObisCode.fromString("0.1.96.3.10.255");
    public static final ObisCode DISCONNECT_CONTROL_ATTR2 = ObisCode.fromString("0.2.96.3.10.255");
    public static final ObisCode DISCONNECT_CONTROL_ATTR3 = ObisCode.fromString("0.3.96.3.10.255");
    public static final ObisCode DISCONNECT_CONTROL_ATTR4 = ObisCode.fromString("0.4.96.3.10.255");

    public static final ObisCode GPRS_MODEM_SETUP_ATTR1 = ObisCode.fromString("0.1.25.4.0.255");
    public static final ObisCode GPRS_MODEM_SETUP_ATTR2 = ObisCode.fromString("0.2.25.4.0.255");
    public static final ObisCode GPRS_MODEM_SETUP_ATTR3 = ObisCode.fromString("0.3.25.4.0.255");
    public static final ObisCode GPRS_MODEM_SETUP_ATTR4 = ObisCode.fromString("0.4.25.4.0.255");
    public static final ObisCode GPRS_MODEM_SETUP_ATTR5 = ObisCode.fromString("0.5.25.4.0.255");
    public static final ObisCode GPRS_MODEM_SETUP_ATTR6 = ObisCode.fromString("0.6.25.4.0.255");
    public static final ObisCode GPRS_MODEM_SETUP_ATTR7 = ObisCode.fromString("0.7.25.4.0.255");
    public static final ObisCode GPRS_MODEM_SETUP_ATTR8 = ObisCode.fromString("0.8.25.4.0.255");
    public static final ObisCode GPRS_MODEM_SETUP_ATTR9 = ObisCode.fromString("0.9.25.4.0.255");
    public static final ObisCode GPRS_MODEM_SETUP_ATTR10 = ObisCode.fromString("0.10.25.4.0.255");
    public static final ObisCode GPRS_MODEM_SETUP_ATTR11 = ObisCode.fromString("0.11.25.4.0.255");

    public static final ObisCode PPP_SETUP_ATTR1 = ObisCode.fromString("0.1.25.3.0.255");
    public static final ObisCode PPP_SETUP_ATTR2 = ObisCode.fromString("0.2.25.3.0.255");
    public static final ObisCode PPP_SETUP_ATTR3 = ObisCode.fromString("0.3.25.3.0.255");
    public static final ObisCode PPP_SETUP_ATTR4 = ObisCode.fromString("0.4.25.3.0.255");
    public static final ObisCode PPP_SETUP_ATTR5 = ObisCode.fromString("0.5.25.3.0.255");
    public static final ObisCode PPP_SETUP_ATTR6 = ObisCode.fromString("0.6.25.3.0.255");

    // Beacon custom class for Modem Watchdog parameters
    public static final ObisCode MODEM_WATCHDOG_ATTR1 = ObisCode.fromString("0.162.96.128.0.1");
    public static final ObisCode MODEM_WATCHDOG_ATTR2 = ObisCode.fromString("0.162.96.128.0.2");
    public static final ObisCode MODEM_WATCHDOG_ATTR3 = ObisCode.fromString("0.162.96.128.0.3");
    public static final ObisCode MODEM_WATCHDOG_ATTR1_LEGACY = ObisCode.fromString("0.0.128.0.11.1");
    public static final ObisCode MODEM_WATCHDOG_ATTR2_LEGACY = ObisCode.fromString("0.0.128.0.11.2");
    public static final ObisCode MODEM_WATCHDOG_ATTR3_LEGACY = ObisCode.fromString("0.0.128.0.11.3");


    public static final ObisCode MULTI_APN_CONFIG = ObisCode.fromString("0.162.96.160.0.255");
    public static final ObisCode MULTI_APN_CONFIG_LEGACY = ObisCode.fromString("0.128.25.3.0.255");

    /**
     * ObisCode mappers for the ConcentratorSetup attributes, found on base OBISCode 0.187.96.128.0.255
     * https://confluence.eict.vpdc/display/G3IntBeacon3100/Manage+DC+operations
     */
    public static final ObisCode CONCENTRATOR_SETUP_ACTIVE                      = ObisCode.fromString("0.187.96.128.0.2");
    public static final ObisCode CONCENTRATOR_SETUP_MAX_CONCURENT_SESSIONS      = ObisCode.fromString("0.187.96.128.0.3");
    public static final ObisCode CONCENTRATOR_SETUP_METER_INFO_JSON             = ObisCode.fromString("0.187.96.128.0.4");
    public static final ObisCode CONCENTRATOR_SETUP_METER_INFO_SERIAL           = ObisCode.fromString("0.187.96.128.0.41");
    public static final ObisCode CONCENTRATOR_SETUP_METER_INFO_MAC              = ObisCode.fromString("0.187.96.128.0.42");
    public static final ObisCode CONCENTRATOR_SETUP_PROTOCOL_EVENT_LOG_LEVEL    = ObisCode.fromString("0.187.96.128.0.5");

    /**
     * ObisCode mappers for the MemoryManagement attributes, found on 0.194.96.128.0.255 classId 20021
     */
    public static final ObisCode MEMORY_MANAGEMENT_ATTR2 = ObisCode.fromString("0.194.96.128.0.2");
    public static final ObisCode MEMORY_MANAGEMENT_ATTR3 = ObisCode.fromString("0.194.96.128.0.3");
    public static final ObisCode MEMORY_MANAGEMENT_ATTR2_LEGACY = ObisCode.fromString("0.0.128.0.20.2");
    public static final ObisCode MEMORY_MANAGEMENT_ATTR3_LEGACY = ObisCode.fromString("0.0.128.0.20.3");

    /**
     * ObisCode mappers for the Image transfer attributes
     */
    public static final ObisCode IMAGE_TRANSFER_ATTR2 = ObisCode.fromString("0.2.44.0.0.255");
    public static final ObisCode IMAGE_TRANSFER_ATTR3 = ObisCode.fromString("0.3.44.0.0.255");
    public static final ObisCode IMAGE_TRANSFER_ATTR4 = ObisCode.fromString("0.4.44.0.0.255");
    public static final ObisCode IMAGE_TRANSFER_ATTR5 = ObisCode.fromString("0.5.44.0.0.255");
    public static final ObisCode IMAGE_TRANSFER_ATTR6 = ObisCode.fromString("0.6.44.0.0.255");

    /**
     * ObisCode mappers for the Limiter attributes
     */
    public static final ObisCode LIMITER_ATTR4 = ObisCode.fromString("0.4.17.0.0.255");
    public static final ObisCode LIMITER_ATTR6 = ObisCode.fromString("0.6.17.0.0.255");
    public static final ObisCode LIMITER_ATTR7 = ObisCode.fromString("0.7.17.0.0.255");

    /**
     * ObisCode mappers for the End of billing period 1 scheduler attributes
     */
    public static final ObisCode BILLING_SCHEDULER_ATTR2 = ObisCode.fromString("0.2.15.0.0.255");
    public static final ObisCode BILLING_SCHEDULER_ATTR3 = ObisCode.fromString("0.3.15.0.0.255");
    public static final ObisCode BILLING_SCHEDULER_ATTR4 = ObisCode.fromString("0.4.15.0.0.255");

    /**
     * ObisCode mappers for Image transfer activation scheduler attributes
     */
    public static final ObisCode IMAGE_TRANSFER_ACTIVATION_SCHEDULER_ATTR2 = ObisCode.fromString("0.2.15.0.2.255");
    public static final ObisCode IMAGE_TRANSFER_ACTIVATION_SCHEDULER_ATTR3 = ObisCode.fromString("0.3.15.0.2.255");
    public static final ObisCode IMAGE_TRANSFER_ACTIVATION_SCHEDULER_ATTR4 = ObisCode.fromString("0.4.15.0.2.255");

    /**
     * ObisCode mappers for Disconnect control scheduler attributes
     */
    public static final ObisCode DISCONNECT_CONTROL_SCHEDULER_ATTR2 = ObisCode.fromString("0.2.15.0.1.255");
    public static final ObisCode DISCONNECT_CONTROL_SCHEDULER_ATTR3 = ObisCode.fromString("0.3.15.0.1.255");
    public static final ObisCode DISCONNECT_CONTROL_SCHEDULER_ATTR4 = ObisCode.fromString("0.4.15.0.1.255");

    /**
     * ObisCode mappers for NTP Setup
     */
    public static final ObisCode NTP_SETUP_ATTR1 = ObisCode.fromString("0.1.25.10.0.255");
    public static final ObisCode NTP_SETUP_ATTR2 = ObisCode.fromString("0.2.25.10.0.255");
    public static final ObisCode NTP_SETUP_ATTR3 = ObisCode.fromString("0.3.25.10.0.255");
    public static final ObisCode NTP_SETUP_ATTR4 = ObisCode.fromString("0.4.25.10.0.255");

    /**
     * ObisCode mappers for SNMP Setup
     */
    public static final ObisCode SNMP_SETUP_ATTR1 = ObisCode.fromString("0.128.96.194.1.255");
    public static final ObisCode SNMP_SETUP_ATTR2 = ObisCode.fromString("0.128.96.194.2.255");
    public static final ObisCode SNMP_SETUP_ATTR3 = ObisCode.fromString("0.128.96.194.3.255");
    public static final ObisCode SNMP_SETUP_ATTR4 = ObisCode.fromString("0.128.96.194.4.255");
    public static final ObisCode SNMP_SETUP_ATTR5 = ObisCode.fromString("0.128.96.194.5.255");
    public static final ObisCode SNMP_SETUP_ATTR6 = ObisCode.fromString("0.128.96.194.6.255");
    public static final ObisCode SNMP_SETUP_ATTR7 = ObisCode.fromString("0.128.96.194.7.255");
    public static final ObisCode SNMP_SETUP_ATTR8 = ObisCode.fromString("0.128.96.194.8.255");
    public static final ObisCode SNMP_SETUP_ATTR9 = ObisCode.fromString("0.128.96.194.9.255");
    public static final ObisCode SNMP_SETUP_ATTR10 = ObisCode.fromString("0.128.96.194.10.255");

    /**
     * ObisCode mappers for NTP Setup
     */
    public static final ObisCode LAST_FIRMWARE_ACTIVATION_EXTENDED_ATTR2 = ObisCode.fromString("0.136.96.192.2.255");

    /**
     * ObisCode mappers for LTE Monitoring
     */
    public static final ObisCode LTE_MONITORING_ATTR2 = ObisCode.fromString("0.0.25.11.2.255");

    /**
     * ObisCode mappers for GSM Diagnostics IC
     */
    private static final ObisCode GSM_DIAGNOSTICS_ATTR2 = ObisCode.fromString("0.0.25.6.2.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR3 = ObisCode.fromString("0.0.25.6.3.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR4 = ObisCode.fromString("0.0.25.6.4.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR5 = ObisCode.fromString("0.0.25.6.5.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR6 = ObisCode.fromString("0.0.25.6.6.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR7 = ObisCode.fromString("0.0.25.6.7.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR8 = ObisCode.fromString("0.0.25.6.8.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR_MINUS_1 = ObisCode.fromString("0.0.25.6.254.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR_MINUS_2 = ObisCode.fromString("0.0.25.6.253.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR_MINUS_3 = ObisCode.fromString("0.0.25.6.252.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR_MINUS_4 = ObisCode.fromString("0.0.25.6.251.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR_MINUS_5 = ObisCode.fromString("0.0.25.6.250.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR_MINUS_6 = ObisCode.fromString("0.0.25.6.249.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR_MINUS_7 = ObisCode.fromString("0.0.25.6.248.255");
    private static final ObisCode GSM_DIAGNOSTICS_ATTR_MINUS_8 = ObisCode.fromString("0.0.25.6.247.255");

    /**
     * ObisCode mappers for WWAN state transition IC
     */
    private static final ObisCode WWAN_STATE_TRANSITION_ATTR2 = ObisCode.fromString("0.162.96.192.2.255");

    /**
     * ObisCode mappers for LTE Monitoring
     */
    public static final ObisCode G3_PLC_JOIN_REQ_TIMESTAMP = ObisCode.fromString("0.168.96.193.0.255");
    public static final ObisCode G3_PLC_JOIN_REQ_TIMESTAMP_ATTR2 = ObisCode.fromString("0.168.96.193.2.255");

    /**
     * ObisCode mapper for Specific G3-PLC Network Management Objects
     */
    public static final ObisCode G3_PLC_BANDPLAN = ObisCode.fromString("0.0.94.43.128.255");
    public static final ObisCode PSK_RENEWAL_OBISCODE = ObisCode.fromString("0.0.94.33.128.255");
    public static final ObisCode PSK_KEK_RENEWAL_OBISCODE = ObisCode.fromString("0.0.94.43.133.255");


    protected final List<G3Mapping> mappings = new ArrayList<G3Mapping>();
    private final Logger logger;
    private final CosemObjectFactory cosemObjectFactory;
    private final TimeZone deviceTimeZone;
    private G3StoredValues dailyStoredValues;
    private G3StoredValues monthlyStoredValues;

    /**
     * Constructor which should only be used to access the mappings,
     * <b>not</b> for actual readout of the registers!
     */
    public G3RegisterMapper() {
        initializeMappings();

        this.logger = null;
        this.cosemObjectFactory = null;
        this.deviceTimeZone = null;
    }

    /**
     * G3 register mapping, used to read ata from the meter as a register value
     */
    public G3RegisterMapper(CosemObjectFactory cosemObjectFactory, TimeZone deviceTimeZone, Logger logger) {
        this.logger = logger;
        this.cosemObjectFactory = cosemObjectFactory;
        this.deviceTimeZone = deviceTimeZone;

        initializeMappings();

        dailyStoredValues = new G3StoredValues(cosemObjectFactory, deviceTimeZone, G3ProfileType.DAILY_PROFILE.getObisCode(), true);
        monthlyStoredValues = new G3StoredValues(cosemObjectFactory, deviceTimeZone, G3ProfileType.MONTHLY_PROFILE.getObisCode(), false);
    }

    protected void initializeMappings() {
        this.mappings.addAll(getClockMappings());
        this.mappings.addAll(getNTPSetupMappings());
        this.mappings.addAll(getBillingPeriodMappings());
        this.mappings.addAll(getBreakerManagementMappings());
        this.mappings.addAll(getCommunicationMappings());
        this.mappings.addAll(getDataBaseMappings());
        this.mappings.addAll(getEventManagerMappings());
        this.mappings.addAll(getTotalEnergyRegistering());
        this.mappings.addAll(getRatedEnergyRegistering());
        this.mappings.addAll(getVoltageQualityMeasurements());
        this.mappings.addAll(getPLCStatisticsMappings());
        this.mappings.addAll(getUsbSetupMappings());
        this.mappings.addAll(getDisconnectControlRegistering());
        this.mappings.addAll(getGprsModemSetupMappings());
        this.mappings.addAll(getPPPSetupRegistering());
        this.mappings.addAll(getModemWatchdogMappings());
        this.mappings.addAll(getImageTransferMappings());
    }

    /**
     * Find the proper G3Mapping in the list of supported registers.
     * If not supported, returns null.
     */
    public G3Mapping getG3Mapping(ObisCode obisCode) {
        for (G3Mapping g3Mapping : getMappings()) {
            if (g3Mapping.getObisCode().equals(obisCode)) {
                return g3Mapping;
            }
        }
        return null;
    }

    public List<G3Mapping> getMappings() {
        return mappings;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    /**
     * Lookup the register mapper and use it to fetch the data and create a correct register mapping
     *
     * @param obisCode The obisCode of the data to read
     * @return The register value
     * @throws java.io.IOException
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        logger.fine("Attempting to read out register [" + obisCode.toString() + "]");
        if (obisCode.getF() != 255) {
            HistoricalValue historicalValue = getStoredValuesImpl(obisCode).getHistoricalValue(obisCode);
            return new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
        }

        for (G3Mapping mapping : mappings) {
            if (mapping.getObisCode().equals(obisCode)) {
                final RegisterValue registerValue = mapping.readRegister(cosemObjectFactory);
                if (registerValue != null) {
                    return registerValue;
                }
            }
        }
        throw new NoSuchRegisterException(obisCode.toString());
    }

    private StoredValues getStoredValuesImpl(ObisCode obisCode) throws NoSuchRegisterException {
        if (isDaily(obisCode)) {
            return dailyStoredValues;
        } else if (isMonthly(obisCode)) {
            return monthlyStoredValues;
        } else {
            throw new NoSuchRegisterException("Unsupported F-field in obis code " + obisCode);
        }
    }

    private boolean isDaily(ObisCode obisCode) {
        return obisCode.getF() > 11;
    }

    private boolean isMonthly(ObisCode obisCode) {
        return obisCode.getF() < 12;
    }

    public final List<G3Mapping> getClockMappings() {
        List<G3Mapping> basicChecks = new ArrayList<>();
        basicChecks.add(new ClockMapping(Clock.getDefaultObisCode()));
        return basicChecks;
    }

    protected Collection<? extends G3Mapping> getMemoryManagementMappings() {
        List<G3Mapping> memoryManagement = new ArrayList<G3Mapping>();
        memoryManagement.add(new MemoryManagementMapping(MEMORY_MANAGEMENT_ATTR2));
        memoryManagement.add(new MemoryManagementMapping(MEMORY_MANAGEMENT_ATTR3));
        memoryManagement.add(new MemoryManagementMapping(MEMORY_MANAGEMENT_ATTR2_LEGACY));
        memoryManagement.add(new MemoryManagementMapping(MEMORY_MANAGEMENT_ATTR3_LEGACY));
        return memoryManagement;
    }

    public final List<G3Mapping> getBillingPeriodMappings() {
        List<G3Mapping> billingMappings = new ArrayList<>();
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, ASYNC_EOB_SCHEDULE_ALL));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, ASYNC_EOB_SCHEDULE_TYPE));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, ASYNC_EOB_SCHEDULE_TIME));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, DAILY_EOB_SCHEDULE_ALL));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, DAILY_EOB_SCHEDULE_TYPE));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, DAILY_EOB_SCHEDULE_TIME));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, MONTHLY_EOB_SCHEDULE_ALL));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, MONTHLY_EOB_SCHEDULE_TYPE));
        billingMappings.add(new SingleActionScheduleMapping(deviceTimeZone, MONTHLY_EOB_SCHEDULE_TIME));
        return billingMappings;
    }

    public final List<G3Mapping> getBreakerManagementMappings() {
        final List<G3Mapping> breakerMappings = new ArrayList<>();
        breakerMappings.add(new DisconnectControlMapper(BREAKER_STATE));
        breakerMappings.add(new DownstreamVoltageMonitoringMapper(DOWNSTREAM_VOLTAGE_MONITORING, Unit.get("V")));
        breakerMappings.add(new OverHeatCurrentLimitMapper(OVERHEAT_CURRENT_LIMIT, Unit.get("A")));
        breakerMappings.add(new DataValueMapping(OVERHEAT_DURATION_LIMIT, Unit.get("s")));
        return breakerMappings;
    }

    public final List<G3Mapping> getCommunicationMappings() {
        final List<G3Mapping> communcationMappings = new ArrayList<>();
        communcationMappings.add(new DataValueMapping(CONSUMER_INTERFACE_SETUP));
        communcationMappings.add(new DataValueMapping(EURIDIS_PROTOCOL_SETUP));
        return communcationMappings;
    }

    public final List<G3Mapping> getDataBaseMappings() {
        final List<G3Mapping> dataBaseMappings = new ArrayList<>();
        dataBaseMappings.add(new FirmwareMapping(FIRMWARE_ENTRIES));
        dataBaseMappings.add(new FirmwareMapping(FIRMWARE_ENTRY_1));
        dataBaseMappings.add(new FirmwareMapping(FIRMWARE_ENTRY_2));
        dataBaseMappings.add(new FirmwareMapping(FIRMWARE_ENTRY_3));
        dataBaseMappings.add(new FirmwareMapping(FIRMWARE_ENTRY_4));
        dataBaseMappings.add(new FirmwareMapping(FIRMWARE_ENTRY_5));
        dataBaseMappings.add(new LogicalDeviceNameMapping(LOGICAL_DEVICE_NAME));
        dataBaseMappings.add(new ProducerConsumerMapping(PRODUCER_CONSUMER_CODE));
        dataBaseMappings.add(new TestModeMapper(TEST_MODE_CODE));
        return dataBaseMappings;
    }

    public final List<G3Mapping> getEventManagerMappings() {
        final List<G3Mapping> eventMappings = new ArrayList<>();
        eventMappings.add(new DataValueMapping(ALARM_FILTER));
        eventMappings.add(new DataValueMapping(ALARM_REGISTER));
        eventMappings.add(new DataValueMapping(BREAKER_EVENT_CODE));
        eventMappings.add(new DataValueMapping(BREAKER_OPENING_COUNTER));
        eventMappings.add(new DataValueMapping(BREAKER_OPENING_COUNTER_OVER_MAX_OPENING_CURRENT));
        eventMappings.add(new DataValueMapping(COMMUNICATION_EVENT_CODE));
        eventMappings.add(new DataValueMapping(COVER_EVENT_CODE));
        eventMappings.add(new DataValueMapping(PEAK_EVENT_CODE));
        eventMappings.add(new DataValueMapping(COVER_OPENING_COUNTER));
        eventMappings.add(new DataValueMapping(ERROR_REGISTER));
        eventMappings.add(new DataValueMapping(IMAGE_TRANSFER_COUNTER));
        eventMappings.add(new DataValueMapping(MAIN_EVENT_CODE));
        eventMappings.add(new DataValueMapping(NUMBER_OF_PROGRAMMING));
        eventMappings.add(new DataValueMapping(STATUS_REGISTER));
        eventMappings.add(new DataValueMapping(VOLTAGE_CUT_EVENT_CODE));
        eventMappings.add(new DataValueMapping(THR_DOWNSTREAM_IMPEDANCE));
        return eventMappings;
    }

    public final List<G3Mapping> getTotalEnergyRegistering() {
        final List<G3Mapping> energyMappings = new ArrayList<>();
        energyMappings.add(new RegisterMapping(TOTAL_EXPORT_ACTIVE_ENERGY));
        energyMappings.add(new RegisterMapping(TOTAL_IMPORT_ACTIVE_ENERGY));
        energyMappings.add(new RegisterMapping(TOTAL_REACTIVE_Q1_ENERGY));
        energyMappings.add(new RegisterMapping(TOTAL_REACTIVE_Q2_ENERGY));
        energyMappings.add(new RegisterMapping(TOTAL_REACTIVE_Q3_ENERGY));
        energyMappings.add(new RegisterMapping(TOTAL_REACTIVE_Q4_ENERGY));
        energyMappings.add(new RegisterMapping(ACTIVE_E_CONSIST_CHK_THR));
        energyMappings.add(new ExtendedRegisterMapping(DAILY_MAX_POWER));
        return energyMappings;
    }

    public final List<G3Mapping> getRatedEnergyRegistering() {
        final List<G3Mapping> ratedEnergyMappings = new ArrayList<>();
        ratedEnergyMappings.add(new RegisterMapping(TARIFF1_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF2_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF3_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF4_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF5_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF6_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF7_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF8_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF9_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF10_IMPORT_ACTIVE_ENERGY_PROVIDER));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF1_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF2_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF3_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK));
        ratedEnergyMappings.add(new RegisterMapping(TARIFF4_IMPORT_ACTIVE_ENERGY_PUBLIC_NETWORK));
        return ratedEnergyMappings;
    }

    public final List<G3Mapping> getVoltageQualityMeasurements() {
        final List<G3Mapping> voltageMappings = new ArrayList<>();
        voltageMappings.add(new RegisterMapping(AVG_ABNORMAL_VOLTAGE_PH1));
        voltageMappings.add(new RegisterMapping(AVG_ABNORMAL_VOLTAGE_PH2));
        voltageMappings.add(new RegisterMapping(AVG_ABNORMAL_VOLTAGE_PH3));
        voltageMappings.add(new DataValueMapping(VOLTAGE_CUT_MINIMUM_DURATION));
        voltageMappings.add(new ExtendedRegisterMapping(PH_WITH_ABNORMAL_VOLTAGE));
        return voltageMappings;
    }

    public final List<G3Mapping> getUsbSetupMappings() {
        final List<G3Mapping> usbSetupMappings = new ArrayList<>();
        usbSetupMappings.add(new USBSetupMapping(USB_SETUP_ATTR1));
        usbSetupMappings.add(new USBSetupMapping(USB_SETUP_ATTR2));
        usbSetupMappings.add(new USBSetupMapping(USB_SETUP_ATTR3));
        usbSetupMappings.add(new USBSetupMapping(USB_SETUP_ATTR4));
        usbSetupMappings.add(new USBSetupMapping(USB_SETUP_ATTR1_LEGACY));
        usbSetupMappings.add(new USBSetupMapping(USB_SETUP_ATTR2_LEGACY));
        usbSetupMappings.add(new USBSetupMapping(USB_SETUP_ATTR3_LEGACY));
        usbSetupMappings.add(new USBSetupMapping(USB_SETUP_ATTR4_LEGACY));
        return usbSetupMappings;
    }

    public final List<G3Mapping> getDisconnectControlRegistering() {
        final List<G3Mapping> disconnectControlMappings = new ArrayList<>();
        disconnectControlMappings.add(new DisconnectControlMapping(DISCONNECT_CONTROL_ATTR1));
        disconnectControlMappings.add(new DisconnectControlMapping(DISCONNECT_CONTROL_ATTR2));
        disconnectControlMappings.add(new DisconnectControlMapping(DISCONNECT_CONTROL_ATTR3));
        disconnectControlMappings.add(new DisconnectControlMapping(DISCONNECT_CONTROL_ATTR4));
        return disconnectControlMappings;
    }

    public final List<G3Mapping> getGprsModemSetupMappings() {
        final List<G3Mapping> gprsModemSetupMappings = new ArrayList<>();
        gprsModemSetupMappings.add(new GprsModemSetupMapping(GPRS_MODEM_SETUP_ATTR1));
        gprsModemSetupMappings.add(new GprsModemSetupMapping(GPRS_MODEM_SETUP_ATTR2));
        gprsModemSetupMappings.add(new GprsModemSetupMapping(GPRS_MODEM_SETUP_ATTR3));
        gprsModemSetupMappings.add(new GprsModemSetupMapping(GPRS_MODEM_SETUP_ATTR4));
        gprsModemSetupMappings.add(new GprsModemSetupMapping(GPRS_MODEM_SETUP_ATTR5));
        gprsModemSetupMappings.add(new GprsModemSetupMapping(GPRS_MODEM_SETUP_ATTR6));
        gprsModemSetupMappings.add(new GprsModemSetupMapping(GPRS_MODEM_SETUP_ATTR7));
        gprsModemSetupMappings.add(new GprsModemSetupMapping(GPRS_MODEM_SETUP_ATTR8));
        gprsModemSetupMappings.add(new GprsModemSetupMapping(GPRS_MODEM_SETUP_ATTR9));
        gprsModemSetupMappings.add(new GprsModemSetupMapping(GPRS_MODEM_SETUP_ATTR10));
        gprsModemSetupMappings.add(new GprsModemSetupMapping(GPRS_MODEM_SETUP_ATTR11));
        return gprsModemSetupMappings;
    }

    public final List<G3Mapping> getPPPSetupRegistering() {
           final List<G3Mapping> pppSetupMappings = new ArrayList<>();
     pppSetupMappings.add(new PPPSetupMapping(PPP_SETUP_ATTR1));
        pppSetupMappings.add(new PPPSetupMapping(PPP_SETUP_ATTR2));
        pppSetupMappings.add(new PPPSetupMapping(PPP_SETUP_ATTR3));
        pppSetupMappings.add(new PPPSetupMapping(PPP_SETUP_ATTR4));
        pppSetupMappings.add(new PPPSetupMapping(PPP_SETUP_ATTR5));
        pppSetupMappings.add(new PPPSetupMapping(PPP_SETUP_ATTR6));
        return pppSetupMappings;
    }

    public final List<G3Mapping> getModemWatchdogMappings() {
        final List<G3Mapping> ModemWatchdogMappings = new ArrayList<>();
        ModemWatchdogMappings.add(new ModemWatchdogMapping(MODEM_WATCHDOG_ATTR1));
        ModemWatchdogMappings.add(new ModemWatchdogMapping(MODEM_WATCHDOG_ATTR2));
        ModemWatchdogMappings.add(new ModemWatchdogMapping(MODEM_WATCHDOG_ATTR3));
        ModemWatchdogMappings.add(new ModemWatchdogMapping(MODEM_WATCHDOG_ATTR1_LEGACY));
        ModemWatchdogMappings.add(new ModemWatchdogMapping(MODEM_WATCHDOG_ATTR2_LEGACY));
        ModemWatchdogMappings.add(new ModemWatchdogMapping(MODEM_WATCHDOG_ATTR3_LEGACY));
        return ModemWatchdogMappings;
    }

    public final List<G3Mapping> getPLCStatisticsMappings() {
        final List<G3Mapping> plcStatistics = new ArrayList<>();
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR1));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR2));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR3));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR4));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR5));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR6));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR7));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR8));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR9));
        plcStatistics.add(new PlcStatisticsMapping(PHYS_MAC_LAYER_COUNTERS_ATTR10));

        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR1));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR2));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR3));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR4));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR5));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR6));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR7));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR8));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR9));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR10));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR11));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR12));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR13));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR14));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR15));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR16));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR17));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR18));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR19));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR20));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR21));
        plcStatistics.add(new PlcStatisticsMapping(MAC_SETUP_ATTR22));

        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR1));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR2));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR3));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR4));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR5));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR6));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR7));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR8));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR9));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR10));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR11));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR12));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR13));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR14));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR15));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR16));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR17));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR18));
        plcStatistics.add(new PlcStatisticsMapping(SIXLOWPAN_SETUP_ATTR19));

        return plcStatistics;
    }

    protected final List<G3Mapping> getBeaconPushEventNotificationAttributesMappings() {
        final List<G3Mapping> pushEventNotificationMappings = new ArrayList<G3Mapping>();
        pushEventNotificationMappings.add(new BeaconEventNotificationMapping(EVENT_NOTIFICATION_ATTR1_LEGACY));
        pushEventNotificationMappings.add(new BeaconEventNotificationMapping(EVENT_NOTIFICATION_ATTR2_LEGACY));
        pushEventNotificationMappings.add(new BeaconEventNotificationMapping(EVENT_NOTIFICATION_ATTR4_LEGACY));

        return pushEventNotificationMappings;
    }

    protected final List<G3Mapping> getPushSetupMappings() {
        final List<G3Mapping> pushEventNotificationMappings = new ArrayList<G3Mapping>();

        pushEventNotificationMappings.add(new PushSetupMapping(PUSH_SETUP_ATTR1));
        pushEventNotificationMappings.add(new PushSetupMapping(PUSH_SETUP_ATTR3));
        pushEventNotificationMappings.add(new PushSetupMapping(PUSH_SETUP_ATTR6));
        pushEventNotificationMappings.add(new PushSetupMapping(PUSH_SETUP_ATTR7));
        pushEventNotificationMappings.add(new PushSetupMapping(PUSH_SETUP_ATTRc2));
        pushEventNotificationMappings.add(new PushSetupMapping(PUSH_SETUP_ATTRc3));
        pushEventNotificationMappings.add(new PushSetupMapping(PUSH_SETUP_ATTRc4));
        pushEventNotificationMappings.add(new PushSetupMapping(PUSH_SETUP_ATTRc5));

        return pushEventNotificationMappings;
    }

    public final List<G3Mapping> getIPv4SetupMappings() {
        final List<G3Mapping> ipv4SetupMappings = new ArrayList<>();

        ipv4SetupMappings.add(new IPv4SetupMapping(WAN_DL_REFERENCE));
        ipv4SetupMappings.add(new IPv4SetupMapping(WAN_IP_ADDRESS));
        ipv4SetupMappings.add(new IPv4SetupMapping(WAN_MULTICAST_IP_ADDRESS));
        ipv4SetupMappings.add(new IPv4SetupMapping(WAN_IP_OPTIONS));
        ipv4SetupMappings.add(new IPv4SetupMapping(WAN_SUBNET_MASK));
        ipv4SetupMappings.add(new IPv4SetupMapping(WAN_GATEWAY_IP_ADDRESS));
        ipv4SetupMappings.add(new IPv4SetupMapping(WAN_USE_DHCP_FLAG));
        ipv4SetupMappings.add(new IPv4SetupMapping(WAN_PRIMARY_DNS_ADDRESS));
        ipv4SetupMappings.add(new IPv4SetupMapping(WAN_SECONDARY_DNS_ADDRESS));

        ipv4SetupMappings.add(new IPv4SetupMapping(LAN_DL_REFERENCE));
        ipv4SetupMappings.add(new IPv4SetupMapping(LAN_IP_ADDRESS));
        ipv4SetupMappings.add(new IPv4SetupMapping(LAN_MULTICAST_IP_ADDRESS));
        ipv4SetupMappings.add(new IPv4SetupMapping(LAN_IP_OPTIONS));
        ipv4SetupMappings.add(new IPv4SetupMapping(LAN_SUBNET_MASK));
        ipv4SetupMappings.add(new IPv4SetupMapping(LAN_GATEWAY_IP_ADDRESS));
        ipv4SetupMappings.add(new IPv4SetupMapping(LAN_USE_DHCP_FLAG));
        ipv4SetupMappings.add(new IPv4SetupMapping(LAN_PRIMARY_DNS_ADDRESS));
        ipv4SetupMappings.add(new IPv4SetupMapping(LAN_SECONDARY_DNS_ADDRESS));

        ipv4SetupMappings.add(new IPv4SetupMapping(WWAN_DL_REFERENCE));
        ipv4SetupMappings.add(new IPv4SetupMapping(WWAN_IP_ADDRESS));
        ipv4SetupMappings.add(new IPv4SetupMapping(WWAN_MULTICAST_IP_ADDRESS));
        ipv4SetupMappings.add(new IPv4SetupMapping(WWAN_IP_OPTIONS));
        ipv4SetupMappings.add(new IPv4SetupMapping(WWAN_SUBNET_MASK));
        ipv4SetupMappings.add(new IPv4SetupMapping(WWAN_GATEWAY_IP_ADDRESS));
        ipv4SetupMappings.add(new IPv4SetupMapping(WWAN_USE_DHCP_FLAG));
        ipv4SetupMappings.add(new IPv4SetupMapping(WWAN_PRIMARY_DNS_ADDRESS));
        ipv4SetupMappings.add(new IPv4SetupMapping(WWAN_SECONDARY_DNS_ADDRESS));

        return ipv4SetupMappings;
    }


    protected List<G3Mapping> getConcentratorSetupMappings() {
        final List<G3Mapping> concentratorSetupMapping = new ArrayList<G3Mapping>();

        concentratorSetupMapping.add(new ConcentratorSetupMapping(CONCENTRATOR_SETUP_ACTIVE));
        concentratorSetupMapping.add(new ConcentratorSetupMapping(CONCENTRATOR_SETUP_MAX_CONCURENT_SESSIONS));
        concentratorSetupMapping.add(new ConcentratorSetupMapping(CONCENTRATOR_SETUP_METER_INFO_JSON));
        concentratorSetupMapping.add(new ConcentratorSetupMapping(CONCENTRATOR_SETUP_METER_INFO_SERIAL));
        concentratorSetupMapping.add(new ConcentratorSetupMapping(CONCENTRATOR_SETUP_METER_INFO_MAC));
        concentratorSetupMapping.add(new ConcentratorSetupMapping(CONCENTRATOR_SETUP_PROTOCOL_EVENT_LOG_LEVEL));

        return concentratorSetupMapping;
    }


    protected final List<G3Mapping> getMultiAPNConfigurationMappings() {
        final List<G3Mapping> apnConfigsMappings = new ArrayList<G3Mapping>();
        apnConfigsMappings.add(new MultiAPNConfigMapping(MULTI_APN_CONFIG));
        apnConfigsMappings.add(new MultiAPNConfigMapping(MULTI_APN_CONFIG_LEGACY));
        return apnConfigsMappings;
    }

    protected final List<G3Mapping> getImageTransferMappings() {
        final List<G3Mapping> imageTransferMappings = new ArrayList<G3Mapping>();
        imageTransferMappings.add(new ImageTransferMapping(IMAGE_TRANSFER_ATTR2));
        imageTransferMappings.add(new ImageTransferMapping(IMAGE_TRANSFER_ATTR3));
        imageTransferMappings.add(new ImageTransferMapping(IMAGE_TRANSFER_ATTR4));
        imageTransferMappings.add(new ImageTransferMapping(IMAGE_TRANSFER_ATTR5));
        imageTransferMappings.add(new ImageTransferMapping(IMAGE_TRANSFER_ATTR6));
        return imageTransferMappings;
    }

    protected final List<G3Mapping> getLimiterMappings() {
        final List<G3Mapping> limiterMappings = new ArrayList<G3Mapping>();
        limiterMappings.add(new LimiterMapping(LIMITER_ATTR4));
        limiterMappings.add(new LimiterMapping(LIMITER_ATTR6));
        limiterMappings.add(new LimiterMapping(LIMITER_ATTR7));
        return limiterMappings;
    }

    protected final List<G3Mapping> getNTPSetupMappings() {
        final List<G3Mapping> ntpSetupMappings = new ArrayList<>();
        ntpSetupMappings.add(new NTPSetupMapping(NTP_SETUP_ATTR1));
        ntpSetupMappings.add(new NTPSetupMapping(NTP_SETUP_ATTR2));
        ntpSetupMappings.add(new NTPSetupMapping(NTP_SETUP_ATTR3));
        ntpSetupMappings.add(new NTPSetupMapping(NTP_SETUP_ATTR4));
        return ntpSetupMappings;
    }

    protected final List<G3Mapping> getBillingSchedulerMappings() {
        final List<G3Mapping> schedulerMappings = new ArrayList<G3Mapping>();
        schedulerMappings.add(new SchedulerMapping(deviceTimeZone, BILLING_SCHEDULER_ATTR2));
        schedulerMappings.add(new SchedulerMapping(deviceTimeZone, BILLING_SCHEDULER_ATTR3));
        schedulerMappings.add(new SchedulerMapping(deviceTimeZone, BILLING_SCHEDULER_ATTR4));
        return schedulerMappings;
    }

    protected final List<G3Mapping> getDisconnectControlSchedulerMappings() {
        final List<G3Mapping> schedulerMappings = new ArrayList<G3Mapping>();
        schedulerMappings.add(new SchedulerMapping(deviceTimeZone, DISCONNECT_CONTROL_SCHEDULER_ATTR2));
        schedulerMappings.add(new SchedulerMapping(deviceTimeZone, DISCONNECT_CONTROL_SCHEDULER_ATTR3));
        schedulerMappings.add(new SchedulerMapping(deviceTimeZone, DISCONNECT_CONTROL_SCHEDULER_ATTR4));
        return schedulerMappings;
    }

    protected final List<G3Mapping> getImageTransferActivationSchedulerMappings() {
        final List<G3Mapping> schedulerMappings = new ArrayList<G3Mapping>();
        schedulerMappings.add(new SchedulerMapping(deviceTimeZone, IMAGE_TRANSFER_ACTIVATION_SCHEDULER_ATTR2));
        schedulerMappings.add(new SchedulerMapping(deviceTimeZone, IMAGE_TRANSFER_ACTIVATION_SCHEDULER_ATTR3));
        schedulerMappings.add(new SchedulerMapping(deviceTimeZone, IMAGE_TRANSFER_ACTIVATION_SCHEDULER_ATTR4));
        return schedulerMappings;
    }

    protected final List<G3Mapping> getLastFirmwareActivationMappings() {
        final List<G3Mapping> lastFirmwareActivationMappings = new ArrayList<G3Mapping>();
        lastFirmwareActivationMappings.add(new LastFirmwareActivationMapping(LAST_FIRMWARE_ACTIVATION_EXTENDED_ATTR2));
        return lastFirmwareActivationMappings;
    }

    protected final List<G3Mapping> getSNMPSetupMappings() {
        final List<G3Mapping> snmpSetupMappings = new ArrayList<>();
        snmpSetupMappings.add(new SNMPSetupMapping(SNMP_SETUP_ATTR1));
        snmpSetupMappings.add(new SNMPSetupMapping(SNMP_SETUP_ATTR2));
        snmpSetupMappings.add(new SNMPSetupMapping(SNMP_SETUP_ATTR3));
        snmpSetupMappings.add(new SNMPSetupMapping(SNMP_SETUP_ATTR4));
        snmpSetupMappings.add(new SNMPSetupMapping(SNMP_SETUP_ATTR5));
        snmpSetupMappings.add(new SNMPSetupMapping(SNMP_SETUP_ATTR6));
        snmpSetupMappings.add(new SNMPSetupMapping(SNMP_SETUP_ATTR7));
        snmpSetupMappings.add(new SNMPSetupMapping(SNMP_SETUP_ATTR8));
        snmpSetupMappings.add(new SNMPSetupMapping(SNMP_SETUP_ATTR9));
        snmpSetupMappings.add(new SNMPSetupMapping(SNMP_SETUP_ATTR10));
        return snmpSetupMappings;
    }

    protected final List<G3Mapping> getLTEMonitoringMappings() {
        final List<G3Mapping> lteMonitoringMappings = new ArrayList<>();
        lteMonitoringMappings.add(new LTEMonitoringMapping(LTE_MONITORING_ATTR2));
        return lteMonitoringMappings;
    }

    protected final List<G3Mapping> getWWANStateTransitionMappings() {
        final List<G3Mapping> wwanStateTransitionMappings = new ArrayList<>();
        wwanStateTransitionMappings.add(new WWANStateTransitionMapping(WWAN_STATE_TRANSITION_ATTR2));
        return wwanStateTransitionMappings;
    }

    protected final List<G3Mapping> getGSMDiagnosticsMappings() {
        final List<G3Mapping> gsmDiagnosticsMappings = new ArrayList<>();
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR2));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR3));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR4));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR5));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR6));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR7));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR8));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR_MINUS_1));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR_MINUS_2));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR_MINUS_3));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR_MINUS_4));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR_MINUS_5));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR_MINUS_6));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR_MINUS_7));
        gsmDiagnosticsMappings.add(new GSMDiagnosticsMapping(GSM_DIAGNOSTICS_ATTR_MINUS_8));
        return gsmDiagnosticsMappings;
    }

    protected final List<G3Mapping> getG3PlcJoinRequestTimestampMapping() {
        final List<G3Mapping> g3Mappings = new ArrayList<>();
        g3Mappings.add(new G3PlcJoinRequestTimestampMapping(G3_PLC_JOIN_REQ_TIMESTAMP));
        g3Mappings.add(new G3PlcJoinRequestTimestampMapping(G3_PLC_JOIN_REQ_TIMESTAMP_ATTR2));
        return g3Mappings;
    }

}