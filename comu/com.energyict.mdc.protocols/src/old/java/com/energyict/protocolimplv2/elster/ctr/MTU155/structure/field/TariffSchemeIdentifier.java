/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class TariffSchemeIdentifier extends AbstractField<TariffSchemeIdentifier> {

    private byte[] tariffSchemeIdentifier;

    public byte[] getBytes() {
        return tariffSchemeIdentifier;
    }

    public byte[] getTariffSchemeIdentifier() {
        return tariffSchemeIdentifier;
    }

    public int getLength() {
        return 2;
    }

    public TariffSchemeIdentifier parse(byte[] rawData, int offset) throws CTRParsingException {
        tariffSchemeIdentifier = ProtocolTools.getSubArray(rawData, offset, offset + getLength());
        return this;
    }

    public void setTariffSchemeIdentifier(byte[] tariffSchemeIdentifier) {
        this.tariffSchemeIdentifier = tariffSchemeIdentifier.clone();
    }
}