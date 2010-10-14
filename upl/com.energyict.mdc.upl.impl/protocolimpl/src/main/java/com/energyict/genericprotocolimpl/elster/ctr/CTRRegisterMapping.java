package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 14-okt-2010
 * Time: 11:44:31
 */
public class CTRRegisterMapping {

    private ObisCode obisCode;
    private CTRObjectID objectId;
    private String description;

    public CTRRegisterMapping(ObisCode obisCode, CTRObjectID objectId, String description) {
        this.description = description;
        this.obisCode = obisCode;
        this.objectId = objectId;
    }

    public CTRRegisterMapping(String obisCode, String objectId, String description) {
        this(ObisCode.fromString(obisCode), new CTRObjectID(objectId), description);
    }

    public CTRRegisterMapping(String obisCode, String objectId) {
        this(ObisCode.fromString(obisCode), new CTRObjectID(objectId));
    }

    public CTRRegisterMapping(ObisCode obisCode, CTRObjectID objectId) {
        this(obisCode, objectId, obisCode.getDescription());
    }

}
