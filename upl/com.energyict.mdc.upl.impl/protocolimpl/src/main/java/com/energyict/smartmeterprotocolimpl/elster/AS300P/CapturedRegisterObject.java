package com.energyict.smartmeterprotocolimpl.elster.AS300P;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.Register;

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
     * @param obisCode
     * @param serialNumber
     * @param attribute
     * @param classId
     */
    public CapturedRegisterObject(ObisCode obisCode, String serialNumber, int attribute, DLMSClassId classId) {
        this(new DLMSAttribute(obisCode, attribute, classId), serialNumber);
    }

    /**
     * This class identifies a register in an smart meter by its DLMSAttribute and serial number of the (slave)device
     *
     * @param dlmsAttribute
     * @param serialNumber
     */
    public CapturedRegisterObject(DLMSAttribute dlmsAttribute, String serialNumber) {
        super(-1, dlmsAttribute.getObisCode(), serialNumber);
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
