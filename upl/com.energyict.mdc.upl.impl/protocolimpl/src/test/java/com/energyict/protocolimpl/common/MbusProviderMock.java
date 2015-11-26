package com.energyict.protocolimpl.common;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.webrtu.common.MbusProvider;


public class MbusProviderMock extends MbusProvider{
    /**
     * Constructor
     *
     * @param cof               - the {@link com.energyict.dlms.cosem.CosemObjectFactory} to use
     * @param fixMbusHexShortId - boolean indicating we need to convert the Identification number from hex or from BCD (true is
     */
    public MbusProviderMock(CosemObjectFactory cof, boolean fixMbusHexShortId) {
        super(cof, fixMbusHexShortId);
    }

    /**
     * Construct the shortId from the four given fields
     * @param manufacturer - the manufacturer ID of the meter
     * @param identification - the identification number(serialnumber) of the meter
     * @param version - the version of the device type
     * @param deviceType - the device type
     * @return a string which is a concatenation of the manipulated given fields
     */
    public String constructShortId(Unsigned16 manufacturer, Unsigned32 identification, Unsigned8 version, Unsigned8 deviceType){
        return super.constructShortId(manufacturer, identification, version, deviceType);
    }
}
