package com.energyict.protocolimplv2.eict.rtu3.beacon3100.firmware;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.util.Arrays;

/**
 * This class will provide the methods to be used by the protocol in order to do the firmware signature check
 *
 * Created by H165680 on 3/13/2018.
 */
public class BeaconFirmwareSignatureCheck {

    /** The size of the signature, in bytes. */
    private static final int SIGNATURE_SIZE = 256;


    public static boolean firmwareSignatureCheckSupported() {
        return true;
    }

    public static boolean verifyFirmwareSignature(File firmwareFile, final PublicKey publicKey) throws InvalidKeyException, SignatureException, IOException, NoSuchAlgorithmException {
        final Signature signer = Signature.getInstance("SHA256withECDSA");
        final byte[] buffer = new byte[4096];

        signer.initVerify(publicKey);
        int length = (int) (firmwareFile.length() - SIGNATURE_SIZE);
        int currentBytesRead;
        int readLength = Integer.min(length, 4096);
        int remainingLength = length - readLength;
        try (final FileInputStream stream = new FileInputStream(firmwareFile)) {
            while ((currentBytesRead = stream.read(buffer, 0, readLength)) != -1) {
                signer.update(buffer, 0, currentBytesRead);
                readLength = Integer.min(remainingLength, 4096);
                if (readLength == 0) {
                    break;
                }
                remainingLength -= readLength;
            }
        }

        return signer.verify(getSignature(firmwareFile));

    }

    /**
     * Returns the signature appended to the upgrade file.
     *
     * @return The signature appended to the upgrade file.
     */
    private static byte[] getSignature(File firmwareFile) throws IOException {
        final byte[] signature = new byte[SIGNATURE_SIZE];

        try (final FileInputStream stream = new FileInputStream(firmwareFile)) {
            stream.skip(firmwareFile.length() - SIGNATURE_SIZE);
            stream.read(signature);
            if(signature[0] == 0x30) { //ASN1 SEQUENCE{
                int signatureLength = signature[1] /*length*/ + 2 /*0x30 + length*/;
                return Arrays.copyOf(signature, signatureLength);
            }
        }
        return signature;
    }

//Stuff bellow is just for testing - CONM-240 - begin
//The code below was tested in an IntelliJ stand alone project
   /*import sun.security.ec.ECPublicKeyImpl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.rmi.server.ExportException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

public class PublicKey {
    private static final int SIGNATURE_SIZE = 256;

    public static byte[] getBytesFromHexString(final String hexString, final String prefix) {
        if (hexString == null || hexString.isEmpty()) {
            return new byte[0];
        }

        int prefixLength = (prefix == null) ? 0 : prefix.length();
        int charsPerByte = prefixLength + 2;
        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        for (int i = 0; i < hexString.length(); i += charsPerByte) {
            bb.write(Integer.parseInt(hexString.substring(i + prefixLength, i + charsPerByte), 16));
        }
        return bb.toByteArray();
    }

    public static void main(String[] args) {
        ECPublicKeyImpl ecPublicKey = null;
        File f = null;

        System.out.println("Pornire main");
        try {
            //c:\Lori\2018\05_mai\05_CONM_240\beacon_certificates\Claudiu_firmware-upgrade-v02.00.00.bin
           // f = new File("C:\\protocols to implement\\Salzburg\\firmware-upgrade-v02.00.00.bin");
           //f = new File("C:\\Lori\\2018\\05_mai\\05_CONM_240\\beacon_certificates\\Claudiu_firmware-upgrade-v02.00.00.bin");
            f = new File("c:\\Lori\\2018\\05_mai\\05_CONM_240\\firmware-upgrade-v02.00.00.bin");
            ecPublicKey = new ECPublicKeyImpl(getBytesFromHexString("3059301306072A8648CE3D020106082A8648CE3D0301070342000436DFFF138FE8368B7478A552A42C389B31B24259FF2EA58AE758E8B9DFB057E9BC1FAE63FC6778C94ED49DEA0266D335104455222DE4ACEA9513D751108D9CEA", ""));  //semnatura Claudiu
           // ecPublicKey = new ECPublicKeyImpl(getBytesFromHexString("3059301306072A8648CE3D020106082A8648CE3D030107034200045ADFB0922452DB4FA5DE4266172BA06F0F74D02E3A1F81286609ED0CFEA6565C7CFA216C52E9515023F14505913D72D5574AE1CF8B202457B5A3D4E535712F75","")); //semnatura DOA
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        //verify signature
        try {
            boolean firmwareSignatureOk = verifyFirmwareSignature(f, ecPublicKey);
            System.out.println("firmwareSignatureOk = "+firmwareSignatureOk);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println("OK");
    }

    public static boolean verifyFirmwareSignature(File firmwareFile, final java.security.PublicKey publicKey) throws InvalidKeyException, SignatureException, IOException, NoSuchAlgorithmException {
        final Signature signer = Signature.getInstance("SHA256withECDSA");
        final byte[] buffer = new byte[4096];

        signer.initVerify(publicKey);
        int length = (int) (firmwareFile.length() - SIGNATURE_SIZE);
        int currentBytesRead;
        int readLength = Integer.min(length, 4096);
        int remainingLength = length - readLength;
        try (final FileInputStream stream = new FileInputStream(firmwareFile)) {
            while ((currentBytesRead = stream.read(buffer, 0, readLength)) != -1) {
                signer.update(buffer, 0, currentBytesRead);
                readLength = Integer.min(remainingLength, 4096);
                if (readLength == 0) {
                    break;
                }
                remainingLength -= readLength;
            }
        }

        return signer.verify(getSignature(firmwareFile));

    }

    /**
     * Returns the signature appended to the upgrade file.
     *
     * @return The signature appended to the upgrade file.
     */
 /*   private static byte[] getSignature(File firmwareFile) throws IOException {
        final byte[] signature = new byte[SIGNATURE_SIZE];
        try (final FileInputStream stream = new FileInputStream(firmwareFile)) {
            stream.skip(firmwareFile.length() - SIGNATURE_SIZE);
            stream.read(signature);
            if(signature[0] == 0x30) { //ASN1 SEQUENCE{
                int signatureLegth = signature[1]  + 2 /*0x30 + length*/;
  /*              return Arrays.copyOf(signature, signatureLegth);
            }
        }
        return signature;

    }
}  */
    //Stuff above is just for testing - CONM-240 - end

    //Stuff bellow is just for testing - initial version

/*    private static final int HEX = 16;

    *//**
     * @param hexString
     * @param prefix
     * @return
     *//*
    public static byte[] getBytesFromHexString(final String hexString, final String prefix) {
        if (hexString == null || hexString.isEmpty()) {
            return new byte[0];
        }

        int prefixLength = (prefix == null) ? 0 : prefix.length();
        int charsPerByte = prefixLength + 2;
        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        for (int i = 0; i < hexString.length(); i += charsPerByte) {
            bb.write(Integer.parseInt(hexString.substring(i + prefixLength, i + charsPerByte), HEX));
        }
        return bb.toByteArray();
    }

    public static void main(String[] args) {
        ECPublicKeyImpl ecPublicKey = null;
        File f = null;

        try {
            f = new File("C:\\protocols to implement\\Salzburg\\firmware-upgrade-v02.00.00.bin");
            ecPublicKey = new ECPublicKeyImpl(getBytesFromHexString("3059301306072A8648CE3D020106082A8648CE3D0301070342000436DFFF138FE8368B7478A552A42C389B31B24259FF2EA58AE758E8B9DFB057E9BC1FAE63FC6778C94ED49DEA0266D335104455222DE4ACEA9513D751108D9CEA", ""));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        //verify signature
        try {
            boolean firmwareSignatureOk = verifyFirmwareSignature(f, ecPublicKey);
            System.out.println("firmwareSignatureOk = "+firmwareSignatureOk);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }*/
}
