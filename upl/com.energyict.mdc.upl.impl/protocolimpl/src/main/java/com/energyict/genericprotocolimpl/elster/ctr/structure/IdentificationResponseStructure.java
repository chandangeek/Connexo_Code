package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 6-okt-2010
 * Time: 15:47:10
 */
public class IdentificationResponseStructure extends Data<IdentificationResponseStructure> {

    private CTRAbstractValue<String> pdr;

    public IdentificationResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                pdr.getBytes()
        ));
    }
    @Override
    public IdentificationResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {
        int ptr = offset;

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType valueAttributeType = new AttributeType();
        valueAttributeType.setHasValueFields(true);

        this.pdr = factory.parse(rawData, ptr, valueAttributeType, "C.0.0").getValue()[0];
        ptr += pdr.getValueLength();

        return this;
    }

    public CTRAbstractValue<String> getPdr() {
        return pdr;
    }
}
