package com.energyict.protocolimpl.edmi.mk10.registermapping;

public final class MK10Register {

    public static final int SYSTEM_MODEL_ID = 0xF000;
    public static final int SYSTEM_EQUIPMENT_TYPE = 0xF001;
    public static final int SYSTEM_SERIALNUMBER = 0xF002;
    public static final int SYSTEM_SOFTWARE_VERSION = 0xF003;
    public static final int SYSTEM_SOFTWARE_REVISION = 0xF090;
    public static final int SYSTEM_BOOTLOADER_REVISION = 0xF091;

    public static final int CT_MULTIPLIER = 0xF700;
    public static final int VT_MULTIPLIER = 0xF701;
    public static final int CT_DIVISOR = 0xF702;
    public static final int VT_DIVISOR = 0xF703;

    public static final int SURVEY1_STARTDATE = 0xD800;
    public static final int TOU_CHANNEL_DEFINITIONS = 0xD880;

    public static final int NUMBER_OF_BILLING_RESETS = 0xF032;
    public static final int BILLING_RESET_TO_DATE = 0x6200;
    public static final int BILLING_RESET_FROM_DATE = 0x6400;
}