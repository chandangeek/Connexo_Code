package com.energyict.genericprotocolimpl.common;


public class RtuMessageConstant {
    
    /** RtuMessage tag for connecting load */
    public final static String CONNECT_LOAD = "connectLoad";
    /** RtuMessage tag for disconnecting load */
    public final static String DISCONNECT_LOAD = "disconnectLoad";
    /** RtuMessage tag to indicate the digital output to apply the connectControl*/
    public final static String DIGITAL_OUTPUT = "Digital_output";
    
    /** RtuMessage tag for reading profile data */
    public final static String READ_PROFILE = "readProfile";

    /** RtuMessage tag for reading data on demand */
    public final static String READ_ON_DEMAND = "onDemand";
    
    /** RtuMessage tag for tou schedule */
    public final static String  TOU_SCHEDULE = "UserFile ID of tariff program";
    
    /** RtuMessage tag for threshold parameters */
    public final static String THRESHOLD_PARAMETERS = "thresholdParameters";
    public final static String THRESHOLD_GROUPID = "Threshold GroupId *";
    public final static String PARAMETER_GROUPID = "Parameter GroupId *";
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
    
    /** RtuMessage tag for changing the repeater mode of a PLC meter */
    public final static String REPEATER_MODE = "repeaterMode";
    
    /** RtuMessage tag for changing the PLC frequency of the meter */
    public final static String CHANGE_PLC_FREQUENCY = "changePLCFreq";
    public final static String FREQUENCY_MARK = "Frequency mark";
    public final static String FREQUENCY_SPACE = "Frequency space";
    
    /** RtuMessage tag for upgrading the meters firmware */
    public final static String FIRMWARE_UPGRADE = "firmwareUpgrade";
    public final static String FIRMWARE = "UserFile ID of firmware bin file";
    public final static String FIRMWARE_METERS = "GroupID of meters to receive new firmware";
    
    /** RtuMessage tag for prepaid functionality */
    public final static String SET_PREPAID = "Set Prepaid credit";
    public final static String ADD_PREPAID = "Add Prepaid credit";
    public final static String READ_PREPAID = "Read Prepaid credit";
    
    /** RtuMessage tags for load limiting */
	public static final String LOAD_LIMIT_ENABLE = "Enable load limiting";
	public static final String LOAD_LIMIT_DISALBE = "Disable load limitng";
	public static final String LOAD_LIMIT_PARAMETERS = "Parameters load limiting";
	public static final String LOAD_LIMIT_READ_FREQUENCY = "Read_frequency";
	public static final String LOAD_LIMIT_THRESHOLD = "Threshold";
	public static final String LOAD_LIMIT_DURATION = "Duration";
    
}
