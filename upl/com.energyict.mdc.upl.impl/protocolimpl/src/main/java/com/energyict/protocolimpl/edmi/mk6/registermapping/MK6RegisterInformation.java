/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edmi.mk6.registermapping;

import com.energyict.protocolimpl.edmi.common.core.DataType;

/**
 * @author sva
 * @since 8/03/2017 - 10:07
 */
public enum MK6RegisterInformation {

    SYSTEM_MODEL_ID(0xF000, DataType.A_STRING),
    SYSTEM_EQUIPMENT_TYPE(0xF001, DataType.A_STRING),
    SYSTEM_SERIAL_NUMBER(0xF002, DataType.G_STRING_OR_LONG),
    SYSTEM_SOFTWARE_VERSION(0xF003, DataType.A_STRING),
    SYSTEM_LAST_VERSION_NUMBER(0xFC18, DataType.A_STRING),
    SYSTEM_LAST_REVISION_NUMBER(0xFC19, DataType.L_LONG),
    SYSTEM_SOFTWARE_REVISION(0xF090, DataType.L_LONG),

    CT_MULTIPLIER(0xF700, DataType.F_FLOAT),
    VT_MULTIPLIER(0xF701, DataType.F_FLOAT),
    CT_DIVISOR(0xF702, DataType.F_FLOAT),
    VT_DIVISOR(0xF703, DataType.F_FLOAT),


    NUMBER_OF_BILLING_RESETS(0xF032, DataType.L_LONG),
    LAST_BILLING_RESET_DATE(0xFC00, DataType.T_TIME_DATE_SINCE__1_97),
    SECOND_LAST_BILLING_RESET_DATE(0xFC01, DataType.T_TIME_DATE_SINCE__1_97),

    EXTENSION_NAME(0x00020000, DataType.A_STRING),
    REGISTER_ID_OF_EXTENSION(0x00021000, DataType.H_HEX_SHORT),
    NUMBER_OF_LOADED_EXTENSIONS(0x0002F001, DataType.I_SHORT),


    LOAD_SURVEY_NUMBER_OF_CHANNELS(0x5F012, DataType.C_BYTE),
    LOAD_SURVEY_NUMBER_OF_ENTRIES(0x5F013, DataType.L_LONG),
    LOAD_SURVEY_INTERVAL_IN_SECONDS(0x5F014, DataType.L_LONG),
    LOAD_SURVEY_LOAD_SURVEY_ENTRY_WIDTH(0x5F018, DataType.I_SHORT),
    LOAD_SURVEY_START_TIME(0x5F020, DataType.T_TIME_DATE_SINCE__1_97),
    LOAD_SURVEY_NUMBER_OF_STORED_ENTRIES(0x5F021, DataType.L_LONG),

    LOAD_SURVEY_CHANNEL_REGISTER_ID(0x5E000, DataType.X_HEX_LONG),
    LOAD_SURVEY_CHANNEL_SIZE(0x5E100, DataType.I_SHORT),
    LOAD_SURVEY_CHANNEL_TYPE(0x5E200, DataType.C_BYTE),
    LOAD_SURVEY_CHANNEL_UNIT(0x5E300, DataType.C_BYTE),
    LOAD_SURVEY_CHANNEL_NAME(0x5E400, DataType.A_STRING),
    LOAD_SURVEY_CHANNEL_RECORD_OFFSET(0x5E500, DataType.I_SHORT),
    LOAD_SURVEY_CHANNEL_SCALING(0x5E600, DataType.C_BYTE),
    LOAD_SURVEY_CHANNEL_SCALING_FACTOR(0x5E800, DataType.F_FLOAT),
    LOAD_SURVEY_FILE_ACCESS_POINT(0x5F008, DataType.OTHER);

    private final int registerId;
    private final DataType dataType;

    MK6RegisterInformation(int registerId, DataType dataType) {
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