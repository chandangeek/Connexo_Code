package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.ImageTransferAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

/**
 * Created by H165680 on 6/16/2017.
 */
public class ImageTransferMapping extends G3Mapping {

    private ImageTransferAttributesMapping imageTransferAttributesMapping;

    protected ImageTransferMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public ObisCode getBaseObisCode() {                 //Set the B-Filed to 0
        return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 1, (byte) 0);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        instantiateMappers(cosemObjectFactory);
        return readRegister(getObisCode());
    }

    private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
        if (imageTransferAttributesMapping == null) {
            imageTransferAttributesMapping = new ImageTransferAttributesMapping(cosemObjectFactory);
        }
    }

    @Override
    public int getAttributeNumber() {
        return getObisCode().getB();        //The B-field of the obiscode indicates which attribute is being read
    }

    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        instantiateMappers(null);  //Not used here

        if (imageTransferAttributesMapping.canRead(getObisCode())) {
            RegisterValue registerValue = imageTransferAttributesMapping.parse(getObisCode(), abstractDataType);
            if(getObisCode().getB() == 2){
                registerValue.setQuantity(new Quantity(abstractDataType.getUnsigned32().getValue(), Unit.get(BaseUnit.UNITLESS)));
            }
            return registerValue;
        }
        throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
    }

    private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (imageTransferAttributesMapping.canRead(obisCode)) {
            return imageTransferAttributesMapping.readRegister(obisCode);
        }
        throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
    }

    @Override
    public int getDLMSClassId() {
        if(getObisCode().equalsIgnoreBAndEChannel(ImageTransfer.getDefaultObisCode()) ){
            return DLMSClassId.IMAGE_TRANSFER.getClassId();
        } else {
            return -1;
        }
    }
}
