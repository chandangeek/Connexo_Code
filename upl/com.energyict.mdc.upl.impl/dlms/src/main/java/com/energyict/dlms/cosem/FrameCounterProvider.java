package com.energyict.dlms.cosem;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Level;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.methods.FrameCounterProviderMethods;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Created by iulian on 8/25/2016.
 */
public final class FrameCounterProvider extends AbstractCosemObject {
	
	/** The default OBIS code. */
    private static final ObisCode DEFAULT_OBIS_CODE = ObisCode.fromString("0.0.43.1.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     * @param	meterSystemTitle		The system title of the meter.
     * @param	ourSystemTitle			Our system title.
     * 
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

    public final long getFrameCounter(byte[] authenticationKey) throws IOException {
        getProtocolLink().getLogger().info("Getting secure frame counter ...");
        
        final byte[] challenge = new byte[64];
        new SecureRandom().nextBytes(challenge);

        if (authenticationKey == null || authenticationKey.length !=16){
            throw new ProtocolException("Cannot invoke get_frame_counter because Authentication Key is invalid.");
        }

        final byte[] clientSystemTitle = this.getProtocolLink().getAso().getAssociationControlServiceElement().getCallingApplicationProcessTitle();
        final byte[] serverSystemTitle = this.getProtocolLink().getAso().getAssociationControlServiceElement().getRespondingAPTtitle();
        
        if (clientSystemTitle == null){
            throw new ProtocolException("Cannot invoke get_frame_counter because getCallingApplicationProcessTitle is null.");
        } 
        
        if (serverSystemTitle == null){
            throw new ProtocolException("Cannot invoke get_frame_counter because getRespondingAPTtitle is null.");
        }

        getLogger().finest(" - all validation passed cST=["+clientSystemTitle+"], sST=["+serverSystemTitle+"], invoking method  ...");
        byte[] responseByteArray = this.methodInvoke(FrameCounterProviderMethods.GET_FRAME_COUNTER, OctetString.fromByteArray(challenge));

        getLogger().finest(" - response received!");
        Structure response =  AXDRDecoder.decode(responseByteArray, Structure.class);
        if (response != null && response.isStructure()) {
            OctetString mac = response.getDataType(0).getOctetString();
            long counter = response.getDataType(1).longValue();
            getLogger().finest(" - response decoded, frameCounter=["+counter+"], but first will validate the challenge ...");
            
            this.validateMac(mac.getOctetStr(),counter, challenge, authenticationKey, clientSystemTitle, serverSystemTitle);
            
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


    private final void validateMac(byte[] mac, long frameCounter, byte[] challenge, byte[] ak, byte[] cSystemTitle, byte[] sSystemTitle) throws ProtocolException {
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
            
            final byte[] expectedChallengeResponse = hmac.doFinal();
            
            if (!Arrays.equals(mac, expectedChallengeResponse)) {
                getLogger().warning("Framecounter HMAC validation failed, received MAC [" + ProtocolTools.getHexStringFromBytes(mac, "") + "] while expecting [" + ProtocolTools.getHexStringFromBytes(expectedChallengeResponse, "") + "]");
                
                throw new ProtocolException("Framecounter HMAC validation failed, received MAC [" + ProtocolTools.getHexStringFromBytes(mac, "") + "] while expecting [" + ProtocolTools.getHexStringFromBytes(expectedChallengeResponse, "") + "]");
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
