/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.umi;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    DOMAIN_NAME_DEVICE("domain.name.device", "Device"),

    UMI_STD_STATUS_NAME(UmiwanStdStatusCustomPropertySet.PREFIX + ".umiwanStdStatusName", "Umiwan STD status"),
    UMI_NEXT_CALL(UmiwanStdStatusCustomPropertySet.PREFIX + ".nextCall", "Next call"),
    UMI_LAST_TRY(UmiwanStdStatusCustomPropertySet.PREFIX + ".lastTry", "Last try"),
    UMI_LAST_CALL(UmiwanStdStatusCustomPropertySet.PREFIX + ".lastCall", "Last call"),
    UMI_LAST_DURATION(UmiwanStdStatusCustomPropertySet.PREFIX + ".lastDuration", "Last duration"),
    UMI_SHORT_RETRY_CTR(UmiwanStdStatusCustomPropertySet.PREFIX + ".shortRetryCtr", "Short retry counter"),
    UMI_LONG_RETRY_CTR(UmiwanStdStatusCustomPropertySet.PREFIX + ".longRetryCtr", "Long retry counter"),
    UMI_ALL_FAILURE_CTR(UmiwanStdStatusCustomPropertySet.PREFIX + ".allFailureCtr", "All failure counter"),
    UMI_ALL_RETRY_CTR(UmiwanStdStatusCustomPropertySet.PREFIX + ".allRetryCtr", "All retry counter"),
    UMI_ALL_SUCCESS_CTR(UmiwanStdStatusCustomPropertySet.PREFIX + ".allSuccessCtr", "All success counter"),
    UMI_RETRY_CTR_1(UmiwanStdStatusCustomPropertySet.PREFIX + ".retryCtr1", "Retry counter 1"),
    UMI_RETRY_CTR_2(UmiwanStdStatusCustomPropertySet.PREFIX + ".retryCtr2", "Retry counter 2"),
    UMI_RETRY_CTR_3(UmiwanStdStatusCustomPropertySet.PREFIX + ".retryCtr3", "Retry counter 3"),
    UMI_RETRY_CTR_4(UmiwanStdStatusCustomPropertySet.PREFIX + ".retryCtr4", "Retry counter 4"),
    UMI_ERROR_CODE(UmiwanStdStatusCustomPropertySet.PREFIX + ".errorCode", "Error code"),
    UMI_RETRY_TYPE(UmiwanStdStatusCustomPropertySet.PREFIX + ".retryType", "Retry type"),
    UMI_STATUS_FLAG(UmiwanStdStatusCustomPropertySet.PREFIX + ".statusFlag", "Status flag"),
    UMI_EMC_READ_FAIL_CTR(UmiwanStdStatusCustomPropertySet.PREFIX + ".umiEmcReadFailCtr", "Umi EMC read fail counter"),
    UMI_EMC_READ_PASS_CTR(UmiwanStdStatusCustomPropertySet.PREFIX + ".umiEmcReadPassCtr", "Umi EMC read pass counter"),
    UMI_GSM_STD_STATUS_NAME(GsmStdStatusCustomPropertySet.PREFIX + ".gsmStdStatusName", "Gsm STD status"),
    UMI_SUBSCRIBER_ID(GsmStdStatusCustomPropertySet.PREFIX + ".subscriberId", "Subscriber id"),
    UMI_MODEM_MODEL(GsmStdStatusCustomPropertySet.PREFIX + ".modemModel", "Modem model"),
    UMI_MODEM_REVISION(GsmStdStatusCustomPropertySet.PREFIX + ".n", "Modem revision"),
    UMI_MODEM_FIRMWARE(GsmStdStatusCustomPropertySet.PREFIX + ".modemFirmware", "Modem firmware"),
    UMI_MODEM_SERIAL(GsmStdStatusCustomPropertySet.PREFIX + ".modemSerial", "Modem serial"),
    UMI_PROVIDER(GsmStdStatusCustomPropertySet.PREFIX + ".provider", "Provider"),
    UMI_LOCAL_IP_ADDR(GsmStdStatusCustomPropertySet.PREFIX + ".localIpAddr", "Local ip address"),
    UMI_LAST_ERROR_TIME(GsmStdStatusCustomPropertySet.PREFIX + ".lastErrorTime", "Last error time"),
    UMI_LAST_ERROR_MSG(GsmStdStatusCustomPropertySet.PREFIX + ".lastErrorMsg", "Last error message"),
    UMI_RSSI(GsmStdStatusCustomPropertySet.PREFIX + ".rssi", "RSSI"),
    UMI_BER(GsmStdStatusCustomPropertySet.PREFIX + ".ber", "BER"),
    UMI_BATTERY_VOLTAGE(GsmStdStatusCustomPropertySet.PREFIX + ".batteryVoltage", "Battery voltage"),
    UMI_STATUS_FLAGS(GsmStdStatusCustomPropertySet.PREFIX + ".statusFlags", "Status flags"),
    UMI_LAST_ERROR_CODE(GsmStdStatusCustomPropertySet.PREFIX + ".lastErrorCode", "Last error code"),
    UMI_CME_ERROR(GsmStdStatusCustomPropertySet.PREFIX + ".cmeError", "CME error"),
    UMI_LAST_STATE(GsmStdStatusCustomPropertySet.PREFIX + ".lastState", "Last state"),
    UMI_ICCID(GsmStdStatusCustomPropertySet.PREFIX + ".iccid", "ICCID");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
