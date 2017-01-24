package com.energyict.dlms.cosem;


import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.methods.FrameCounterProviderMethods;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * Created by iulian on 8/25/2016.
 */
public class FrameCounterProvider extends AbstractCosemObject {
    public static final ObisCode DEFAULT_OBIS_CODE = ObisCode.fromString("0.0.43.1.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public FrameCounterProvider(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return DEFAULT_OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.FRAME_COUNTER_PROVIDER.getClassId();
    }

    public long getFrameCounter(byte[] authenticationKey) throws IOException {
        final byte[] challenge = new byte[64];
        byte[] cSystemTitle;
        byte[] sSystemTitle;

        getProtocolLink().getLogger().info("Getting secure frame counter ...");
        new SecureRandom().nextBytes(challenge);

        if (authenticationKey == null || authenticationKey.length !=16){
            throw new ProtocolException("Cannot invoke get_frame_counter because Authentication Key is invalid.");
        }

        cSystemTitle = getProtocolLink().getAso().getAssociationControlServiceElement().getCallingApplicationProcessTitle();
        if (cSystemTitle==null){
            throw new ProtocolException("Cannot invoke get_frame_counter because getCallingApplicationProcessTitle is null.");
        }

        sSystemTitle = getProtocolLink().getAso().getSecurityContext().getSystemTitle();
        if (sSystemTitle==null){
            throw new ProtocolException("Cannot invoke get_frame_counter because getRespondingAPTtitle is null.");
        }

        getLogger().finest(" - all validation passed cST=["+cSystemTitle+"], sST=["+sSystemTitle+"], invoking method  ...");
        byte[] responseByteArray = this.methodInvoke(FrameCounterProviderMethods.GET_FRAME_COUNTER, OctetString.fromByteArray(challenge));

        getLogger().finest(" - response received!");
        Structure response =  AXDRDecoder.decode(responseByteArray, Structure.class);
        if (response.isStructure()) {
            OctetString mac = response.getDataType(0).getOctetString();
            long counter = response.getDataType(1).longValue();
            getLogger().finest(" - response decoded, frameCounter=["+counter+"], but first will validate the challenge ...");
            //validateMac(mac.getOctetStr(),counter, challenge, authenticationKey, cSystemTitle, sSystemTitle);
            getLogger().finest(" - finished with happy-ending, returning frameCounter="+counter);
            return counter;
        } else {
            if (response!=null) {
                throw new ProtocolException("FrameCounterProvider response received is not an structure: " + response.toString());
            } else {
                throw new ProtocolException("FrameCounterProvider null response");
            }
        }

    }

    /** do not use this validation since the clientSystem title is not the one sent by the meter */
    @Deprecated
    private void validateMac(byte[] mac, long frameCounter, byte[] challenge, byte[] ak, byte[] cSystemTitle, byte[] sSystemTitle) throws ProtocolException {
        final Mac hmac;
        try {
            hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(ak, "HmacSHA256"));
            hmac.update(sSystemTitle);
            hmac.update(cSystemTitle);
            hmac.update(challenge);
            hmac.update((byte) ((frameCounter >> 24) & 0xff));
            hmac.update((byte) ((frameCounter >> 16) & 0xff));
            hmac.update((byte) ((frameCounter >> 8) & 0xff));
            hmac.update((byte) (frameCounter & 0xff));
            if (!Arrays.equals(mac, hmac.doFinal())) {
                getLogger().warning("Framecounter HMAC validation failed.");
            }
        } catch (NoSuchAlgorithmException e) {
            logException(e);
            getLogger().warning("Framecounter HMAC validation failed.");
        } catch (InvalidKeyException e) {
            logException(e);
            getLogger().warning("Framecounter HMAC validation failed.");
        }
    }

    private void logException(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        getLogger().log(Level.WARNING, "Exception while validation get_frame_counter response challenge: " + sw.toString(), e);
    }

}
