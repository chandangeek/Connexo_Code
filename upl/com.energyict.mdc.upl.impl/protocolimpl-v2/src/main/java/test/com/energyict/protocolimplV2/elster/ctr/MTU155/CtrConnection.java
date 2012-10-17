package test.com.energyict.protocolimplV2.elster.ctr.MTU155;

import test.com.energyict.protocolimplV2.elster.ctr.MTU155.encryption.CTREncryption;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.exception.CTRConnectionException;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.frame.Frame;

/**
 * Copyrights EnergyICT
 * Date: 13-okt-2010
 * Time: 10:27:24
 */
public interface CtrConnection<F extends Frame> {

    F sendFrameGetResponse(F frame) throws CTRConnectionException;

    CTREncryption getCTREncryption();

}
