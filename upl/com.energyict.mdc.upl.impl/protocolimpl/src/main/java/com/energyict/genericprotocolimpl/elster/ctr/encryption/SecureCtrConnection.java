package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.genericprotocolimpl.elster.ctr.CtrConnection;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copyrights EnergyICT
 * Date: 5-okt-2010
 * Time: 15:02:43
 */
public class SecureCtrConnection extends CtrConnection {

    private CTREncryption ctrEncryption;


    public SecureCtrConnection(InputStream in, OutputStream out, MTU155Properties propertiess) {
        super(in, out, propertiess);
    }

    @Override
    public GPRSFrame sendFrameGetResponse(GPRSFrame frame) {

        GPRSFrame gprsFrame = super.sendFrameGetResponse(frame);

        return gprsFrame;    //To change body of overridden methods use File | Settings | File Templates.
    }
    
}
