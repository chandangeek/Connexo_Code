/*
 * FeatureId.java
 *
 * Created on 13 december 2006, 15:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures;

import com.energyict.protocolimpl.itron.quantum1000.minidlms.RemoteProcedureCallFactory;

/**
 *
 * @author Koen
 */
abstract public class FeatureId extends AbstractRemoteProcedure {

    public static final int FID_PROTOCOL_DNP30 = 1;
    public static final int FID_PROTOCOL_IEC870_5_102 = 2;
    public static final int FID_EXTENDED_LOAD_PROFILE_1 = 3;
    public static final int FID_EXTENDED_LOAD_PROFILE_2 = 4;
    public static final int FID_EXTENDED_LOAD_PROFILE_3 = 5;
    public static final int FID_EXTENDED_LOAD_PROFILE_4 = 6;
    public static final int FID_EXTENDED_LOAD_PROFILE_5 = 7;
    public static final int FID_EXTENDED_LOAD_PROFILE_6 = 8;
    public static final int FID_EXTENDED_LOAD_PROFILE_7 = 9;
    public static final int FID_EXTENDED_LOAD_PROFILE_8 = 10;
    public static final int FID_TIME_OF_USE = 11;
    public static final int FID_SECOND_DIGITAL_STATE_OUTPUT = 12;
    public static final int FID_SECOND_DIGITAL_STATE_INPUT = 13;
    public static final int FID_SECOND_PULSE_OUTPUT = 14;
    public static final int FID_SECOND_PULSE_INPUT = 15;
    public static final int FID_SECOND_ANALOG_OUTPUT = 16;
    public static final int FID_SECOND_ANALOG_INPUT = 17;
    public static final int FID_STANDARD_LOAD_PROFILE_2 = 18;
    public static final int FID_HARMONICS = 19;
    public static final int FID_VOLTAGE_QUALITY = 20;
    public static final int FID_SYSTEM_LOSS_COMPENSATION = 21;
    public static final int FID_PROTOCOL_TRIMARAN_PLUS = 22;
    public static final int FID_PROTOCOL_GPS = 23;
    public static final int FID_METER_INPUTS_AND_OUTPUTS = 24;
    public static final int FID_EXTENDED_EVENTLOGS = 25;
    public static final int FID_PROTOCOL_MODBUS = 26;
    public static final int FID_CANADA = 27;
    public static final int FID_TIME_OF_USE_ADVANCED = 28;
    public static final int FID_SPAIN_DIST = 29;
    public static final int FID_SPAIN_GEN = 30;

    /** Creates a new instance of FeatureId */
    public FeatureId(RemoteProcedureCallFactory remoteProcedureCallFactory) {
        super(remoteProcedureCallFactory);
    }

}
