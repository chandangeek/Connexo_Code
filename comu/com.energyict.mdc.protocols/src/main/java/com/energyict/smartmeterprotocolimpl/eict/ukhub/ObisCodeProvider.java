/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.eict.ukhub;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.DLMSClassId;

public class ObisCodeProvider {

    public static int OBJECT_LIST_VERSION  = 1;

    public static final ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    public static final ObisCode FRAUD_DETECTION_EVENT_LOG = ObisCode.fromString("0.0.99.98.1.255");
    public static final ObisCode FIRMWARE_EVENT_LOG = ObisCode.fromString("0.0.99.98.3.255");
    public static final ObisCode COMM_FAILURE_EVENT_LOG = ObisCode.fromString("0.0.99.98.6.255");
    public static final ObisCode HAN_MANAGEMENT_FAILURE_EVENT_LOG = ObisCode.fromString("0.0.99.98.15.255");
    public static final ObisCode MANUFACTURER_SPECIFIC_EVENT_LOG = ObisCode.fromString("0.0.99.98.31.255");
    public static final ObisCode ELSTER_SPECIFIC_EVENT_LOG = ObisCode.fromString("0.0.99.98.20.255");
    public static final ObisCode DEBUG_EVENT_LOG = ObisCode.fromString("0.0.96.128.0.255");

    public static final ObisCode REBOOT_OBISCODE = ObisCode.fromString("0.0.10.0.0.255");

    public static final ObisCode HanManagementEventObject = ObisCode.fromString("0.0.96.11.15.255");

    public static final ObisCode FIRMWARE_UPDATE = ObisCode.fromString("0.0.44.0.0.255");
    public static final ObisCode IMAGE_ACTIVATION_SCHEDULER = ObisCode.fromString("0.0.15.0.2.255");
    public static final ObisCode ZIGBEE_NCP_FIRMWARE_UPDATE = ObisCode.fromString("0.1.44.0.0.255");

    public static final UniversalObject[] OBJECT_LIST;

    static {
        OBJECT_LIST = new UniversalObject[]{
                new UniversalObject(ObisCode.fromString("0.0.96.12.5.255"), DLMSClassId.REGISTER),
                new UniversalObject(ObisCode.fromString("0.0.2.0.0.255"), DLMSClassId.MODEM_CONFIGURATION),
                new UniversalObject(ObisCode.fromString("0.0.2.1.0.255"), DLMSClassId.AUTO_CONNECT),
                new UniversalObject(ObisCode.fromString("0.0.2.2.0.255"), DLMSClassId.AUTO_ANSWER),
                new UniversalObject(ObisCode.fromString("0.0.25.0.0.255"), DLMSClassId.TCP_UDP_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.25.1.0.255"), DLMSClassId.IPV4_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.25.3.0.255"), DLMSClassId.PPP_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.25.4.0.255"), DLMSClassId.GPRS_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.35.5.0.255"), DLMSClassId.ZIGBEE_HAN_MANAGEMENT),
                new UniversalObject(ObisCode.fromString("0.4.0.2.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.5.0.2.0.255"), DLMSClassId.DATA),
                new UniversalObject(HanManagementEventObject, DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.99.97.15.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.93.44.15.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.15.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.41.0.0.255"), DLMSClassId.SAP_ASSIGNMENT),
                new UniversalObject(ObisCode.fromString("0.0.40.0.1.255"), DLMSClassId.ASSOCIATION_LN),
                new UniversalObject(ObisCode.fromString("0.0.40.0.2.255"), DLMSClassId.ASSOCIATION_LN),
                new UniversalObject(ObisCode.fromString("0.0.40.0.3.255"), DLMSClassId.ASSOCIATION_LN),
                new UniversalObject(ObisCode.fromString("0.0.40.0.4.255"), DLMSClassId.ASSOCIATION_LN),
                new UniversalObject(ObisCode.fromString("0.0.40.0.5.255"), DLMSClassId.ASSOCIATION_LN),
                new UniversalObject(ObisCode.fromString("0.0.40.0.0.255"), DLMSClassId.ASSOCIATION_LN),
                new UniversalObject(ObisCode.fromString("0.0.43.0.1.255"), DLMSClassId.SECURITY_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.43.0.2.255"), DLMSClassId.SECURITY_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.43.0.3.255"), DLMSClassId.SECURITY_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.43.0.4.255"), DLMSClassId.SECURITY_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.43.0.5.255"), DLMSClassId.SECURITY_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.43.0.0.255"), DLMSClassId.SECURITY_SETUP),
                new UniversalObject(ObisCode.fromString("0.0.42.0.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.1.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.1.1.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.1.2.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.1.3.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.1.4.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.1.5.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.1.6.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.1.7.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.1.9.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.1.10.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.1.50.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.1.51.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.0.2.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.1.0.2.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.2.0.2.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.3.0.2.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.6.0.2.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.7.0.2.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.8.0.2.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.1.0.0.255"), DLMSClassId.CLOCK),
                new UniversalObject(ObisCode.fromString("1.0.0.9.1.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("1.0.0.9.2.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("1.0.0.9.11.255"), DLMSClassId.REGISTER),
                new UniversalObject(ObisCode.fromString("1.0.0.9.9.255"), DLMSClassId.REGISTER),
                new UniversalObject(ObisCode.fromString("0.0.97.97.0.255"), DLMSClassId.REGISTER),
                new UniversalObject(ObisCode.fromString("0.0.97.98.0.255"), DLMSClassId.REGISTER),
                new UniversalObject(ObisCode.fromString("0.0.97.98.10.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.11.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.99.98.0.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.93.44.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.0.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.11.1.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.99.98.1.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.93.44.1.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.1.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.11.3.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.99.98.3.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.93.44.3.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.3.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.11.6.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.99.97.6.255"), DLMSClassId.PROFILE_GENERIC),
                new UniversalObject(ObisCode.fromString("0.0.93.44.6.255"), DLMSClassId.DATA),
                new UniversalObject(ObisCode.fromString("0.0.96.15.6.255"), DLMSClassId.DATA),
                new UniversalObject(FIRMWARE_UPDATE, DLMSClassId.IMAGE_TRANSFER),
                new UniversalObject(ObisCode.fromString("0.0.15.0.2.255"), DLMSClassId.SINGLE_ACTION_SCHEDULE),
                new UniversalObject(ObisCode.fromString("0.0.10.0.107.255"), DLMSClassId.SCRIPT_TABLE),
                new UniversalObject(ObisCode.fromString("0.0.35.2.0.255"), DLMSClassId.ZIGBEE_SETC_CONTROL),
        };
    }

    public static ObisCode getFrameCounterObisCode(final int backupClientId) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
