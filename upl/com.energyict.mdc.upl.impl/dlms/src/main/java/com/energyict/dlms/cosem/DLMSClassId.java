package com.energyict.dlms.cosem;

import java.io.IOException;

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
	TCP_UDP_SETUP(41),
	IPV4_SETUP(42),
	ETHERNET_SETUP(43),
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
	REGISTER_TABLE(61),
	STATUS_MAPPING(63),
	SECURITY_SETUP(64),
	DISCONNECT_CONTROL(70),
	LIMITER(71),
	MBUS_CLIENT(72),
	WIRELESS_MODE_Q_CHANNEL(73);

	private int classId;

    /**
     * Provate constructor to create
     * @param classId
     */
    private DLMSClassId(int classId) {
		this.classId = classId;
	}

    /**
     * Getter for the class number (classId)
     *
     * @return the class id as int
     */
    public int getClassId() {
        return classId;
    }

    private void setClassId(int classId) {
        this.classId = classId;
    }

    /**
     * Get a DLMSClassId by a given classId. If the classId doesn't exist, return the UNKNOWN DLMSClassId
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
     * Get a desctiption based on the classId as int
     * @param dlmsClassId
     * @return
     */
    public static String getDescription(int dlmsClassId) {
        return findById(dlmsClassId).toString();
	}

    public boolean isRegister() {
        return getClassId() == REGISTER.getClassId();
    }

    public boolean isExtendedRegister() {
        return getClassId() == EXTENDED_REGISTER.getClassId();
    }

    public boolean isDemandRegister() {
        return getClassId() == DEMAND_REGISTER.getClassId();
    }

    public boolean isProfileGeneric() {
        return getClassId() == PROFILE_GENERIC.getClassId();
    }

    public boolean isData() {
        return getClassId() == DATA.getClassId();
    }

    public boolean isClock() {
        return getClassId() == CLOCK.getClassId();
    }

}
