/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms;

import com.energyict.mdc.common.Unit;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;

import java.io.IOException;

/**
 * This class represents the ScalerUnit structure as described in the dlms blue book
 * <p/>
 * Created on 31 oktober 2002, 17:04
 *
 * @author koen
 */
public class ScalerUnit {

    /**
     * The scale value: where
     * <pre>
     * actual value = value * (10 ^ scale)
     * </pre>
     */
    private final int scale;

    /**
     * The DLMS unit code id as described in the dlms blue book (Table 3 – Enumerated values for physical units)
     */
    private final int unit;

    /**
     * Construct a ScalerUnit object from a given structure object as described in the dlms blue book
     *
     * @param scalerUnitStructure The structure containing the scaler and unit
     */
    public ScalerUnit(Structure scalerUnitStructure) {
        scale = scalerUnitStructure.getDataType(0).getInteger8().intValue();
        unit = scalerUnitStructure.getDataType(1).getTypeEnum().intValue();
    }

    /**
     * Construct a new ScalerUnit object from a given "AbstractDataType". However, the dataType
     * should always be of the type 'Structure' so it makes no sense at all to accept ALL dataTypes.
     * Thats why this method is deprecated. You should use {@link ScalerUnit#ScalerUnit(com.energyict.dlms.axrdencoding.Structure)}
     *
     * @param dataType The abstract data type that MUST be a Structure.
     * @deprecated You should use {@link ScalerUnit#ScalerUnit(com.energyict.dlms.axrdencoding.Structure)}
     */
    @Deprecated
    public ScalerUnit(AbstractDataType dataType) {
        scale = dataType.getStructure().getDataType(0).getInteger8().intValue();
        unit = dataType.getStructure().getDataType(1).getTypeEnum().intValue();
    }

    /**
     * Create a new 'ScalerUnit' Structure from the current scaler and unit value.
     *
     * @return The new 'ScalerUnit' Structure object
     */
    public Structure getScalerUnitStructure() {
        Structure structure = new Structure();
        structure.addDataType(new Integer8(scale));
        structure.addDataType(new TypeEnum(unit));
        return structure;
    }

    /**
     * Construct a new ScalerUnit object given the scaler and unit in the EIServer Unit object
     * This method should be used carefully because not every EIServer unit has a matching DLMS unit
     *
     * @param unit The EIServer Unit object to construct the ScalerUnit from
     */
    public ScalerUnit(Unit unit) {
        this.unit = unit.getDlmsCode();
        this.scale = unit.getScale();
    }


    /**
     * Construct a new ScalerUnit object given the scaler and the given unit in the EIServer Unit object.
     * The scaler in the EIServer Unit object is ignored, and the given scaler parameter is used.
     * This method should be used carefully because not every EIServer unit has a matching DLMS unit
     *
     * @param scale Scale to construct the ScalerUnit from
     * @param unit  The EIServer unit to construct the ScalerUnit from. Only the BaseUnit is used, the scaler is ignored.
     */
    public ScalerUnit(int scale, Unit unit) {
        this.unit = unit.getDlmsCode();
        this.scale = scale;
    }

    /**
     * Construct a new ScalerUnit object given the scaler and the given dlms unit id
     * This method should be used carefully because not every EIServer unit has a matching DLMS unit.
     * The unit id should only contain a value that's described in the bluebook.
     *
     * @param scale Scale to construct the ScalerUnit from
     * @param unit  The dlms unit id to construct the ScalerUnit from.
     */
    public ScalerUnit(int scale, int unit) {
        byte bScale = (byte) scale;
        this.scale = (int) bScale;
        this.unit = unit;
    }


    /**
     * Construct a new ScalerUnit from raw dlms response data of a scaler unit structure
     *
     * @param buffer The raw byte data af a structure containing the scaler and unit as described in the dlms blue book
     * @throws IOException
     */
    public ScalerUnit(byte[] buffer) throws IOException {
        this(buffer, 0);
    }

    /**
     * /**
     * Construct a new ScalerUnit from raw dlms response data of a scaler unit structure, from a given offset
     *
     * @param buffer The raw byte data af a structure containing the scaler and unit as described in the dlms blue book
     * @param offset The offset of the structure in the raw data
     * @throws IOException
     */
    public ScalerUnit(byte[] buffer, int offset) throws IOException {
        byte bScale = (byte) DLMSUtils.parseValue2long(buffer, offset + 2);
        this.scale = (int) bScale;
        this.unit = (int) DLMSUtils.parseValue2long(buffer, offset + 4) & 0xff;
    }

    /**
     * Getter for the dlms unit code as defined in the dlms blue book
     *
     * @return the dlms unit code
     */
    public int getUnitCode() {
        return unit;
    }

    /**
     * Getter for the scaler as numerical value:
     * <pre>
     * actual value = value * (10 ^ scale)
     * </pre>
     *
     * @return scaler as an int value
     */
    public int getScaler() {
        return scale;
    }

    /**
     * Tries to map the dlms unit 1v1 to the eiserver unit, taking the scaler in account.
     * This could be dangerous, because the dlms unit id is not always the same as the eiserver unit id!
     *
     * @return The unit
     * @deprecated Use the {@link com.energyict.dlms.ScalerUnit#getEisUnit()} method to automatically do the translation.
     */
    @Deprecated
    public Unit getUnit() {
        return Unit.get(unit, scale);
    }

    /**
     * Returns a correct EIServer unit, taking the scaler in account.
     * The original DLMS unit code is translated to the correct EIServer code by using the {@link DlmsUnit} enum
     *
     * @return The EIServer unit
     */
    public Unit getEisUnit() {
        return Unit.get(DlmsUnit.fromValidDlmsCode(getUnitCode()).getEisUnitCode(), scale);
    }

    public String toString() {
        return "scale=" + scale + " unit=" + unit + ", " + getEisUnit();
    }

}
