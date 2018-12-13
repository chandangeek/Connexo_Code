package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.simpleobjectmodel.SimpleImageTransferObject;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * User: heuckeg
 * Date: 20.06.13
 * Time: 15:04
 */
public class Ek280FirmwareUpdateMessage extends FirmwareUpdateMessage
{
    public Ek280FirmwareUpdateMessage(DlmsMessageExecutor messageExecutor)
    {
        super(messageExecutor);
    }

    @Override
    public String getFirmwareIdentifier(byte[] decodedBytes) throws NoSuchAlgorithmException
    {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(decodedBytes, 0, decodedBytes.length);
        String identifier = new BigInteger(1, md5.digest()).toString(16);
        identifier = identifier.substring(16);
        System.out.println("EK280 firmware signature:" + identifier);
        return identifier;
    }

    @Override
    public boolean isSameImage(SimpleImageTransferObject imageTransferObject, int imageSize, String identifier) throws IOException
    {
        return imageTransferObject.isSameImage(identifier, imageSize);
    }
}
