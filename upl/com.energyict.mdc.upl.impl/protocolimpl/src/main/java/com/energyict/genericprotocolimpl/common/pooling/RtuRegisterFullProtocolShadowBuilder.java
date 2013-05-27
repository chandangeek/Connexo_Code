package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.mdw.amr.Register;

/**
 * Provides functionality to create a fullShadow <CODE>Register</CODE> which will contain necessary information for the protocol
 */
public class RtuRegisterFullProtocolShadowBuilder {

    private RtuRegisterFullProtocolShadowBuilder() {
    }

    /**
     * Creates a <CODE>ChannelFullProtocolShadow</CODE> object
     *
     * @param rtuRegister the <CODE>Register</CODE> to convert
     * @return a fully build rtuRegister shadow Object
     */
    public static RtuRegisterFullProtocolShadow createRtuRegisterFullProtocolShadow(Register rtuRegister) {
        RtuRegisterFullProtocolShadow rrfps = new RtuRegisterFullProtocolShadowImpl();
        rrfps.setRegisterObisCode(rtuRegister.getRegisterSpec().getDeviceObisCode());
        rrfps.setRegisterUnit(rtuRegister.getUnit());
        rrfps.setRtuRegisterId(rtuRegister.getId());
        return rrfps;
    }
}
