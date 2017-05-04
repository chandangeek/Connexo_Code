/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.protocolimplv2.elster.ctr.MTU155.encryption.CTREncryption;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRConnectionException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.Frame;

public interface CtrConnection<F extends Frame> {

    F sendFrameGetResponse(F frame) throws CTRConnectionException;

    CTREncryption getCTREncryption();

}
