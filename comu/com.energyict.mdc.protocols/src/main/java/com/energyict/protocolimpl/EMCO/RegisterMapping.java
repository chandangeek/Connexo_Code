/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.EMCO;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

public class RegisterMapping {

    private final ObisCode obisCode;
    private final int objectId;
    private final Unit unit;
    private final int unitRegisterId;
    private final String description;

    /*
     * Constructor for registers with a fixed unit.
     *
     * @param obisCodeString    ObisCode
     * @param objectId          S-FP93 communication register object ID
     * @param type              the type of the register value
     * @param unit              the unit of the register value
     * @param description       A string description
     */
    public RegisterMapping(String obisCodeString, int objectId, Unit unit, String description) {
        this.obisCode = ObisCode.fromString(obisCodeString);
        this.objectId = objectId;
        this.unit = unit;
        this.unitRegisterId = 0;
        this.description = description;
    }

    /*
     * Constructor for registers who have no fixed unit.
     * E.g.: Temperature registers can be expressed in units deg F, deg R, deg C or Kelvin.
     * A special 'unit register' (E.g.: 'temperature units') contains the unit in use.
     *
     * @param obisCodeString    ObisCode
     * @param objectId          S-FP93 communication register object ID
     * @param type              the type of the register value
     * @param unitRegisterId    the ID of the unit register
     * @param description       A string description
     */
    public RegisterMapping(String obisCodeString, int objectId, int unitRegisterId, String description) {
        this.obisCode = ObisCode.fromString(obisCodeString);
        this.objectId = objectId;
        this.unit = null;
        this.unitRegisterId = unitRegisterId;
        this.description = description;
    }

     /*
     * Constructor for registers who have no unit.
     * ( E.g. used for registers who contain a string value. )
     *
     * @param obisCodeString    ObisCode
     * @param objectId          S-FP93 communication register object ID
     * @param type              the type of the register value
     * @param description       A string description
     */
    public RegisterMapping(String obisCodeString, int objectId, String description) {
        this.obisCode = ObisCode.fromString(obisCodeString);
        this.objectId = objectId;
        this.unit = null;
        this.unitRegisterId = 0;
        this.description = description;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public int getObjectId() {
        return objectId;
    }

    public Unit getUnit() {
        return unit;
    }

    public int getUnitRegisterId() {
        return unitRegisterId;
    }

    public String getDescription() {
        return description;
    }
}