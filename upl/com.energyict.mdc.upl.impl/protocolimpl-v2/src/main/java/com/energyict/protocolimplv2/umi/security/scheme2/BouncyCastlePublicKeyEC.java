package com.energyict.protocolimplv2.umi.security.scheme2;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.math.ec.ECCurve;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.spec.ECFieldFp;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

public class BouncyCastlePublicKeyEC implements ECPublicKey, java.security.interfaces.ECPublicKey {

    /** Byte value indicating the start of an uncompressed Point data array */
    private static final byte  UNCOMPRESSED_POINT_TAG = 0x04;

    private byte[] rawPublicKey;
    private org.bouncycastle.math.ec.ECPoint pointQ;
    private ECPoint       pointW;


    public BouncyCastlePublicKeyEC(ECPublicKey pubKeyEC) {
        this(pubKeyEC.getQ().getEncoded());
    }

    public BouncyCastlePublicKeyEC(byte[] data) {
        rawPublicKey = data;
        pointQ = getQ(rawPublicKey);
        pointW = getW(rawPublicKey);
    }

    @Override
    public String getAlgorithm() {
        return "sha256withECDSA";
    }

    @Override
    public String getFormat() {
        return "UMI";
    }

    @Override
    public byte[] getEncoded() {
        return getDEREncoded();
    }

    @Override
    public org.bouncycastle.math.ec.ECPoint getQ() {
        return pointQ;
    }

    @Override
    public ECPoint getW() {
        return pointW;
    }

    @Override
    public org.bouncycastle.jce.spec.ECParameterSpec getParameters() {
        final X9ECParameters ecParameters = SECNamedCurves.getByName("secp256r1");
        return new org.bouncycastle.jce.spec.ECParameterSpec(
                ecParameters.getCurve(),
                ecParameters.getG(), // G
                ecParameters.getN(), // n
                ecParameters.getH()); // h
    }

    @Override
    public java.security.spec.ECParameterSpec getParams()
    {
        final X9ECParameters ecParameters = SECNamedCurves.getByName("secp256r1");

        final EllipticCurve curve = new EllipticCurve(
                // ECField2m ?
                new ECFieldFp(((ECCurve.Fp)ecParameters.getCurve()).getQ()), // q
                ecParameters.getCurve().getA().toBigInteger(),  // a
                ecParameters.getCurve().getB().toBigInteger()); // b

        return new java.security.spec.ECParameterSpec(
                curve,
                ECPointUtil.decodePoint(curve, ecParameters.getG().getEncoded()), // G
                ecParameters.getN(), // n
                ecParameters.getH().intValue()); // h
    }

    /**
     * Decodes an uncompressed ECPoint. First byte must be 0x04, otherwise
     * IllegalArgumentException is thrown.
     * @param data
     * @return
     */
    public static ECPoint decodePoint(byte[] data) {
        if( data[0] != UNCOMPRESSED_POINT_TAG ){
            throw new IllegalArgumentException("First byte must be 0x" + UNCOMPRESSED_POINT_TAG);
        }
        byte[] xEnc = new byte[(data.length - 1) / 2];
        byte[] yEnc = new byte[(data.length - 1) / 2];

        System.arraycopy(data, 1, xEnc, 0, xEnc.length);
        System.arraycopy(data, xEnc.length + 1, yEnc, 0, yEnc.length);

        return new ECPoint(new BigInteger(1, xEnc), new BigInteger(1, yEnc));
    }

    public static org.bouncycastle.math.ec.ECPoint getQ(byte[] rawPublicKey) {
        final X9ECParameters ecParameters = SECNamedCurves.getByName("secp256r1");
        return ecParameters.getCurve().decodePoint(rawPublicKey);
    }

    public static ECPoint getW(byte[] rawPublicKey) {
        return decodePoint(rawPublicKey);
    }

    public static byte[] encodeLength(final int lenValue){
        byte lenBytes = 0;
        if( lenValue>0x7F ){
            // Assume that one byte is sufficient for representing the length
            lenBytes = 1;
            if( lenValue>0xFF ) {
                // No, two bytes is required (assuming that the length is always <= 65535)
                lenBytes = 2;
            }
        }
        final ByteBuffer bb = ByteBuffer.allocate(1 + lenBytes);
        if( lenBytes==0 ){
            // One byte is enough - write the length value directly
            bb.put(0, (byte)lenValue);
        }
        else {
            // First write down how many bytes the length value requires.
            // This is done by setting the MSB + bitmap representing the actual length
            bb.put(0, (byte)(0x80 + lenBytes));
            if( lenBytes==1 ) {
                bb.put(1, (byte)lenValue);
            }
            else {
                bb.putShort(1, (short)lenValue);
            }
        }
        return bb.array();
    }

    public byte[] getDEREncoded() {
        return rawPublicKey;
    }
}
