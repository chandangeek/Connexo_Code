/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edmi.mk10.registermapping;

import com.energyict.protocolimpl.edmi.common.core.DataType;

/**
 * @author sva
 * @since 8/03/2017 - 10:07
 */
public enum MK10RegisterInformation {

    SYSTEM_MODEL_ID(0xF000, DataType.A_STRING),
    SYSTEM_EQUIPMENT_TYPE(0xF001, DataType.A_STRING),
    SYSTEM_SERIAL_NUMBER(0xF002, DataType.A_STRING),
    SYSTEM_SOFTWARE_VERSION(0xF003, DataType.A_STRING),
    SYSTEM_SOFTWARE_REVISION(0xF090, DataType.L_LONG),
    SYSTEM_BOOTLOADER_REVISION(0xF091, DataType.I_SHORT),
    SYSTEM_EDITION(0xFF00, DataType.L_LONG),

    CT_MULTIPLIER(0xF700, DataType.L_LONG),
    VT_MULTIPLIER(0xF701, DataType.L_LONG),
    CT_DIVISOR(0xF702, DataType.L_LONG),
    VT_DIVISOR(0xF703, DataType.L_LONG),

    NUMBER_OF_BILLING_RESETS(0xF032, DataType.L_LONG),
    LAST_BILLING_RESET_DATE(0x6200, DataType.T_TIME_DATE_SINCE__1_97),
    SECOND_LAST_BILLING_RESET_DATE(0x6400, DataType.T_TIME_DATE_SINCE__1_97),
    SURVEY_BASE_REGISTER(0xD800, DataType.L_LONG),
    TOU_CHANNEL_DEFINITIONS(0xD880, DataType.I_SHORT)

    ;

    private final int registerId;
    private final DataType dataType;

    MK10RegisterInformation(int registerId, DataType dataType) {
        this.registerId = registerId;
        this.dataType = dataType;
    }

    public int getRegisterId() {
        return registerId;
    }

    public DataType getDataType() {
        return dataType;
    }
}