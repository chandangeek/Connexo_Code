package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.mdw.amr.RtuRegister;

/**
 * Provides functionality to create a fullShadow <CODE>RtuRegister</CODE> which will contain necessary information for the protocol
 */
public class RtuRegisterFullProtocolShadowBuilder {

    private RtuRegisterFullProtocolShadowBuilder() {
    }

    /**
     * Creates a <CODE>ChannelFullProtocolShadow</CODE> object
     *
     * @param rtuRegister the <CODE>RtuRegister</CODE> to convert
     * @return a fully build rtuRegister shadow Object
     */
    public static RtuRegisterFullProtocolShadow createRtuRegisterFullProtocolShadow(RtuRegister rtuRegister) {
        RtuRegisterFullProtocolShadow rrfps = new RtuRegisterFullProtocolShadowImpl();
        rrfps.setRegisterObisCode(rtuRegister.getRtuRegisterSpec().getObisCode());
        rrfps.setRegisterUnit(rtuRegister.getUnit());
        rrfps.setRtuRegisterId(rtuRegister.getId());
        return rrfps;
    }
}
