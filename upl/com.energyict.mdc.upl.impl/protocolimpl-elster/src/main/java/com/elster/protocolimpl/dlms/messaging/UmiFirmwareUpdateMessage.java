package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.classes.class18.ImageToActivateInfo;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleImageTransferObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * User: heuckeg
 * Date: 20.06.13
 * Time: 15:22
 */
public class UmiFirmwareUpdateMessage extends FirmwareUpdateMessage

{
    public UmiFirmwareUpdateMessage(DlmsMessageExecutor messageExecutor)
    {
        super(messageExecutor);
    }

    @Override
    public String getFirmwareIdentifier(byte[] decodedBytes) throws NoSuchAlgorithmException
    {
        int v1 = (int)decodedBytes[26] & 0xFF;
        int v2 = (int)decodedBytes[25] & 0xFF;
        int v3 = (int)decodedBytes[24] & 0xFF;
        String identifier = String.format("%d.%d.%d", v1, v2, v3);
        System.out.println("Umi firmware signature:" + identifier);
        return identifier;
    }

    @Override
    public boolean isSameImage(SimpleImageTransferObject imageTransferObject, int imageSize, String identifier) throws IOException
    {
        boolean result = false;
        ImageToActivateInfo[] info = imageTransferObject.getImageToActivateInfo();
        if (info.length > 0)
        {
            byte[] sig = info[0].getImageIdentification();

            String ident = "";
            for (byte b: sig)
            {
                if (ident.length() > 0)
                {
                    ident += ".";
                }
                ident = ident + ((int)b & 0xFF);
            }
            System.out.println("ident=<" + ident + ">");

            result = (info[0].getImageSize() == imageSize) && (identifier.equals(ident));
        }

        return result;
    }

}
