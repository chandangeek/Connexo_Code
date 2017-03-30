/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.messaging;

import aQute.bnd.annotation.ProviderType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Models the unique identifier of a {@link MessageSpec}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-15 (09:22)
 */
@ProviderType
public enum DeviceMessageId {

    ACTIVITY_CALENDAR_READ(1),
    ACTIVITY_CALENDAR_WRITE_CONTRACTS_FROM_XML_USERFILE(2),
    ACTIVITY_CALENDER_SEND(3),
    ACTIVITY_CALENDER_SEND_WITH_DATETIME(4),
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE(5),
    ACTIVITY_CALENDER_SEND_WITH_DATE(6),
    ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND(7),
    ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE(8),
    ACTIVITY_CALENDAR_CLEAR_AND_DISABLE_PASSIVE_TARIFF(9),
    ACTIVATE_CALENDAR_PASSIVE(10),
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT(11),
    ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME(12),

    CONTACTOR_OPEN(1001),
    CONTACTOR_OPEN_WITH_OUTPUT(1002),
    CONTACTOR_OPEN_WITH_ACTIVATION_DATE(1003),
    CONTACTOR_ARM(1004),
    CONTACTOR_ARM_WITH_ACTIVATION_DATE(1005),
    CONTACTOR_CLOSE(1006),
    CONTACTOR_CLOSE_WITH_OUTPUT(1007),
    CONTACTOR_CLOSE_WITH_ACTIVATION_DATE(1008),
    CONTACTOR_CHANGE_CONNECT_CONTROL_MODE(1009),
    CONTACTOR_CLOSE_RELAY(1010),
    CONTACTOR_OPEN_RELAY(1011),
    CONTACTOR_SET_RELAY_CONTROL_MODE(1012),

    ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS(2001),
    ALARM_CONFIGURATION_WRITE_ALARM_FILTER(2002),
    ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS(2003),
    ALARM_CONFIGURATION_CONFIGURE_PUSH_EVENT_NOTIFICATION(2004),

    PLC_CONFIGURATION_FORCE_MANUAL_RESCAN_PLC_BUS(3001),
    PLC_CONFIGURATION_SET_MULTICAST_ADDRESSES(3002),
    PLC_CONFIGURATION_SET_ACTIVE_CHANNEL(3003),
    PLC_CONFIGURATION_SET_CHANNEL_FREQUENCIES(3004),
    PLC_CONFIGURATION_SET_SFSK_INITIATOR_PHASE(3005),
    PLC_CONFIGURATION_SET_SFSK_MAX_FRAME_LENGTH(3006),
    PLC_CONFIGURATION_SET_ACTIVE_SCAN_DURATION(3007),
    PLC_CONFIGURATION_SET_BROAD_CAST_LOG_TABLE_ENTRY_TTL(3008),
    PLC_CONFIGURATION_SET_DISCOVERY_ATTEMPTS_SPEED(3009),
    PLC_CONFIGURATION_SET_MAX_AGE_TIME(3010),
    PLC_CONFIGURATION_SET_MAX_NUMBER_OF_HOPS(3011),
    PLC_CONFIGURATION_SET_MAX_PAN_CONFLICTS_COUNT(3012),
    PLC_CONFIGURATION_SET_PAN_CONFLICT_WAIT_TIME(3013),
    PLC_CONFIGURATION_SET_TONE_MASK(3014),
    PLC_CONFIGURATION_SET_WEAK_LQI_VALUE(3015),
    PLC_CONFIGURATION_WRITE_PLC_G3_TIMEOUT(3017),
    PLC_CONFIGURATION_RESET_PLC_OFDM_MAC_COUNTERS(3018),
    PLC_CONFIGURATION_SET_PAN_ID(3019),
    PLC_CONFIGURATION_SET_MAX_ORPHAN_TIMER(3020),
    PLC_CONFIGURATION_SET_SFSK_REPEATER(3021),
    PLC_CONFIGURATION_SET_SFSK_GAIN(3022),
    PLC_CONFIGURATION_SET_TIMEOUT_NOT_ADDRESSED(3023),
    PLC_CONFIGURATION_SET_SFSK_MAC_TIMEOUTS(3024),
    PLC_CONFIGURATION_SET_PLC_CHANNEL_FREQ_SNR_CREDITS(3025),
    PLC_CONFIGURATION_SET_MAX_NUMBER_OF_HOPS_ATTRIBUTENAME(3026),
    PLC_CONFIGURATION_SET_WEAK_LQI_VALUE_ATTRIBUTENAME(3027),
    PLC_CONFIGURATION_SET_SECURITY_LEVEL(3028),
    PLC_CONFIGURATION_SET_ROUTING_CONFIGURATION(3029),
    PLC_CONFIGURATION_SET_BROADCAST_LOG_TABLE_ENTRY_TTL_ATTRIBUTENAME(3030),
    PLC_CONFIGURATION_SET_MAX_JOIN_WAIT_TIME(3031),
    PLC_CONFIGURATION_SET_PATH_DISCOVERY_TIME(3032),
    PLC_CONFIGURATION_SET_METRIC_TYPE(3033),
    PLC_CONFIGURATION_SET_TMR_TTL(3034),
    PLC_CONFIGURATION_SET_MAX_FRAME_RETRIES(3035),
    PLC_CONFIGURATION_SET_NEIGHBOUR_TABLE_ENTRY_TTL(3036),
    PLC_CONFIGURATION_SET_HIGH_PRIORITY_WINDOW_SIZE(3037),
    PLC_CONFIGURATION_SET_CSMA_FAIRNESS_LIMIT(3038),
    PLC_CONFIGURATION_SET_BEACON_RANDOMIZATION_WINDOW_LENGTH(3039),
    PLC_CONFIGURATION_SET_MAC_A(3040),
    PLC_CONFIGURATION_SET_MAC_K(3041),
    PLC_CONFIGURATION_SET_MINIMUM_CW_ATTEMPTS(3042),
    PLC_CONFIGURATION_SET_MAX_BE(3043),
    PLC_CONFIGURATION_SET_MAX_CSMA_BACK_OFF(3044),
    PLC_CONFIGURATION_SET_MIN_BE(3045),
    PLC_CONFIGURATION_PATH_REQUEST(3046),
    PLC_CONFIGURATION_SET_AUTOMATIC_ROUTE_MANAGEMENT(3047),
    PLC_CONFIGURATION_ENABLE_SNR(3048),
    PLC_CONFIGURATION_SET_SNR_PACKET_INTERVAL(3049),
    PLC_CONFIGURATION_SET_SNR_QUIET_TIME(3050),
    PLC_CONFIGURATION_SET_SNR_PAYLOAD(3051),
    PLC_CONFIGURATION_ENABLE_KEEP_ALIVE(3052),
    PLC_CONFIGURATION_SET_KEEP_ALIVE_SCHEDULE_INTERVAL(3053),
    PLC_CONFIGURATION_SET_KEEP_ALIVE_BUCKET_SIZE(3054),
    PLC_CONFIGURATION_SET_MIN_INACTIVE_METER_TIME(3055),
    PLC_CONFIGURATION_SET_MAX_INACTIVE_METER_TIME(3056),
    PLC_CONFIGURATION_SET_KEEP_ALIVE_RETRIES(3057),
    PLC_CONFIGURATION_SET_KEEP_ALIVE_TIMEOUT(3058),
    PLC_CONFIGURATION_SET_COORD_SHORT_ADDRESS(3059),
    PLC_CONFIGURATION_SET_DISABLE_DEFAULT_ROUTING(3060),
    PLC_CONFIGURATION_SET_DEVICE_TYPE(3061),
    PLC_CONFIGURATION_SET_TONE_MASK_ATTRIBUTE_NAME(3062),
    PLC_CONFIGURATION_ENABLE_G3_INTERFACE(3063),
    PLC_CONFIGURATION_WRITE_G3_KEEP_ALIVE(3064),

    NETWORK_CONNECTIVITY_ACTIVATE_WAKEUP_MECHANISM(4001),
    NETWORK_CONNECTIVITY_DEACTIVATE_SMS_WAKEUP(4002),
    NETWORK_CONNECTIVITY_CHANGE_GPRS_USER_CREDENTIALS(4003),
    NETWORK_CONNECTIVITY_CHANGE_GPRS_APN_CREDENTIALS(4004),
    NETWORK_CONNECTIVITY_ADD_PHONENUMBERS_TO_WHITE_LIST(4005),
    NETWORK_CONNECTIVITY_ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST(4006),
    NETWORK_CONNECTIVITY_CHANGE_SMS_CENTER_NUMBER(4007),
    NETWORK_CONNECTIVITY_CHANGE_DEVICE_PHONENUMBER(4008),
    NETWORK_CONNECTIVITY_CHANGE_GPRS_IP_ADDRESS_AND_PORT(4009),
    NETWORK_CONNECTIVITY_CHANGE_WAKEUP_FREQUENCY(4010),
    NETWORK_CONNECTIVITY_CHANGE_INACTIVITY_TIMEOUT(4011),
    NETWORK_CONNECTIVITY_SET_PROXY_SERVER(4012),
    NETWORK_CONNECTIVITY_SET_PROXY_USERNAME(4013),
    NETWORK_CONNECTIVITY_SET_PROXY_PASSWORD(4014),
    NETWORK_CONNECTIVITY_SET_DHCP(4015),
    NETWORK_CONNECTIVITY_SET_DHCP_TIMEOUT(4016),
    NETWORK_CONNECTIVITY_SET_IP_ADDRESS(4017),
    NETWORK_CONNECTIVITY_SET_SUBNET_MASK(4018),
    NETWORK_CONNECTIVITY_SET_GATEWAY(4019),
    NETWORK_CONNECTIVITY_SET_NAME_SERVER(4020),
    NETWORK_CONNECTIVITY_SET_HTTP_PORT(4021),
    NETWORK_CONNECTIVITY_CONFIGURE_KEEP_ALIVE_SETTINGS(4022),
    NETWORK_CONNECTIVITY_PREFER_GPRS_UPSTREAM_COMMUNICATION(4023),
    NETWORK_CONNECTIVITY_ENABLE_MODEM_WATCHDOG(4024),
    NETWORK_CONNECTIVITY_SET_MODEM_WATCHDOG_PARAMETERS(4025),
    NETWORK_CONNECTIVITY_CLEAR_WHITE_LIST(4026),
    NETWORK_CONNECTIVITY_ENABLE_WHITE_LIST(4027),
    NETWORK_CONNECTIVITY_DISABLE_WHITE_LIST(4028),
    NETWORK_CONNECTIVITY_ENABLE_OPERATING_WINDOW(4029),
    NETWORK_CONNECTIVITY_DISABLE_OPERATING_WINDOW(4030),
    NETWORK_CONNECTIVITY_SET_OPERATING_WINDOW_START_TIME(4031),
    NETWORK_CONNECTIVITY_SET_OPERATING_WINDOW_END_TIME(4032),
    NETWORK_CONNECTIVITY_RUN_METER_DISCOVERY(4033),
    NETWORK_CONNECTIVITY_RUN_ALARM_METER_DISCOVERY(4034),
    NETWORK_CONNECTIVITY_RUN_REPEATER_CALL(4035),
    NETWORK_CONNECTIVITY_SET_NETWORK_MANAGEMENT_PARAMETERS(4036),

    FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER(5001),
    FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE(5002),
    FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE_ACTIVATE_IMMEDIATE(5003),
    FIRMWARE_UPGRADE_ACTIVATE(5004),
    FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE(5005),
    FIRMWARE_UPGRADE_WITH_USER_FILE_VERSION_AND_ACTIVATE_DATE(5006),
    FIRMWARE_UPGRADE_URL_ACTIVATE_IMMEDIATE(5007),
    FIRMWARE_UPGRADE_URL_AND_ACTIVATE_DATE(5008),
    FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE(5009),

    ZIGBEE_CONFIGURATION_CREATE_HAN_NETWORK(6001),
    ZIGBEE_CONFIGURATION_REMOVE_HAN_NETWORK(6002),
    ZIGBEE_CONFIGURATION_JOIN_SLAVE_DEVICE(6003),
    ZIGBEE_CONFIGURATION_REMOVE_MIRROR(6004),
    ZIGBEE_CONFIGURATION_REMOVE_SLAVE_DEVICE(6005),
    ZIGBEE_CONFIGURATION_REMOVE_ALL_SLAVE_DEVICES(6006),
    ZIGBEE_CONFIGURATION_BACK_UP_HAN_PARAMETERS(6007),
    ZIGBEE_CONFIGURATION_RESTORE_HAN_PARAMETERS(6008),
    ZIGBEE_CONFIGURATION_READ_STATUS(6009),
    ZIGBEE_CONFIGURATION_CHANGE_HAN_STARTUP_ATTRIBUTE_SETUP(6010),
    ZIGBEE_CONFIGURATION_NCP_FIRMWARE_UPDATE_WITH_USER_FILE(6011),
    ZIGBEE_CONFIGURATION_NCP_FIRMWARE_UPDATE_WITH_USER_FILE_AND_ACTIVATE(6012),

    SECURITY_ACTIVATE_DLMS_ENCRYPTION(7001),
    SECURITY_CHANGE_DLMS_AUTHENTICATION_LEVEL(7002),
    SECURITY_CHANGE_ENCRYPTION_KEY(7003),
    SECURITY_CHANGE_CLIENT_PASSWORDS(7004),
    SECURITY_WRITE_PSK(7005),
    SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY(7006),
    SECURITY_CHANGE_AUTHENTICATION_KEY(7007),
    SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY(7008),
    SECURITY_CHANGE_PASSWORD(7009),
    SECURITY_CHANGE_PASSWORD_WITH_NEW_PASSWORD(7010),
    SECURITY_CHANGE_LLS_SECRET(7011),
    SECURITY_CHANGE_LLS_SECRET_HEX(7012),
    SECURITY_CHANGE_HLS_SECRET(7013),
    SECURITY_CHANGE_HLS_SECRET_HEX(7014),
    SECURITY_ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY(7015),
    SECURITY_CHANGE_EXECUTION_KEY(7016),
    SECURITY_CHANGE_TEMPORARY_KEY(7017),
    SECURITY_BREAK_OR_RESTORE_SEALS(70118),
    SECURITY_TEMPORARY_BREAK_SEALS(7019),
    SECURITY_GENERATE_NEW_PUBLIC_KEY(7020),
    SECURITY_GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM(7021),
    SECURITY_SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP(7022),
    SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P0(7023),
    SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P1(7024),
    SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P0(7025),
    SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P1(7026),
    SECURITY_CHANGE_HLS_SECRET_USING_SERVICE_KEY(7027),
    SECURITY_CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY(7028),
    SECURITY_CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY(7029),
    SECURITY_CHANGE_HLS_SECRET_WITH_PASSWORD(7030),
    SECURITY_CHANGE_WEBPORTAL_PASSWORD(7031),
    SECURITY_CHANGE_WEBPORTAL_PASSWORD2(7032),
    SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS(7033),
    SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS(7034),
    SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P3(7035),
    SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P3(7036),
    SECURITY_KEY_RENEWAL(7037),

    DEVICE_ACTIONS_BILLING_RESET(8001),
    DEVICE_ACTIONS_GLOBAL_METER_RESET(8002),
    DEVICE_ACTIONS_DEMAND_RESET(8003),
    DEVICE_ACTIONS_POWER_OUTAGE_RESET(8004),
    DEVICE_ACTIONS_POWER_QUALITY_RESET(8005),
    DEVICE_ACTIONS_ERROR_STATUS_RESET(8006),
    DEVICE_ACTIONS_REGISTERS_RESET(8007),
    DEVICE_ACTIONS_LOAD_LOG_RESET(8008),
    DEVICE_ACTIONS_EVENT_LOG_RESET(8009),
    DEVICE_ACTIONS_ALARM_REGISTER_RESET(8010),
    DEVICE_ACTIONS_ERROR_REGISTER_RESET(8011),
    DEVICE_ACTIONS_REBOOT_DEVICE(8012),
    DEVICE_ACTIONS_DISABLE_WEBSERVER(8013),
    DEVICE_ACTIONS_ENABLE_WEBSERVER(8014),
    DEVICE_ACTIONS_RESTORE_FACTORY_SETTINGS(8015),
    DEVICE_ACTIONS_SET_FTION_REBOOT(8016),
    DEVICE_ACTIONS_SET_FTION_INITIALIZE(8017),
    DEVICE_ACTIONS_SET_FTION_MAIL_LOG(8018),
    DEVICE_ACTIONS_SET_FTION_SAVE_CONFIG(8019),
    DEVICE_ACTIONS_SET_FTION_UPGRADE(8020),
    DEVICE_ACTIONS_SET_FTION_CLEAR_MEM(8021),
    DEVICE_ACTIONS_SET_FTION_MAIL_CONFIG(8022),
    DEVICE_ACTIONS_SET_FTION_MODEM_RESET(8023),
    DEVICE_ACTIONS_CHANGE_ADMIN_PASSWORD(8024),
    DEVICE_ACTIONS_SET_OUTPUT_ON(8025),
    DEVICE_ACTIONS_SET_OUTPUT_OFF(8026),
    DEVICE_ACTIONS_SET_OUTPUT_TOGGLE(8027),
    DEVICE_ACTIONS_SET_OUTPUT_PULSE(8028),
    DEVICE_ACTIONS_SET_ANALOG_OUT(8029),
    DEVICE_ACTIONS_BILLING_RESET_CONTRACT_1(8030),
    DEVICE_ACTIONS_BILLING_RESET_CONTRACT_2(8031),
    DEVICE_ACTIONS_SET_PASSIVE_EOB_DATETIME(8032),
    DEVICE_ACTIONS_REBOOT_APPLICATION(8033),
    DEVICE_ACTIONS_DEMAND_RESET_WITH_FORCE_CLOCK(8034),
    DEVICE_ACTIONS_HARD_RESET_DEVICE(8035),
    DEVICE_ACTIONS_FTION_UPGRADE(8036),
    DEVICE_ACTIONS_RTU_PLUS_SERVER_ENTER_MAINTENANCE_MODE(8037),
    DEVICE_ACTIONS_RTU_PLUS_SERVER_EXIT_MAINTENANCE_MODE(8038),
    DEVICE_ACTIONS_FORCE_MESSAGE_TO_FAILED(8039),
    DEVICE_ACTIONS_FTION_UPGRADE_AND_INIT(8040),
    DEVICE_ACTIONS_FTION_UPGRADE_AND_INIT_WITH_NEW_EISERVER_URL(8041),
    DEVICE_ACTIONS_FTION_UPGRADE_WITH_NEW_EISERVER_URL(8042),
    DEVICE_ACTIONS_FTION_INIT_DATABASE_KEEP_CONFIG(8043),
    DEVICE_ACTIONS_FTION_REBOOT(8044),
    DEVICE_ACTIONS_FTION_RESTART(8045),
    DEVICE_ACTIONS_FTION_SCAN_BUS(8046),
    DEVICE_ACTIONS_SYNC_MASTERDATA(8047),
    DEVICE_ACTIONS_SYNC_DEVICE_DATA(8048),
    DEVICE_ACTIONS_PAUSE_DC_SCHEDULER(8049),
    DEVICE_ACTIONS_RESUME_DC_SCHEDULER(8050),
    DEVICE_ACTIONS_SYNC_ONE_CONFIGURATION_FOR_DC(8051),
    DEVICE_ACTIONS_TRIGGER_PRELIMINARY_PROTOCOL(8052),

    PRICING_GET_INFORMATION(9001),
    PRICING_SET_INFORMATION(9002),
    PRICING_SET_STANDING_CHARGE(9003),
    PRICING_UPDATE_INFORMATION(9004),

    DISPLAY_CONSUMER_MESSAGE_CODE_TO_PORT_P1(10001),
    DISPLAY_CONSUMER_MESSAGE_TEXT_TO_PORT_P1(10002),
    DISPLAY_SET_MESSAGE(10003),
    DISPLAY_SET_MESSAGE_WITH_OPTIONS(10004),
    DISPLAY_SET_MESSAGE_ON_IHD_WITH_OPTIONS(10005),
    DISPLAY_CLEAR_MESSAGE(10006),

    GENERAL_WRITE_RAW_IEC1107_CLASS(11001),
    GENERAL_WRITE_FULL_CONFIGURATION(11002),

    LOAD_BALANCING_WRITE_CONTROL_THRESHOLDS(12001),
    LOAD_BALANCING_SET_DEMAND_CLOSE_TO_CONTRACT_POWER_THRESHOLD(12002),
    LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS(12003),
    LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3(12004),
    LOAD_BALANCING_CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS(12005),
    LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP(12006),
    LOAD_BALANCING_SET_EMERGENCY_PROFILE_GROUP_IDS(12007),
    LOAD_BALANCING_CLEAR_LOAD_LIMIT_CONFIGURATION(12008),
    LOAD_BALANCING_CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP(12009),
    LOAD_BALANCING_ENABLE_LOAD_LIMITING(12010),
    LOAD_BALANCING_ENABLE_LOAD_LIMITING_FOR_GROUP(12011),
    LOAD_BALANCING_DISABLE_LOAD_LIMITING(12012),
    LOAD_BALANCING_CONFIGURE_SUPERVISION_MONITOR(12013),
    LOAD_BALANCING_SET_LOAD_LIMIT_DURATION(12014),
    LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD(12015),
    LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD_WITH_TARIFFS(12016),
    LOAD_BALANCING_SET_LOAD_LIMIT_MEASUREMENT_READING_TYPE(12017),
    LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION(12018),
    LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION_WITH_TARIFFS(12019),

    LOAD_PROFILE_PARTIAL_REQUEST(13001),
    LOAD_PROFILE_RESET_ACTIVE_IMPORT(13002),
    LOAD_PROFILE_RESET_ACTIVE_EXPORT(13003),
    LOAD_PROFILE_RESET_DAILY(13004),
    LOAD_PROFILE_RESET_MONTHLY(13005),
    LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP1(13006),
    LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP2(13007),
    LOAD_PROFILE_WRITE_CONSUMER_PRODUCER_MODE(13008),
    LOAD_PROFILE_REGISTER_REQUEST(13009),

    LOG_BOOK_SET_INPUT_CHANNEL(14001),
    LOG_BOOK_SET_CONDITION(14002),
    LOG_BOOK_SET_CONDITION_VALUE(14003),
    LOG_BOOK_SET_TIME_TRUE(14004),
    LOG_BOOK_SET_TIME_FALSE(14005),
    LOG_BOOK_SET_OUTPUT_CHANNEL(14006),
    LOG_BOOK_SET_ALARM(14007),
    LOG_BOOK_SET_TAG(14008),
    LOG_BOOK_SET_INVERSE(14009),
    LOG_BOOK_SET_IMMEDIATE(14010),
    LOG_BOOK_READ_DEBUG(1401),
    LOG_BOOK_READ_MANUFACTURER_SPECIFIC(14012),
    LOG_BOOK_RESET_MAIN_LOGBOOK(14013),
    LOG_BOOK_RESET_COVER_LOGBOOK(14014),
    LOG_BOOK_RESET_BREAKER_LOGBOOK(14015),
    LOG_BOOK_RESET_COMMUNICATION_LOGBOOK(14016),
    LOG_BOOK_RESET_LQI_LOGBOOK(14017),
    LOG_BOOK_RESET_VOLTAGE_CUT_LOGBOOK(14018),

    CLOCK_SET_TIME(15001),
    CLOCK_SET_TIMEZONE_OFFSET(15002),
    CLOCK_ENABLE_OR_DISABLE_DST(15003),
    CLOCK_SET_END_OF_DST(15004),
    CLOCK_SET_START_OF_DST(15005),
    CLOCK_SET_START_OF_DST_WITHOUT_HOUR(15006),
    CLOCK_SET_END_OF_DST_WITHOUT_HOUR(15007),
    CLOCK_SET_DST_ALGORITHM(15008),
    CLOCK_SET_DST(15009),
    CLOCK_SET_TIMEZONE(15010),
    CLOCK_SET_TIME_ADJUSTMENT(15011),
    CLOCK_SET_NTP_SERVER(15012),
    CLOCK_SET_REFRESH_CLOCK_EVERY(15013),
    CLOCK_SET_NTP_OPTIONS(15014),
    CLOCK_SET_SYNCHRONIZE_TIME(15015),
    CLOCK_SET_CONFIGURE_DST(15016),
    CLOCK_SET_CONFIRUE_DST_WITHOUT_HOUR(15017),

    PEAK_SHAVING_SET_ACTIVE_CHANNEL(16001),
    PEAK_SHAVING_SET_REACTIVE_CHANNEL(16002),
    PEAK_SHAVING_SET_TIME_BASE(16003),
    PEAK_SHAVING_SET_P_OUT(16004),
    PEAK_SHAVING_SET_P_IN(16005),
    PEAK_SHAVING_SET_DEAD_TIME(16006),
    PEAK_SHAVING_SET_AUTOMATIC(16007),
    PEAK_SHAVING_SET_CYCLIC(16008),
    PEAK_SHAVING_SET_INVERT(16009),
    PEAK_SHAVING_SET_ADAPT_SETPOINT(16010),
    PEAK_SHAVING_SET_INSTANT_ANALOG_OUT(16011),
    PEAK_SHAVING_SET_PREDICTED_ANALOG_OUT(16012),
    PEAK_SHAVING_SETPOINT_ANALOG_OUT(16013),
    PEAK_SHAVING_SET_DIFFERENCE_ANALOG_OUT(16014),
    PEAK_SHAVING_SET_TARIFF(16015),
    PEAK_SHAVING_SET_RESET_LOADS(16016),
    PEAK_SHAVING_SET_SETPOINT(16017),
    PEAK_SHAVING_SET_SWITCH_TIME(16018),
    PEAK_SHAVING_SET_LOAD(16019),

    MAIL_CONFIGURATION_SET_POP_USERNAME(17001),
    MAIL_CONFIGURATION_SET_POP_PASSWORD(17002),
    MAIL_CONFIGURATION_SET_POP_HOST(17003),
    MAIL_CONFIGURATION_SET_POP_READ_MAIL_EVERY(17004),
    MAIL_CONFIGURATION_SET_POP3_OPTIONS(17005),
    MAIL_CONFIGURATION_SET_SMTP_FROM(17006),
    MAIL_CONFIGURATION_SET_SMTP_TO(17007),
    MAIL_CONFIGURATION_SET_SMTP_CONFIGURATION_TO(17008),
    MAIL_CONFIGURATION_SET_SMTP_SERVER(17009),
    MAIL_CONFIGURATION_SET_SMTP_DOMAIN(17010),
    MAIL_CONFIGURATION_SET_SMTP_SEND_MAIL_EVERY(17011),
    MAIL_CONFIGURATION_SET_SMTP_CURRENT_INTERVAL(17012),
    MAIL_CONFIGURATION_SET_SMTP_DATABASE_ID(17013),
    MAIL_CONFIGURATION_SET_SMTP_OPTIONS(17014),

    EIWEB_SET_PASSWORD(18001),
    EIWEB_SET_PAGE(18002),
    EIWEB_SET_FALLBACK_PAGE(18003),
    EIWEB_SET_SEND_EVERY(18004),
    EIWEB_SET_CURRENT_INTERVAL(18005),
    EIWEB_SET_DATABASE_ID(18006),
    EIWEB_SET_OPTIONS(18007),

    PPP_CONFIGURATION_SET_ISP1_PHONE(19001),
    PPP_CONFIGURATION_SET_ISP1_USERNAME(19002),
    PPP_CONFIGURATION_SET_ISP1_PASSWORD(19003),
    PPP_CONFIGURATION_SET_ISP1_TRIES(19004),
    PPP_CONFIGURATION_SET_ISP2_PHONE(19005),
    PPP_CONFIGURATION_SET_ISP2_USERNAME(19006),
    PPP_CONFIGURATION_SET_ISP2_PASSWORD(19007),
    PPP_CONFIGURATION_SET_ISP2_TRIES(19008),
    PPP_CONFIGURATION_SET_IDLE_TIMEOUT(19009),
    PPP_CONFIGURATION_SET_RETRY_INTERVAL(19010),
    PPP_CONFIGURATION_SET_OPTIONS(19011),
    PPP_CONFIGURATION_SET_IDLE_TIME(19012),

    MODEM_CONFIGURATION_SET_DIAL_COMMAND(20001),
    MODEM_CONFIGURATION_SET_MODEM_INIT_1(20002),
    MODEM_CONFIGURATION_SET_MODEM_INIT_2(20003),
    MODEM_CONFIGURATION_SET_MODEM_INIT_3(20004),
    MODEM_CONFIGURATION_SET_PPP_BAUD_RATE(20005),
    MODEM_CONFIGURATION_SET_MODEMTYPE(20006),
    MODEM_CONFIGURATION_SET_RESET_CYCLE(20007),

    SMS_CONFIGURATION_SET_DATA_NUMBER(21001),
    SMS_CONFIGURATION_SET_ALARM_NUMBER(21002),
    SMS_CONFIGURATION_SET_EVERY(21003),
    SMS_CONFIGURATION_SET_NUMBER(21004),
    SMS_CONFIGURATION_SET_CORRECTION(21005),
    SMS_CONFIGURATION_SET_CONFIG(21006),

    MODBUS_CONFIGURATION_SET_MM_EVERY(22001),
    MODBUS_CONFIGURATION_SET_MM_TIMEOUT(22002),
    MODBUS_CONFIGURATION_SET_MM_INSTANT(22003),
    MODBUS_CONFIGURATION_SET_MM_OVERFLOW(22004),
    MODBUS_CONFIGURATION_SET_MM_CONFIG(22005),
    MODBUS_CONFIGURATION_WRITE_SINGLE_REGISTERS(22006),
    MODBUS_CONFIGURATION_WRITE_MULTIPLE_REGISTERS(22007),

    MBUS_CONFIGURATION_SET_EVERY(23001),
    MBUS_CONFIGURATION_SET_INTER_FRAME_TIME(23002),
    MBUS_CONFIGURATION_SET_CONFIG(23003),
    MBUS_CONFIGURATION_SET_VIF(23004),

    MBUS_SETUP_DECOMMISSION(24001),
    MBUS_SETUP_DATA_READOUT(24002),
    MBUS_SETUP_COMMISSION(24003),
    MBUS_SETUP_DECOMMISSION_ALL(24004),
    MBUS_SETUP_SET_ENCRYPTION_KEYS(24005),
    MBUS_SETUP_SET_ENCRYPTION_KEYS_USING_CRYPTOSERVER(24006),
    MBUS_SETUP_USE_CORRECTED_VALUES(24007),
    MBUS_SETUP_USE_UNCORRECTED_VALUES(24008),
    MBUS_SETUP_COMMISSION_WITH_CHANNEL(24009),
    MBUS_SETUP_WRITE_CAPTURE_DEFINITION_FOR_ALL_INSTANCES(24010),
    MBUS_SETUP_WRITE_CAPTURE_PERIOD(24011),

    OPUS_CONFIGURATION_SET_OS_NUMBER(25001),
    OPUS_CONFIGURATION_SET_PASSWORD(25002),
    OPUS_CONFIGURATION_SET_TIMEOUT(25003),
    OPUS_CONFIGURATION_SET_CONFIG(25004),

    POWER_CONFIGURATION_IEC1107_LIMIT_POWER_QUALITY(26001),
    POWER_CONFIGURATION_SET_REFERENCE_VOLTAGE(26002),
    POWER_CONFIGURATION_SET_VOLTAGE_SAG_TIME_THRESHOLD(26003),
    POWER_CONFIGURATION_SET_VOLTAGE_SWELL_TIME_THRESHOLD(26004),
    POWER_CONFIGURATION_SET_VOLTAGE_SAG_THRESHOLD(26005),
    POWER_CONFIGURATION_SET_VOLTAGE_SWELL_THRESHOLD(26006),
    POWER_CONFIGURATION_SET_LONG_POWER_FAILURE_TIME_THRESHOLD(26007),
    POWER_CONFIGURATION_SET_LONG_POWER_FAILURE_THRESHOLD(26008),

    PREPAID_CONFIGURATION_ADD_CREDIT(27001),
    PREPAID_CONFIGURATION_DISABLE(27002),
    PREPAID_CONFIGURATION_ENABLE(27003),

    DLMS_CONFIGURATION_SET_DEVICE_ID(28001),
    DLMS_CONFIGURATION_SET_METER_ID(28002),
    DLMS_CONFIGURATION_SET_PASSWORD(28003),
    DLMS_CONFIGURATION_SET_IDLE_TIME(28004),

    CHANNEL_CONFIGURATION_SET_FUNCTION(29001),
    CHANNEL_CONFIGURATION_SET_PARAMETERS(29002),
    CHANNEL_CONFIGURATION_SET_NAME(29003),
    CHANNEL_CONFIGURATION_SET_UNIT(29004),
    CHANNEL_CONFIGURATION_SET_LP_DIVISOR(29005),

    TOTALIZER_CONFIGURATION_SET_SUM_MASK(30001),
    TOTALIZER_CONFIGURATION_SET_SUBSTRACT_MASK(30002),

    CONFIGURATION_CHANGE_WRITE_EXCHANGE_STATUS(31001),
    CONFIGURATION_CHANGE_WRITE_RADIO_ACKNOWLEDGE(31002),
    CONFIGURATION_CHANGE_WRITE_RADIO_USER_TIMEOUT(31003),
    CONFIGURATION_CHANGE_WRITE_NEW_PDR_NUMBER(31004),
    CONFIGURATION_CHANGE_CONFIGURE_CONVERTER_MASTER_DATA(31005),
    CONFIGURATION_CHANGE_CONFIGURE_GAS_METER_MASTER_DATA(31006),
    CONFIGURATION_CHANGE_CONFIGURE_GAS_PARAMETERS(31007),
    CONFIGURATION_CHANGE_SET_DESCRIPTION(31008),
    CONFIGURATION_CHANGE_SET_INTERVAL_IN_SECONDS(31009),
    CONFIGURATION_CHANGE_SET_UPGRADE_URL(31010),
    CONFIGURATION_CHANGE_SET_UPGRADE_OPTIONS(31011),
    CONFIGURATION_CHANGE_SET_DEBOUNCE_TRESHOLD(31012),
    CONFIGURATION_CHANGE_SET_TARIFF_MOMENT(31013),
    CONFIGURATION_CHANGE_SET_COMMUNICATION_OFFSET(31014),
    CONFIGURATION_CHANGE_SET_AGGREGATION_INTERVAL(31015),
    CONFIGURATION_CHANGE_SET_PULSE_TIME_TRUE(31016),
    CONFIGURATION_CHANGE_SET_DUKE_POWER_ID(31017),
    CONFIGURATION_CHANGE_SET_DUKE_POWER_PASSWORD(31018),
    CONFIGURATION_CHANGE_SET_DUKE_POWER_IDLE_TIME(31019),
    CONFIGURATION_CHANGE_UPLOAD_METER_SCHEME(31020),
    CONFIGURATION_CHANGE_UPLOAD_SWITCH_POINT_CLOCK_SETTINGS(31021),
    CONFIGURATION_CHANGE_UPLOAD_SWITCH_POINT_CLOCK_UPDATE_SETTINGS(31022),
    CONFIGURATION_CHANGE_PROGRAM_BATTERY_EXPIRY_DATE(31023),
    CONFIGURATION_CHANGE_CHANGE_OF_SUPPLIER(31024),
    CONFIGURATION_CHANGE_CHANGE_OF_TENANCY(31025),
    CONFIGURATION_CHANGE_SET_CALORIFIC_VALUE(31026),
    CONFIGURATION_CHANGE_SET_CONVERSION_FACTOR(31027),
    CONFIGURATION_CHANGE_SET_ALARM_FILTER(31028),
    CONFIGURATION_CHANGE_CHANGE_DEFAULT_RESET_WINDOW(31029),
    CONFIGURATION_CHANGE_CHANGE_ADMINISTRATIVE_STATUS(31030),
    CONFIGURATION_CHANGE_ENABLE_SSL(31031),
    CONFIGURATION_CHANGE_SET_DEVICENAME(31032),
    CONFIGURATION_CHANGE_SET_NTPADDRESS(31033),
    CONFIGURATION_CHANGE_SYNC_NTPSERVER(31034),
    CONFIGURATION_CHANGE_CONFIGURE_AUTOMATIC_DEMAND_RESET(31035),
    CONFIGURATION_CHANGE_CLEAR_FAULTS_FLAGS(31036),
    CONFIGURATION_CHANGE_CLEAR_STATISTICAL_VALUES(31037),
    CONFIGURATION_CHANGE_ENABLE_DISCOVERY_ON_POWER_UP(31038),
    CONFIGURATION_CHANGE_DISABLE_DISCOVERY_ON_POWER_UP(31039),
    CONFIGURATION_CHANGE_CONFIGURE_MASTER_BOARD_PARAMETERS(31040),
    CONFIGURATION_CHANGE_ENABLE_FW(31041),
    CONFIGURATION_CHANGE_DISABLE_FW(31042),

    ADVANCED_TEST_XML_CONFIG(32001),
    ADVANCED_TEST_USERFILE_CONFIG(32002),
    ADVANCED_TEST_LOG_OBJECT_LIST(32003),

    PUBLIC_LIGHTING_SET_RELAY_OPERATING_MODE(33001),
    PUBLIC_LIGHTING_SET_TIME_SWITCHING_TABLE(33002),
    PUBLIC_LIGHTING_SET_THRESHOLD_OVER_CONSUMPTION(33003),
    PUBLIC_LIGHTING_SET_OVERALL_MINIMUM_THRESHOLD(33004),
    PUBLIC_LIGHTING_SET_OVERALL_MAXIMUM_THRESHOLD(33005),
    PUBLIC_LIGHTING_SET_RELAY_TIME_OFFSETS_TABLE(33006),
    PUBLIC_LIGHTING_WRITE_GPS_COORDINATES(33007),


    FIREWALL_ACTIVATE_FIREWALL(34001),
    FIREWALL_DEACTIVATE_FIREWALL(34002),
    FIREWALL_CONFIGURE_FW_WAN(34003),
    FIREWALL_CONFIGURE_FW_LAN(34004),
    FIREWALL_CONFIGURE_FW_GPRS(34005),
    FIREWALL_SET_FW_DEFAULT_STATE(34006),

    OUTPUT_CONFIGURATION_SET_OUTPUT_ON(35001),
    OUTPUT_CONFIGURATION_SET_OUTPUT_OFF(35002),
    OUTPUT_CONFIGURATION_SET_OUTPUT_TOGGLE(35003),
    OUTPUT_CONFIGURATION_SET_OUTPUT_PULSE(35004),
    OUTPUT_CONFIGURATION_OUTPUT_ON(35005),
    OUTPUT_CONFIGURATION_OUTPUT_OFF(35006),
    OUTPUT_CONFIGURATION_ABSOLUTE_DO_SWITCH_RULE(35007),
    OUTPUT_CONFIGURATION_DELETE_DO_SWITCH_RULE(35008),
    OUTPUT_CONFIGURATION_RELATIVE_DO_SWITCH_RULE(35009),
    OUTPUT_CONFIGURATION_WRITE_OUTPUT_STATE(35010),


    UPLINK_CONFIGURATION_ENABLE_PING(36001),
    UPLINK_CONFIGURATION_WRITE_UPLINK_PING_DESTINATION_ADDRESS(36002),
    UPLINK_CONFIGURATION_WRITE_UPLINK_PING_INTERVAL(36003),
    UPLINK_CONFIGURATION_WRITE_UPLINK_PING_TIMEOUT(36004),

    LOGGING_CONFIGURATION_DEVICE_MESSAGE_SET_SERVER_LOG_LEVEL(37001),
    LOGGING_CONFIGURATION_DEVICE_MESSAGE_SET_WEB_PORTAL_LOG_LEVEL(37002),
    LOGGING_CONFIGURATION_DEVICE_MESSAGE_DOWNLOAD_FILE(37003),
    LOGGING_CONFIGURATION_DEVICE_MESSAGE_PUSH_CONFIGURATION(37004),
    LOGGING_CONFIGURATION_DEVICE_MESSAGE_PUSH_LOGS_NOW(37005),
    ;

    private long dbId;

    DeviceMessageId(long dbId) {
        this.dbId = dbId;
    }

    public long dbValue() {
        return this.dbId;
    }

    public static DeviceMessageId havingId(long dbId){
        for(DeviceMessageId messageId : DeviceMessageId.values()) {
            if (messageId.dbValue() == dbId) {
                return messageId;
            }
        }
        throw new IllegalArgumentException(String.format("No DeviceMessageId found having id %d", dbId));
    }

    public static Set<DeviceMessageId> fileManagementRelated() {
        return EnumSet.of(
                DeviceMessageId.GENERAL_WRITE_FULL_CONFIGURATION,
                DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_METER_SCHEME,
                DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_SWITCH_POINT_CLOCK_SETTINGS,
                DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_SWITCH_POINT_CLOCK_UPDATE_SETTINGS,
                DeviceMessageId.ADVANCED_TEST_USERFILE_CONFIG,
                DeviceMessageId.ACTIVITY_CALENDAR_WRITE_CONTRACTS_FROM_XML_USERFILE);
    }

}