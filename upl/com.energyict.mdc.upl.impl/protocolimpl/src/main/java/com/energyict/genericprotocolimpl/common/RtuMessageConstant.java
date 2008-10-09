package com.energyict.genericprotocolimpl.common;


public class RtuMessageConstant {
    
    /** RtuMessage tag for connecting load */
    public final static String CONNECT_LOAD = "connectLoad";
    /** RtuMessage tag for disconnecting load */
    public final static String DISCONNECT_LOAD = "disconnectLoad";
    
    /** RtuMessage tag for reading profile data */
    public final static String READ_PROFILE = "readProfile";

    /** RtuMessage tag for reading data on demand */
    public final static String READ_ON_DEMAND = "onDemand";
    
    /** RtuMessage tag for tou schedule */
    public final static String  TOU_SCHEDULE = "UserFile ID of tariff program";
    
    /** RtuMessage tag for threshold parameters */
    public final static String THRESHOLD_PARAMETERS = "thresholdParameters";
    public final static String THRESHOLD_GROUPID = "Threshold GroupId *";
    public final static String THRESHOLD_POWERLIMIT = "Threshold PowerLimit (W)";
    public final static String CONTRACT_POWERLIMIT = "Contractual PowerLimit (W)";
    public final static String APPLY_THRESHOLD	= "Apply threshold";
    public final static String CLEAR_THRESHOLD	= "Clear threshold - groupID";
    public final static String THRESHOLD_STARTDT = "StartDate (dd/mm/yyyy HH:MM:SS)";
    public final static String THRESHOLD_STOPDT = "EndDate (dd/mm/yyyy HH:MM:SS)";
    
    /** RtuMessage tag for connecting load */
    public final static String LOAD_CONTROL_ON = "loadControlOn";
    /** RtuMessage tag for disconnecting load */
    public final static String LOAD_CONTROL_OFF = "loadControlOff";
    
}
