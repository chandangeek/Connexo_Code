package com.energyict.mdc.common;

/**
 * Defines modules as used by SystemParameterSpec
 * Date: 9-dec-2010
 * Time: 13:39:11
 */
public enum ApplicationModule {

    GENERAL(0, "General"),
    /* Clients */
    EIMASTER(1, "eiMaster"),
    EIDESIGNER(2, "eiDesigner"),
    EIPORTAL(3, "eiPortal"),
    EIDASHBOARD(4, "eiDashBoard"),
    /* Other*/
    LEGACY(97, "Legacy"),
    COMSERVER(99, "ComServer"),
    /* Foundation */
    FSECURE(100, "Security"),
    FASSET(101, "fAsset"),
    FALERT(102, "fAlert"),
    FREPORT(101, "Reporting"),
    /* Modules */
    MVALIDATE(1000, "mValidate"),
    MAUTOMATE(1001, "mAutomate"),
    MREPORT(1002, "mReport"),
    MPREBILL(1003, "mPreBill"),
    MFORECAST(1004, "mForecast"),
    MTRACK(1005, "mTrack"),
    /* Headends */
    HXMOBILE(10000, "hxMobile"),

    /* Interfaces */
    IXMDUS(100000, "ixMDUS"),
    CUSTOM(Integer.MAX_VALUE, "Customization");


    private final int ordinal;
    private final String name;

    ApplicationModule(int ordinal, String name) {
        this.ordinal = ordinal;
        this.name = name;
    }
    // Order of appearance in the eiMaster's System Parameter Window

    public int getOrdinal() {
        return ordinal;
    }

    public String getName() {
        return this.name;
    }

}
