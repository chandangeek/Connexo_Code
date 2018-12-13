package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.*;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 10/12/12
 * Time: 9:44 AM
 */
public class ImageTransferInitiateStructure extends Structure {

    public ImageTransferInitiateStructure(final String imageIdentifier, final int size) {
        super(OctetString.fromString(imageIdentifier), new Unsigned32(size));
    }

    /**
     * @return The image identifier
     */
    public final String getImageIdentifier() {
        try {
            return getDataType(0, OctetString.class).stringValue();
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * @return The image size in bytes
     */
    public final int getImageSize() {
        try {
            return getDataType(1, Unsigned32.class).intValue();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ImageTransferInitiateStructure={");

        sb.append("imageIdentifier='");
        sb.append(getImageIdentifier());
        sb.append("', ");

        sb.append("imageSize=");
        sb.append(getImageSize());

        sb.append('}');
        return sb.toString();
    }

}
