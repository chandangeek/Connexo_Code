package com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles;

import com.energyict.mdc.protocol.api.device.data.Register;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 20/07/11
 * Time: 11:15
 */
public class CapturedRegisterObject extends Register {

    private final DLMSAttribute dlmsAttribute;

    /**
     * This class identifies a register in an smart meter by its obis code and serial number of the (slave)device
     * It also contains the attribute that is captured and the DLMS class id
     *
     * @param obisCode     the ObisCode for this Captured Register
     * @param serialNumber the serialNumber of the device
     * @param attribute    the DLMS attribute for this Captured Register
     * @param classId      the ClassId of this Captured Register
     */
    public CapturedRegisterObject(ObisCode obisCode, String serialNumber, int attribute, DLMSClassId classId) {
        this(new DLMSAttribute(obisCode, attribute, classId), serialNumber);
    }

    /**
     * This class identifies a register in an smart meter by its DLMSAttribute and serial number of the (slave)device
     *
     * @param dlmsAttribute the dlms Attribute for this capturedRegister
     * @param serialNumber  the serialNumber of the device
     */
    public CapturedRegisterObject(DLMSAttribute dlmsAttribute, String serialNumber) {
        this(dlmsAttribute, serialNumber, -1);
    }

    /**
     * @param dlmsAttribute the dlms Attribute for this capturedRegister
     * @param serialNumber  the serialNumber of the device
     * @param rtuRegisterId the rtuRegisterId (if not present, set to -1)
     */
    public CapturedRegisterObject(DLMSAttribute dlmsAttribute, String serialNumber, int rtuRegisterId) {
        super(rtuRegisterId, dlmsAttribute.getObisCode(), serialNumber);
        this.dlmsAttribute = dlmsAttribute;
    }

    public int getAttribute() {
        return this.dlmsAttribute.getAttribute();
    }

    public DLMSAttribute getDlmsAttribute() {
        return dlmsAttribute;
    }

    public DLMSClassId getDLMSClassId() {
        return getDlmsAttribute().getDLMSClassId();
    }

    public int getClassId() {
        return getDlmsAttribute().getDLMSClassId().getClassId();
    }

}
