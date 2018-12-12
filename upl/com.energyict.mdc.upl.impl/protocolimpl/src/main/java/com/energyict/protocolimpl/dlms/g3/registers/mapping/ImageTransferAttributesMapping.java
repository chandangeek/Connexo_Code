package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * Created by H165680 on 6/15/2017.
 */
public class ImageTransferAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 2;
    private static final int MAX_ATTR = 6;

    public ImageTransferAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return ImageTransfer.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer(obisCode);
        return parse(obisCode, readAttribute(obisCode, imageTransfer));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, ImageTransfer imageTransfer) throws IOException {
        switch (obisCode.getB()) {
            case 2:
                return imageTransfer.readImageBlockSize();
            case 3:
                return imageTransfer.readImageTransferedBlockStatus();
            case 4:
                return imageTransfer.readFirstNotTransferedBlockNumber();
            case 5:
                return imageTransfer.readImageTransferEnabledState();
            case 6:
                return imageTransfer.getImageTransferStatus();
            default:
                throw new NoSuchRegisterException("Image transfer attribute [" + obisCode.getB() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getB()) {

            case 2:
                return new RegisterValue(obisCode, "Image block size: " + abstractDataType.getUnsigned32().getValue());
            case 3:
                return new RegisterValue(obisCode, "Image transferred block status: " + abstractDataType.getBitString().toBigDecimal());
            case 4:
                return new RegisterValue(obisCode, "Image first not transferred block number: " + abstractDataType.getUnsigned32().getValue());
            case 5:
                return new RegisterValue(obisCode, "Image transfer enabled: " + abstractDataType.getBooleanObject().getState());
            case 6:
                return new RegisterValue(obisCode, "Image transfer status: " + abstractDataType.getTypeEnum().getValue());
            default:
                throw new NoSuchRegisterException("Image transfer attribute [" + obisCode.getB() + "] not supported!");

        }
    }

}
