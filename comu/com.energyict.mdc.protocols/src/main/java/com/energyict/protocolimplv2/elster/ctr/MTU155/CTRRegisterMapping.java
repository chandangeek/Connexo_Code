package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

/**
 * Maps a CTR register to an obiscode
 * Copyrights EnergyICT
 * Date: 14-okt-2010
 * Time: 11:44:31
 */
public class CTRRegisterMapping {

    private final ObisCode obisCode;
    private final CTRObjectID objectId;
    private final String description;
    private final int valueIndex;

    public CTRRegisterMapping(String obisCode, String objectId, int valueIndex) {
        this(ObisCode.fromString(obisCode), new CTRObjectID(objectId), "", valueIndex);
    }

    public CTRRegisterMapping(ObisCode obisCode, CTRObjectID objectId, String description, int valueIndex) {
        this.obisCode = obisCode;
        this.objectId = objectId;
        this.description = description;
        this.valueIndex = valueIndex;
    }

    public CTRRegisterMapping(String obisCode) {
        this(ObisCode.fromString(obisCode), null);
    }

    public CTRRegisterMapping(String obisCode, String objectId, String description) {
        this(ObisCode.fromString(obisCode), new CTRObjectID(objectId), description, 0);
    }

    public CTRRegisterMapping(String obisCode, String objectId) {
        this(ObisCode.fromString(obisCode), new CTRObjectID(objectId));
    }

    public CTRRegisterMapping(ObisCode obisCode, CTRObjectID objectId) {
        this(obisCode, objectId, obisCode.getDescription(), 0);
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public int getValueIndex() {
        return valueIndex;
    }

    public CTRObjectID getObjectId() {
        return objectId;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return objectId.toString();
    }

}
