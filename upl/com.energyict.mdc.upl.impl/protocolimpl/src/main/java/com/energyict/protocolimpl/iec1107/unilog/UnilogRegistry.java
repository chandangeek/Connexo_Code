/*
 * Created on 17-jan-2005
 *
 */
package com.energyict.protocolimpl.iec1107.unilog;

import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegister;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegisterDataParse;

/**
 * @author fbo
 */

public class UnilogRegistry extends AbstractVDEWRegistry {

    public final String R_DEVICE_ADDRESS = "0.01.1";
    public final String R_METROLOGICAL_REVISION_NUMBER = "0.2.0";
    public final String R_MEASUREMENT_PERIOD_FLOW = "0.8.0";
    public final String R_REGISTRATION_PERIOD_LOAD_PROFILE = "0.8.5";
    public final String R_TIME = "0.9.1";
    public final String R_DATE = "0.9.2";
    public final String R_TIME_DATE = "0.9.1 0.9.2";
    

    /**
     * @param meterExceptionInfo
     * @param protocolLink
     */
    public UnilogRegistry(MeterExceptionInfo meterExceptionInfo,
            ProtocolLink protocolLink) {
        super(meterExceptionInfo, protocolLink);
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry#initRegisters()
     */
    protected void initRegisters() {
        
        VDEWRegister register = new VDEWRegister(R_DEVICE_ADDRESS,
                VDEWRegister.VDEW_STRING, 0, -1, null, VDEWRegister.NOT_WRITEABLE,
                VDEWRegister.CACHED);
        registers.put( R_DEVICE_ADDRESS, register );
        
        register = new VDEWRegister( R_METROLOGICAL_REVISION_NUMBER, 
                VDEWRegister.VDEW_STRING, 0, -1, null, VDEWRegister.NOT_WRITEABLE,
                VDEWRegister.CACHED);
        registers.put( R_METROLOGICAL_REVISION_NUMBER, register );
        
        register = new VDEWRegister( R_MEASUREMENT_PERIOD_FLOW, 
                VDEWRegister.VDEW_INTEGER, 0, -1, null, VDEWRegister.NOT_WRITEABLE,
                VDEWRegister.CACHED);
        registers.put( R_MEASUREMENT_PERIOD_FLOW, register );
        
        register = new VDEWRegister( R_REGISTRATION_PERIOD_LOAD_PROFILE, 
                VDEWRegister.VDEW_INTEGER, 0, -1, null, VDEWRegister.NOT_WRITEABLE,
                VDEWRegister.CACHED);
        registers.put( R_REGISTRATION_PERIOD_LOAD_PROFILE, register );
        
        register = new VDEWRegister( R_TIME, 
                VDEWRegister.VDEW_TIMESTRING, 0, -1, null, VDEWRegister.WRITEABLE,
                VDEWRegister.NOT_CACHED);
        registers.put( R_TIME, register );
        
        register = new VDEWRegister( R_DATE, 
                VDEWRegister.VDEW_DATESTRING, 0, -1, null, VDEWRegister.WRITEABLE,
                VDEWRegister.NOT_CACHED);
        registers.put( R_DATE, register );
        
        register = new VDEWRegister( R_TIME_DATE, 
                VDEWRegisterDataParse.VDEW_TIMEDATE, 0, -1, null, VDEWRegister.NOT_WRITEABLE,
                VDEWRegister.NOT_CACHED );
        registers.put( R_TIME_DATE, register );
        
    }

}