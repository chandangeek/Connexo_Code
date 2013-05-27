package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.*;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 10/12/12
 * Time: 9:44 AM
 */
public class ImageBlockTransferStructure extends Structure {

    public ImageBlockTransferStructure(final int imageBlockNumber, final byte[] imageBlockValue) {
        super(new Unsigned32(imageBlockNumber), OctetString.fromByteArray(imageBlockValue));
    }

    /**
     * @return The image block number
     */
    public final int getImageBlockNumber() {
        try {
            return getDataType(0, Unsigned32.class).intValue();
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * @return The image block content bytes
     */
    public final byte[] getImageBlockValue() {
        try {
            return getDataType(1, OctetString.class).getOctetStr();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ImageBlockTransferStructure={");

        sb.append("imageBlockNumber='");
        sb.append(getImageBlockNumber());
        sb.append("', ");

        sb.append("imageBlockValue=");
        sb.append(DLMSUtils.getHexStringFromBytes(getImageBlockValue(), ""));

        sb.append('}');
        return sb.toString();
    }

}
