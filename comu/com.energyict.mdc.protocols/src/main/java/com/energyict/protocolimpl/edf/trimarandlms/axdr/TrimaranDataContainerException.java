package com.energyict.protocolimpl.edf.trimarandlms.axdr;

import java.io.IOException;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: EnergyICT</p>
 *
 * @author Koenraad Vanderschaeve
 * @version 1.0
 */

public class TrimaranDataContainerException extends IOException {

    public TrimaranDataContainerException(String str) {
        super(str);
    }

    public TrimaranDataContainerException() {
        super();
    }

    public TrimaranDataContainerException(String str, short sReason) {
        super(str);
    }

}