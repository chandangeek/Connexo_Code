package com.energyict.protocolimplv2.elster.ctr.MTU155.util;

import com.energyict.cbo.Unit;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

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
     * Gets the symbol from a given object
     * @param objectID: the ID of the object
     * @return the symbol of the object
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
     * Gets the unit from a given object
     * @param objectID: the ID of the object
     * @return the unit of the object
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
