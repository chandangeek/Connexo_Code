package com.energyict.genericprotocolimpl.elster.ctr.util;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;

/**
 * Copyrights EnergyICT
 * Date: 20-okt-2010
 * Time: 17:23:00
 */
public final class CTRObjectInfo {

    /**
     * Private constructor te prevent instantiation of util class
     */
    private CTRObjectInfo() {

    }

    /**
     *
     * @param objectID
     * @return
     */
    public static String getSymbol(String objectID) {
        CTRObjectFactory factory = new CTRObjectFactory();
        try {
            AbstractCTRObject ctrObject = factory.parse(new byte[1024], 0, AttributeType.getQualifierAndValue(), objectID);
            return ctrObject.getSymbol();
        } catch (CTRParsingException e) {
            return "Unknown";
        }
    }

    /**
     *
     * @param objectID
     * @return
     */
    public static Unit getUnit(String objectID) {
        CTRObjectFactory factory = new CTRObjectFactory();
        try {
            AbstractCTRObject ctrObject = factory.parse(new byte[1024], 0, AttributeType.getQualifierAndValue(), objectID);
            return ctrObject.getUnit(new CTRObjectID(objectID), 0);
        } catch (CTRParsingException e) {
            return Unit.getUndefined();
        }
    }

}
