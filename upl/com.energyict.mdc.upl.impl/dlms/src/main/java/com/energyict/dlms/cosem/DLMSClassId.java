package com.energyict.dlms.cosem;

/**
 * This enum contains all the dlms class id's
 *
 * @author jme
 */
public enum DLMSClassId {

    UNKNOWN(-1),
    DATA(1),
    REGISTER(3),
    EXTENDED_REGISTER(4),
    DEMAND_REGISTER(5),
    REGISTER_ACTIVATION(6),
    PROFILE_GENERIC(7),
    CLOCK(8),
    SCRIPT_TABLE(9),
    SCHEDULE(10),
    SPECIAL_DAYS_TABLE(11),
    ASSOCIATION_SN(12),
    ASSOCIATION_LN(15),
    SAP_ASSIGNMENT(17),
    IMAGE_TRANSFER(18),
    IEC_LOCAL_PORT_SETUP(19),
    ACTIVITY_CALENDAR(20),
    REGISTER_MONITOR(21),
    SINGLE_ACTION_SCHEDULE(22),
    IEC_HDLC_SETUP(23),
    IEC_TWISTED_PAIR_SETUP(24),
    MBUS_SLAVE_PORT_SETUP(25),
    UTILITY_TABLES(26),
    MODEM_CONFIGURATION(27),
    AUTO_ANSWER(28),
    AUTO_CONNECT(29),
    PUSH_EVENT_NOTIFICATION_SETUP(40),
    TCP_UDP_SETUP(41),
    IPV4_SETUP(42),
    MAC_ADDRESS_SETUP(43),
    PPP_SETUP(44),
    GPRS_SETUP(45),
    SMTP_SETUP(46),
    S_FSK_PHY_MAC_SETUP(50),
    S_FSK_ACTIVE_INITIATOR(51),
    S_FSK_MAC_SYNC_TIMEOUTS(52),
    S_FSK_MAC_COUNTERS(53),
    S_FSK_IEC_61334_4_32_LLC_SETUP(55),
    S_FSK_REPORTING_SYSTEM_LIST(56),
    IEC_8802_2_LLC_TYPE1_SETUP(57),
    IEC_8802_2_LLC_TYPE2_SETUP(58),
    IEC_8802_2_LLC_TYPE3_SETUP(59),
    SMS_WAKEUP_CONFIGURATION(60),
    REGISTER_TABLE(61),
    STATUS_MAPPING(63),
    SECURITY_SETUP(64),
    DISCONNECT_CONTROL(70),
    LIMITER(71),
    MBUS_CLIENT(72),
    WIRELESS_MODE_Q_CHANNEL(73),
    CL_432_SETUP(80),
    PRIME_PLC_PHY_LAYER_COUNTERS(81),
    PRIME_PLC_MAC_SETUP(82),
    PRIME_PLC_MAC_FUNCTIONAL_PARAMETERS(83),
    PRIME_PLC_MAC_COUNTERS(84),
    PRIME_PLC_MAC_NETWORK_ADMINISTRATION_DATA(85),
    PRIME_PLC_MAC_APPLICATION_IDENTIFICATION(86),
    PLC_OFDM_TYPE2_PHY_AND_MAC_COUNTERS(90),
    PLC_OFDM_TYPE2_MAC_SETUP(91),
    SIX_LOW_PAN_ADAPTATION_LAYER_SETUP(92),
    MANUFACTURER_SPECIFIC_8193(8193),
    MANUFACTURER_SPECIFIC_8194(8194),
    ACTIVE_PASSIVE(9000),
    INSTRUMENTATION_PROFILE_ENTRY(9099),
    CHANGE_OF_TENANT_SUPPLIER_MANAGEMENT(9100),
    ZIGBEE_HAN_MANAGEMENT(9900),
    ZIGBEE_SAS_STARTUP(9901),
    ZIGBEE_SAS_JOIN(9902),
    ZIGBEE_SAS_APS_FRAGMENTATION(9903),
    ZIGBEE_SETC_CONTROL(9904),
    ZIGBEE_WAN_MANAGEMENT(9905),
    ZIGBEE_SE_DEMAND_RESPONSE_LOAD_CONTROL(9906),
    GPRS_MANUAL_ROAMING_SETUP(10000),
    DEDICATED_EVENT_LOG_SIMPLE(10920),
    LIFE_CYCLE_MANAGEMENT(20000),
    FIREWALL_SETUP(20001),
    G3_NETWORK_MANAGEMENT(20002),
    GENERIC_PLC_IB_SETUP(20003),
    LOGGER_SETTINGS(20004),
    NETWORK_MANAGEMENT(20005),
    MASTERBOARD_SETUP(20006),
    UPLINK_PING_SETUP(20007),
    GATEWAY_SETUP(20008),
    NTP_SERVER_ADDRESS(20010),
    MODEM_WATCHDOG_SETUP(20011),
    EVENT_NOTIFICATION(20012),
    WEB_PORTAL_PASSWORDS(20013),
    SCHEDULE_MANAGER(20014),
    DEVICE_TYPE_MANAGER(20015),
    CLIENT_TYPE_MANAGER(20016);

    /**
     * The dlms class identifier
     */
    private int classId;

    /**
     * Provate constructor to create
     *
     * @param classId
     */
    private DLMSClassId(int classId) {
        this.classId = classId;
    }

    /**
     * Get a DLMSClassId by a given classId. If the classId doesn't exist, return the UNKNOWN DLMSClassId
     *
     * @param id
     * @return
     */
    public static DLMSClassId findById(int id) {
        for (DLMSClassId classId : DLMSClassId.values()) {
            if (classId.getClassId() == id) {
                return classId;
            }
        }
        return UNKNOWN;
    }

    /**
     * Get a description based on the classId as int
     *
     * @param dlmsClassId
     * @return
     */
    public static String getDescription(int dlmsClassId) {
        return findById(dlmsClassId).toString();
    }

    /**
     * Getter for the class number (classId)
     *
     * @return the class id as int
     */
    public int getClassId() {
        return classId;
    }

}
