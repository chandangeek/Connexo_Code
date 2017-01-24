package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the StartDate field in a CTR Structure Object - used in firmware upgrade process
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
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