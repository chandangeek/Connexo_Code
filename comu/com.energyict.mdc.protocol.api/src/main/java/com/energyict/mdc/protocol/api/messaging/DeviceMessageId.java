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
    ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_FROM_XML_USER_FILE(13),
    ACTIVITY_CALENDER_ACTIVITY_CALENDAR_SEND_WITH_DATETIME_FROM_XML_USER_FILE(14),
    ACTIVITY_CALENDER_ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_DEFAULT_TARIFF_CODE(15),
    ACTIVITY_CALENDER_ACTIVITY_CALENDAR_WITH_DATETIME_FROM_XML(16),
    ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_WITH_GIVEN_TABLE_OBIS_FROM_XML(17),
    ACTIVITY_CALENDER_SELECTION_OF_12_LINES_IN_TOU_TABLE(18),

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
    CONTACTOR_OPEN_WITH_OUTPUT_AND_ACTIVATION_DATE(1013),
    CONTACTOR_CLOSE_WITH_OUTPUT_AND_ACTIVATION_DATE(1014),
    CONTACTOR_OPEN_WITH_DATA_PROTECTION(1015),
    CONTACTOR_CLOSE_WITH_DATA_PROTECTION(1016),
    CONTACTOR_ACTION_WITH_ACTIVATION(1017),
    REMOTE_DISCONNECT_WITH_DATA_PROTECTION_AND_ACTIVATION(1018),
    REMOTE_CONNECT_WITH_DATA_PROTECTION_AND_ACTIVATION(1019),

    ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS(2001),
    ALARM_CONFIGURATION_WRITE_ALARM_FILTER(2002),
    ALARM_CONFIGURATION_RESET_ALL_ERROR_BITS(2003),
    ALARM_CONFIGURATION_CONFIGURE_PUSH_EVENT_NOTIFICATION(2004),
    ALARM_CONFIGURATION_RESET_DESCRIPTOR_FOR_ALARM_REGISTER_1_OR_2(2005),
    ALARM_CONFIGURATION_RESET_BITS_IN_ALARM_REGISTER_1_OR_2(2006),
    ALARM_CONFIGURATION_WRITE_FILTER_FOR_ALARM_REGISTER_1_OR_2(2007),
    ALARM_CONFIGURATION_FULLY_CONFIGURE_PUSH_EVENT_NOTIFICATION(2008),
    ALARM_CONFIGURATION_CONFIGURE_PUSH_EVENT_NOTIFICATION_OBJECT_DEFINITIONS(2009),
    ALARM_CONFIGURATION_CONFIGURE_PUSH_EVENT_NOTIFICATION_SEND_DESTINATION(2010),
    ALARM_CONFIGURATION_ENABLE_EVENT_NOTIFICATIONS(2011),
    ALARM_CONFIGURATION_RESET_DESCRIPTOR_FOR_ALARM_REGISTER(2012),
    ALARM_CONFIGURATION_RESET_BITS_IN_ALARM_REGISTER(2013),
    ALARM_CONFIGURATION_WRITE_FILTER_FOR_ALARM_REGISTER(2014),
    RESET_DESCRIPTOR_FOR_SINGLE_ALARM_REGISTER(2015),
    RESET_BITS_IN_ALARM_SINGLE_REGISTER(2016),
    WRITE_FILTER_FOR_SINGLE_ALARM_REGISTER(2017),
    CONFIGURE_PUSH_EVENT_NOTIFICATION_CIPHERING(2018),
    CONFIGURE_PUSH_EVENT_SEND_TEST_NOTIFICATION(2019),

    PLC_CONFIGURATION_FORCE_MANUAL_RESCAN_PLC_BUS(3001),
    PLC_CONFIGURATION_SET_MULTICAST_ADDRESSES(3002),
    PLC_CONFIGURATION_SET_ACTIVE_CHANNEL(3003),
    PLC_CONFIGURATION_SET_CHANNEL_FREQUENCIES(3004),
    PLC_CONFIGURATION_SET_SFSK_INITIATOR_PHASE(3005),
    PLC_CONFIGURATION_SET_SFSK_MAX_FRAME_LENGTH(3006),
    PLC_CONFIGURATION_WRITE_PLC_G3_TIMEOUT(3017),
    PLC_CONFIGURATION_RESET_PLC_OFDM_MAC_COUNTERS(3018),
    PLC_CONFIGURATION_SET_PAN_ID(3019),
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
    PLC_CONFIGURATION_PRIME_CANCEL_FIRMWARE_UPGRADE(3065),
    PLC_CONFIGURATION_PRIME_READ_PIB(3066),
    PLC_CONFIGURATION_PRIME_REQUEST_FIRMWARE_VERSION(3067),
    PLC_CONFIGURATION_PRIME_WRITE_PIB(3068),
    PLC_CONFIGURATION_ENABLE_DISABLE(3069),
    PLC_CONFIGURATION_FREQ_PAIR_SELECTION(3070),
    PLC_CONFIGURATION_REQUEST_CONFIGURATION(3071),
    PLC_CONFIGURATION_CIASE_DISCOVERY_MAX_CREDITS(3072),
    PLC_CONFIGURATION_CHANGE_MAC_ADDRESS(3073),
    PLC_CONFIGURATION_IDIS_DISCOVERY_CONFIGURATION(3074),
    PLC_CONFIGURATION_IDIS_REPEATER_CALL_CONFIGURATION(3075),
    PLC_CONFIGURATION_IDIS_RUN_REPEATER_CALL_NOW(3076),
    PLC_CONFIGURATION_IDIS_RUN_NEW_METER_DISCOVERY_CALL_NOW(3077),
    PLC_CONFIGURATION_IDIS_RUN_ALARM_DISCOVERY_CALL_NOW(3078),
    PLC_CONFIGURATION_IDIS_WHITELIST_CONFIGURATION(3079),
    PLC_CONFIGURATION_IDIS_OPERATING_WINDOW_CONFIGURATION(3080),
    PLC_CONFIGURATION_IDIS_PHY_CONFIGURATION(3081),
    PLC_CONFIGURATION_IDIS_CREDIT_MANAGEMENT_CONFIGURATION(3082),
    PLC_CONFIGURATION_PING_METER(3083),
    PLC_CONFIGURATION_ADD_METERS_TO_BLACK_LIST(3084),
    PLC_CONFIGURATION_REMOVE_METERS_FROM_BLACK_LIST(3085),
    PLC_CONFIGURATION_KICK_METER(3086),
    PLC_CONFIGURATION_PATH_REQUEST_WITH_TIMEOUT(3087),
    PLC_CONFIGURATION_SET_LOW_LQI_VALUE_ATTRIBUTE_NAME(3088),
    PLC_CONFIGURATION_SET_HIGH_LQI_VALUE_ATTRIBUTE_NAME(3089),

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
    NETWORK_CONNECTIVITY_SET_USE_DHCP_FLAG(4037),
    NETWORK_CONNECTIVITY_SET_PRIMARY_DNS_ADDRESS(4038),
    NETWORK_CONNECTIVITY_SET_SECONDARY_DNS_ADDRESS(4039),
    NETWORK_CONNECTIVITY_SET_AUTO_CONNECT_MODE(4040),
    NETWORK_CONNECTIVITY_CHANGE_SESSION_TIMEOUT(4041),
    NETWORK_CONNECTIVITY_SET_CYCLIC_MODE(4042),
    NETWORK_CONNECTIVITY_SET_PREFERRED_DATE_MODE(4043),
    NETWORK_CONNECTIVITY_SET_WAN_CONFIGURATION(4044),
    NETWORK_CONNECTIVITY_WAKEUP_PARAMETERS(4045),
    NETWORK_CONNECTIVITY_PREFERRED_NETWORK_OPERATOR_LIST(4046),
    NETWORK_CONNECTIVITY_CONFIGURE_AUTO_ANSWER(4047),
    NETWORK_CONNECTIVITY_DISABLE_AUTO_ANSWER(4048),
    NETWORK_CONNECTIVITY_CONFIGURE_AUTO_CONNECT(4049),
    NETWORK_CONNECTIVITY_DISABLE_AUTO_CONNECT(4050),
    NETWORK_CONNECTIVITY_SET_MODEM_WATCHDOG_PARAMETERS_2(4051),
    NETWORK_CONNECTIVITY_ENABLE_NETWORK_INTERFACES(4052),
    NETWORK_CONNECTIVITY_SET_HTTPS_PORT(4053),
    EnableNetworkInterfacesForSetupObject(4054),

    FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER(5001),
    FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE(5002),
    FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE_ACTIVATE_IMMEDIATE(5003),
    FIRMWARE_UPGRADE_ACTIVATE(5004),
    FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE(5005),
    FIRMWARE_UPGRADE_WITH_USER_FILE_VERSION_AND_ACTIVATE_DATE(5006),
    FIRMWARE_UPGRADE_URL_ACTIVATE_IMMEDIATE(5007),
    FIRMWARE_UPGRADE_URL_AND_ACTIVATE_DATE(5008),
    FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE(5009),
    FIRMWARE_UPGRADE_UPGRADE_WAVE_CARD(5010),
    FIRMWARE_UPGRADE_PLC_PRIME_SET_FIRMWARE_UPGRADE_FILE(5011),
    FIRMWARE_UPGRADE_PLC_PRIME_START_FIRMWARE_UPGRADE_NODE_LIST(5012),
    FIRMWARE_UPGRADE_FTION_UPGRADE_RF_MESH_FIRMWARE(5013),
    FIRMWARE_UPGRADE_RF_MESH_UPGRADE_URL(5014),
    FIRMWARE_UPGRADE_UPGRADE_BOOTLOADER(5015),
    FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER(5016),
    FIRMWARE_UPGRADE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER(5017),
    FIRMWARE_UPGRADE_BROADCAST_FIRMWARE_UPGRADE(5018),
    FIRMWARE_UPGRADE_VERIFY_AND_ACTIVATE_FIRMWARE(5019),
    FIRMWARE_UPGRADE_DATA_CONCENTRATOR_MULTICAST_FIRMWARE_UPGRADE(5020),
    FIRMWARE_UPGRADE_READ_MULTICAST_PROGRESS(5021),
    FIRMWARE_UPGRADE_WITH_URL_JAR_JAD_FILE_SIZE(5022),
    FIRMWARE_UPGRADE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER(5023),
    FIRMWARE_UPGRADE_ENABLE_IMAGE_TRANSFER(5024),
    FIRMWARE_UPGRADE_TRANSFER_SLAVE_FIRMWARE_FILE_TO_DATA_CONCENTRATOR(5025),
    FIRMWARE_UPGRADE_CONFIGURE_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES(5026),
    FIRMWARE_UPGRADE_START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES(5027), FIRMWARE_IMAGE_ACTIVATION_WITH_DATA_PROTECTION(5028),
    COPY_ACTIVE_FIRMWARE_TO_INACTIVE_PARTITION(5029),
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER_AND_RESUME(5030),
    VerifyAndActivateFirmwareAtGivenDate(5031),
    FIRMWARE_IMAGE_ACTIVATION_WITH_DATA_PROTECTION_AND_ACTIVATION_DATE(5032),

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
    ZIGBEE_CONFIGURATION_UPDATE_LINK_KEY(6013),
    ZIGBEE_CONFIGURATION_JOIN_SLAVE_FROM_DEVICE_TYPE(6014),

    SECURITY_ACTIVATE_DLMS_ENCRYPTION(7001),
    SECURITY_CHANGE_DLMS_AUTHENTICATION_LEVEL(7002),
    SECURITY_CHANGE_CLIENT_PASSWORDS(7004),
    SECURITY_WRITE_PSK(7005),
    SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY(7006),
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
    SECURITY_BREAK_OR_RESTORE_SEALS(7018),
    SECURITY_TEMPORARY_BREAK_SEALS(7019),
    SECURITY_GENERATE_NEW_PUBLIC_KEY(7020),
    SECURITY_GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM(7021),
    SECURITY_SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP(7022),
    SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P0(7023),
    SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P0(7025),
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
    SECURITY_CHANGE_SECURITY_KEYS(7037),
    SECURITY_ACTIVATE_DLMS_SECURITY_VERSION1(7038),
    SECURITY_AGREE_NEW_ENCRYPTION_KEY(7039),
    SECURITY_AGREE_NEW_AUTHENTICATION_KEY(7040),
    SECURITY_CHANGE_SECURITY_SUITE(7041),
    SECURITY_EXPORT_END_DEVICE_CERTIFICATE(7042),
    SECURITY_EXPORT_SUB_CA_CERTIFICATES(7043),
    SECURITY_EXPORT_ROOT_CA_CERTIFICATE(7044),
    SECURITY_DELETE_CERTIFICATE_BY_TYPE(7045),
    SECURITY_DELETE_CERTIFICATE_BY_SERIAL_NUMBER(7046),
    SECURITY_GENERATE_KEY_PAIR(7047),
    SECURITY_GENERATE_CSR(7048),
    SECURITY_CHANGE_WEBPORTAL_PASSWORD1(7049),
    SECURITY_IMPORT_CA_CERTIFICATE(7050),
    SECURITY_IMPORT_END_DEVICE_CERTIFICATE(7051),
    IMPORT_SERVER_END_DEVICE_CERTIFICATE(7052),
    CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY(7053),
    CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY(7054),
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS_FOR_CLIENT(7055),
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS_FOR_CLIENT(7056),
    CHANGE_HLS_SECRET_PASSWORD_FOR_CLIENT(7057),
    CHANGE_MASTER_KEY_WITH_NEW_KEYS(7058),
    CHANGE_MASTER_KEY_WITH_NEW_KEYS_FOR_CLIENT(7059),
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS_FOR_PREDEFINED_CLIENT(7060),
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS_FOR_PREDEFINED_CLIENT(7061),
    CHANGE_MASTER_KEY_WITH_NEW_KEYS_FOR_PREDEFINED_CLIENT(7062),
    SET_REQUIRED_PROTECTION_FOR_DATA_PROTECTION_SETUP(7063),

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
    DEVICE_ACTIONS_SYNC_MASTERDATA_FOR_DC(8048),
    DEVICE_ACTIONS_PAUSE_DC_SCHEDULER(8049),
    DEVICE_ACTIONS_RESUME_DC_SCHEDULER(8050),
    DEVICE_ACTIONS_SYNC_DEVICE_DATA_FOR_DC(8051),
    DEVICE_ACTIONS_SYNC_ONE_CONFIGURATION_FOR_DC(8052),
    DEVICE_ACTIONS_TRIGGER_PRELIMINARY_PROTOCOL(8053), SyncAllDevicesWithDC(8054),
    SyncOneDeviceWithDC(8055),
    SyncOneDeviceWithDCAdvanced(8056),
    SetBufferForAllLoadProfiles(8057),
    SetBufferForSpecificLoadProfile(8058),
    SetBufferForAllEventLogs(8059),
    SetBufferForSpecificEventLog(8060),
    SetBufferForAllRegisters(8061),
    SetBufferForSpecificRegister(8062),
    BillingResetWithActivationDate(8063),
    RemoveLogicalDevice(8064),

    PRICING_GET_INFORMATION(9001),
    PRICING_SET_INFORMATION(9002),
    PRICING_SET_STANDING_CHARGE(9003),
    PRICING_UPDATE_INFORMATION(9004),
    PRICING_SET_STANDING_CHARGE_AND_ACTIVATION_DATE(9005),
    PRICING_SEND_NEW_TARIFF(9006),
    PRICING_SEND_NEW_PRICE_MATRIX(9007),
    PRICING_SET_CURRENCY_AND_ACTIVATION_DATE(9008),
    PRICING_SET_SET_CURRENCY(9009),

    DISPLAY_CONSUMER_MESSAGE_CODE_TO_PORT_P1(10001),
    DISPLAY_CONSUMER_MESSAGE_TEXT_TO_PORT_P1(10002),
    DISPLAY_SET_MESSAGE(10003),
    DISPLAY_SET_MESSAGE_WITH_OPTIONS(10004),
    DISPLAY_SET_MESSAGE_ON_IHD_WITH_OPTIONS(10005),
    DISPLAY_CLEAR_MESSAGE(10006),

    GENERAL_WRITE_RAW_IEC1107_CLASS(11001),
    GENERAL_WRITE_FULL_CONFIGURATION(11002),
    GENERAL_SEND_XML_MESSAGE(11003),

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
    LOAD_BALANCING_UPDATE_SUPERVISION_MONITOR(12020),
    LOAD_BALANCING_CONFIGURE_SUPERVISION_MONITOR_FOR_IMPORT_EXPORT(12021),
    CONFIGURE_LOAD_LIMIT_PARAMETERS_EXCEPT_EMERGENCY_ONES(12022),
    CONFIGURE_LOAD_LIMIT_PARAMETERS_ATTRIBUTES_4TO9(12023),

    LOAD_PROFILE_PARTIAL_REQUEST(13001),
    LOAD_PROFILE_RESET_ACTIVE_IMPORT(13002),
    LOAD_PROFILE_RESET_ACTIVE_EXPORT(13003),
    LOAD_PROFILE_RESET_DAILY(13004),
    LOAD_PROFILE_RESET_MONTHLY(13005),
    LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP1(13006),
    LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP2(13007),
    LOAD_PROFILE_WRITE_CONSUMER_PRODUCER_MODE(13008),
    LOAD_PROFILE_REGISTER_REQUEST(13009),
    LOAD_PROFILE_READ_PROFILE_DATA(13010),
    LOAD_PROFILE_LOAD_PROFILE_OPT_IN_OUT(13011),
    LOAD_PROFILE_SET_DISPLAY_ON_OFF(13012),
    WRITE_MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUES(13013),

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
    LOG_BOOK_READ_DEBUG(14011),
    LOG_BOOK_READ_MANUFACTURER_SPECIFIC(14012),
    LOG_BOOK_RESET_MAIN_LOGBOOK(14013),
    LOG_BOOK_RESET_COVER_LOGBOOK(14014),
    LOG_BOOK_RESET_BREAKER_LOGBOOK(14015),
    LOG_BOOK_RESET_COMMUNICATION_LOGBOOK(14016),
    LOG_BOOK_RESET_LQI_LOGBOOK(14017),
    LOG_BOOK_RESET_VOLTAGE_CUT_LOGBOOK(14018),
    LOG_BOOK_READ(14019),
    LOG_BOOK_RESET_SECURITY_LOGBOOK(14020),
    ResetSecurityGroupEventCounterObjects(14021),
    ResetAllSecurityGroupEventCounters(14022),

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
    CLOCK_SET_FTION_FORCE_TIME_SYNC(15018),
    CLOCK_SET_NTP_OPTION(15019),
    CLOCK_CLEAR_NTP_OPTION(15020),
    CLOCK_SET_CONFIGURE(15021),

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
    MAIL_CONFIGURATION_POP_3_SET_OPTION(17015),
    MAIL_CONFIGURATION_POP_3_CLEAR_OPTION(17016),
    MAIL_CONFIGURATION_SMTP_3_SET_OPTION(17017),
    MAIL_CONFIGURATION_SMTP_3_CLEAR_OPTION(17018),

    EIWEB_SET_PASSWORD(18001),
    EIWEB_SET_PAGE(18002),
    EIWEB_SET_FALLBACK_PAGE(18003),
    EIWEB_SET_SEND_EVERY(18004),
    EIWEB_SET_CURRENT_INTERVAL(18005),
    EIWEB_SET_DATABASE_ID(18006),
    EIWEB_SET_OPTIONS(18007),
    EIWEB_UPDATE_EI_WEB_SSL_CERTIFICATE(18008),
    EIWEB_SET_OPTION(18009),
    EIWEB_CLEAR_OPTION(18010),

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
    PPP_CONFIGURATION_SET_OPTION(19013),
    PPP_CONFIGURATION_CLEAR_OPTION(19014),

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
    SMS_CONFIGURATION_SET_OPTION(21007),
    SMS_CONFIGURATION_CLEAR_OPTION(21008),

    MODBUS_CONFIGURATION_SET_MM_EVERY(22001),
    MODBUS_CONFIGURATION_SET_MM_TIMEOUT(22002),
    MODBUS_CONFIGURATION_SET_MM_INSTANT(22003),
    MODBUS_CONFIGURATION_SET_MM_OVERFLOW(22004),
    MODBUS_CONFIGURATION_SET_MM_CONFIG(22005),
    MODBUS_CONFIGURATION_WRITE_SINGLE_REGISTERS(22006),
    MODBUS_CONFIGURATION_WRITE_MULTIPLE_REGISTERS(22007),
    MODBUS_CONFIGURATION_SET_OPTION(22008),
    MODBUS_CONFIGURATION_CLEAR_OPTION(22009),
    MODBUS_CONFIGURATION_WRITE_MULTIPLE_COILS(22010),
    MODBUS_CONFIGURATION_WRITE_SINGLE_COIL(22011),

    MBUS_CONFIGURATION_SET_EVERY(23001),
    MBUS_CONFIGURATION_SET_INTER_FRAME_TIME(23002),
    MBUS_CONFIGURATION_SET_CONFIG(23003),
    MBUS_CONFIGURATION_SET_VIF(23004),
    MBUS_CONFIGURATION_SET_OPTION(23005),
    MBUS_CONFIGURATION_CLEAR_OPTION(23006),

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
    MBUS_SETUP_WRITE_CAPTURE_DEFINITION(24012),
    MBUS_SETUP_RESET_CLIENT(24013),
    MBUS_SETUP_CHANGE_ATTRIBUTES(24014),
    MBUS_SETUP_CLIENT_REMOTE_COMMISSION(24015),
    ScanAndInstallWiredMbusDevices(24016),
    InstallWirelessMbusDevices(24017),
    ScanAndInstallWiredMbusDeviceForGivenMeterIdentification(24018),
    InstallWirelessMbusDeviceForGivenMeterIdentification(24019),

    OPUS_CONFIGURATION_SET_OS_NUMBER(25001),
    OPUS_CONFIGURATION_SET_PASSWORD(25002),
    OPUS_CONFIGURATION_SET_TIMEOUT(25003),
    OPUS_CONFIGURATION_SET_CONFIG(25004),
    OPUS_CONFIGURATION_SET_OPTION(25005),
    OPUS_CONFIGURATION_CLEAR_OPTION(25006),

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
    MeterPushNotificationSettings(28005),

    CHANNEL_CONFIGURATION_SET_FUNCTION(29001),
    CHANNEL_CONFIGURATION_SET_PARAMETERS(29002),
    CHANNEL_CONFIGURATION_SET_NAME(29003),
    CHANNEL_CONFIGURATION_SET_UNIT(29004),
    CHANNEL_CONFIGURATION_SET_LP_DIVISOR(29005),

    TOTALIZER_CONFIGURATION_SET_SUM_MASK(30001),
    TOTALIZER_CONFIGURATION_SET_SUBSTRACT_MASK(30002),
    TOTALIZER_CONFIGURATION_CLEAR(30003),

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
    CONFIGURATION_CHANGE_SET_CALORIFIC_VALUE_AND_ACTIVATION_DATE(31026),
    CONFIGURATION_CHANGE_SET_CONVERSION_FACTOR_AND_ACTIVATION_DATE(31027),
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
    CONFIGURATION_CHANGE_ENABLE_BOOT_SYNC(31043),
    CONFIGURATION_CHANGE_WHITELISTED_PHONE_NUMBERS(31044),
    CONFIGURATION_CHANGE_UPGRADE_SET_OPTION(31045),
    CONFIGURATION_CHANGE_UPGRADE_CLEAR_OPTION(31047),
    CONFIGURATION_CHANGE_CONFIGURE_BILLING_PERIOD_START_DATE(31048),
    CONFIGURATION_CHANGE_CONFIGURE_BILLING_PERIOD_LENGTH(31049),
    CONFIGURATION_CHANGE_WRITE_NEW_ON_DEMAND_BILLING_DATE(31050),
    CONFIGURATION_CHANGE_CHANGE_UNIT_STATUS(31051),
    CONFIGURATION_CHANGE_CONFIGURE_START_OF_GAS_DAY_SETTINGS(31052),
    CONFIGURATION_CHANGE_CONFIGURE_START_OF_GAS_DAY(31053),
    CONFIGURATION_CHANGE_CONFIGURE_RSSI_MULTIPLE_SAMPLING(31054),
    CONFIGURATION_CHANGE_CHANGE_OF_TENANT(31055),
    CONFIGURATION_CHANGE_CHANGE_OF_TENANT_AND_ACTIVATION_DATE(31056),
    CONFIGURATION_CHANGE_CHANGE_OF_SUPPLIER2(31057),
    CONFIGURATION_CHANGE_CHANGE_OF_SUPPLIER_AND_ACTIVATION_DATE(31058),
    CONFIGURATION_CHANGE_CHANGE_OF_SUPPLIER_IMPORT_ENERGY(31059),
    CONFIGURATION_CHANGE_CHANGE_OF_SUPPLIER_IMPORT_ENERGY_AND_ACTIVATION_DATE(31060),
    CONFIGURATION_CHANGE_CHANGE_OF_SUPPLIER_EXPORT_ENERGY(31061),
    CONFIGURATION_CHANGE_CHANGE_OF_SUPPLIER_EXPORT_ENERGY_AND_ACTIVATION_DATE(31062),
    CONFIGURATION_CHANGE_SET_ENGINEER_PIN(31063),
    CONFIGURATION_CHANGE_SET_ENGINEER_PIN_AND_ACTIVATION_DATE(31064),
    CONFIGURATION_CHANGE_SET_CALORIFIC_VALUE(31065),
    CONFIGURATION_CHANGE_SET_CONVERSION_FACTOR(31066),
    CONFIGURATION_CHANGE_CONFIGURE_ALL_GAS_PARAMETERS(31067),
    CONFIGURATION_CHANGE_CHANGE_METER_LOCATION(31068),
    CONFIGURATION_CHANGE_SEND_SHORT_DISPLAY_MESSAGE(31069),
    CONFIGURATION_CHANGE_SEND_LONG_DISPLAY_MESSAGE(31070),
    CONFIGURATION_CHANGE_RESET_DISPLAY_MESSAGE(31071),
    CONFIGURATION_CHANGE_CONFIGURE_LCD_DISPLAY(31072),
    CONFIGURATION_CHANGE_CONFIGURE_LOAD_PROFILE_DATA_RECORDING(31073),
    CONFIGURATION_CHANGE_CONFIGURE_SPECIAL_DATA_MODE(31074),
    CONFIGURATION_CHANGE_CONFIGURE_MAX_DEMAND_SETTINGS(31075),
    CONFIGURATION_CHANGE_CONFIGURE_CONSUMPTION_LIMITATIONS_SETTINGS(31076),
    CONFIGURATION_CHANGE_CONFIGURE_EMERGENCY_CONSUMPTION_LIMITATION(31077),
    CONFIGURATION_CHANGE_CONFIGURE_TARIFF_SETTINGS(31078),
    CONFIGURATION_CHANGE_ENABLE_GZIP_COMPRESSION(31079),
    CONFIGURATION_CHANGE_SET_AUTHENTICATION_MECHANISM(31080),
    CONFIGURATION_CHANGE_SET_MAX_LOGIN_ATTEMPTS(31081),
    CONFIGURATION_CHANGE_SET_LOCKOUT_DURATION(31082),
    CONFIGURATION_CHANGE_CONFIGURE_GENERAL_LOCAL_PORT_READOUT(31083),
    CONFIGURATION_CHANGE_DISABLE_PUSH_ON_INSTALLATION(31084),
    CONFIGURATION_CHANGE_ENABLE_PUSH_ON_INTERVAL_OBJECTS(31085),
    SET_DEVICE_LOG_LEVEL(31086),
    SetDeviceLocation(31087),
    SetDeviceHostName(31088),
    ConfigureAPNs(31089),
    ENABLE_PUSH_ON_INTERVAL_OBJECTS_WITH_TIME_DATE_ARRAY(31090),

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

    WAVENIS_ADD_ADDRESS_GET_NETWORK_ID(38001),
    WAVENIS_ADD_ADDRESS_WITH_NETWORK_ID(38002),
    WAVENIS_BRANCH_MOVE(38003),
    WAVENIS_CHANGE_MASTER_ADDRESS(38004),
    WAVENIS_COMPARE_REPAIRE_DATABASES(38005),
    WAVENIS_DELETE_ADDRESS(38006),
    WAVENIS_INIT_BUBBLE_UP_SLOT_DATABASE(38007),
    WAVENIS_INIT_DATABASES(38008),
    WAVENIS_PROGRAM_RADIO_ADDRESS(38009),
    WAVENIS_REMOVE_BUBBLE_UP_SLOT(38010),
    WAVENIS_REQUEST_BUBBLE_UP_SLOT(38011),
    WAVENIS_REQUEST_MODULE_STATUS(38012),
    WAVE_CARD_RADIO_ADDRESS(38013),
    WAVENIS_RESTORE_DATABASES_USING_EI_SERVER_MASTERDATA(38014),
    WAVENIS_RESTORE_BUBBLE_UP_DATABASE(38015),
    WAVENIS_RESTORE_LOCAL_FROM_EI_SERVER(38016),
    WAVENIS_RESTORE_ROOT_DATABASE_FROM_LOCAL(38017),
    WAVENIS_RESYNCHRONIZE_MODULE(38018),
    WAVENIS_FREE_REQUEST_RESPONSE(38019),
    WAVENIS_SET_RUN_LEVEL_IDLE(38020),
    WAVENIS_SET_RUN_LEVEL_INIT(38021),
    WAVENIS_SET_RUN_LEVEL_RUN(38022),
    WAVENIS_SET_FRIENDLY_NAME(38023),
    WAVENIS_SET_L1_PREFERRED_LIST(38024),
    WAVENIS_SYNCHRONIZE_MODULE(38025),
    WAVENIS_UPDATE_EI_SERVER_MASTERDATA_USING_LOCAL_DATABASES(38026),
    WAVENIS_ENABLE_DISABLE(38027);

    private long dbId;

    DeviceMessageId(long dbId) {
        this.dbId = dbId;
    }

    public long dbValue() {
        return this.dbId;
    }

    public static DeviceMessageId from(long dbId) {
        for (DeviceMessageId messageId : DeviceMessageId.values()) {
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