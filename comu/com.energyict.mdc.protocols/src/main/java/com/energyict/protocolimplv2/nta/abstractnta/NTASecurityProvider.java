/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.nta.abstractnta;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocols.naming.SecurityPropertySpecName;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.aso.framecounter.DefaultRespondingFrameCounterHandler;
import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;
import com.energyict.dlms.protocolimplv2.SecurityProvider;

import java.io.IOException;
import java.security.SecureRandom;

/**
 * Default implementation of the V2 SecurityProvider.
 * Provides all the securityKeys, just for LOCAL purpose
 * Functionality is implemented according to the NTA specification.
 * <p/>
 * The RespondingFrameCounterHandler is the default one which will not verify or check the received frameCounter.
 * <p/>
 * Subclasses can modify this behavior.
 *
 * @author khe
 */
public class NTASecurityProvider implements SecurityProvider {

    protected final int authenticationLevel;
    protected byte[] cTOs;
    protected byte[] dedicatedKey;
    protected TypedProperties properties;
    protected byte[] masterKey;
    protected byte[] hlsSecret;
    private byte[] authenticationKey;
    private byte[] encryptionKey;
    private RespondingFrameCounterHandler respondingFrameCounterHandler = new DefaultRespondingFrameCounterHandler();

    private Long initialFrameCounter;

    /**
     * Create a new instance of NTASecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public NTASecurityProvider(TypedProperties properties, int authenticationLevel) {
        this.properties = properties;
        this.authenticationLevel = authenticationLevel;
    }

    /**
     * Return the dataTransport authenticationKey
     */
    public byte[] getAuthenticationKey() {
        if (this.authenticationKey == null) {
            String hex = properties.<HexString>getTypedProperty(SecurityPropertySpecName.AUTHENTICATION_KEY.getKey()).getContent();
            this.authenticationKey = DLMSUtils.hexStringToByteArray(hex);
        }
        return this.authenticationKey;
    }

    protected void setAuthenticationKey(byte[] authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

    /**
     * The global encryption key
     */
    public byte[] getGlobalKey() {
        if (this.encryptionKey == null) {
            String hex = properties.<HexString>getTypedProperty(SecurityPropertySpecName.ENCRYPTION_KEY.getKey()).getContent();
            this.encryptionKey = DLMSUtils.hexStringToByteArray(hex);
        }
        return this.encryptionKey;
    }

    /**
     * The HLSSecret is the password of the device
     *
     * @return the password of the device
     */
    public byte[] getHLSSecret() {
        if (this.hlsSecret == null) {
            String passwordString = properties.<Password>getTypedProperty(SecurityPropertySpecName.PASSWORD.getKey()).getValue();
            byte[] passwordBytes = new byte[passwordString.length()];
            for (int i = 0; i < passwordString.length(); i++) {
                passwordBytes[i] = (byte) passwordString.charAt(i);
            }
            hlsSecret = passwordBytes;
        }
        return this.hlsSecret;
    }

    @Override
    public byte[] getMasterKey() {
        if (this.masterKey == null) {
            String hex = properties.<HexString>getTypedProperty(SecurityPropertySpecName.MASTER_KEY.getKey()).getContent();
            this.masterKey = DLMSUtils.hexStringToByteArray(hex);
        }
        return this.masterKey;
    }

    protected void setEncryptionKey(byte[] encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    protected void setHlsSecret(byte[] hlsSecret) {
        this.hlsSecret = hlsSecret;
    }

    public byte[] getCallingAuthenticationValue() throws UnsupportedException {
        switch (this.authenticationLevel) {
            case 0:
                return new byte[0];
            case 1: {
                return getHLSSecret();
            }
            case 2:
                throw new UnsupportedException("SecurityLevel 2 is not implemented.");
            case 3: {    // this is a ClientToServer challenge for MD5
                generateClientToServerChallenge();
                return this.cTOs;
            }
            case 4: {    // this is a ClientToServer challenge for SHA-1
                generateClientToServerChallenge();
                return this.cTOs;
            }
            case 5: {    // this is a ClientToServer challenge for GMAC
                generateClientToServerChallenge();
                return this.cTOs;
            }
            case 6: {    // this is a ClientToServer challenge for SHA-256
                generateClientToServerChallenge();
                return this.cTOs;
            }
            case 7: {    // this is a ClientToServer challenge for ECDSA, it requires a challenge length between 32 and 64 bytes
                generateClientToServerChallenge(32);
                return this.cTOs;
            }
            default:
                return new byte[0];
        }
    }

    /**
     * Generate a random challenge of 16 bytes long
     */
    protected void generateClientToServerChallenge() {
        this.generateClientToServerChallenge(16);
    }

    protected void generateClientToServerChallenge(int length) {
        if (this.cTOs == null) {
            SecureRandom generator = new SecureRandom();
            this.cTOs = new byte[length];
            generator.nextBytes(this.cTOs);
        }
    }

    /**
     * Construct the content of the responseValue when a Manufacturer Specific encryption algorithm ({@link com.energyict.dlms.aso.AuthenticationTypes#MAN_SPECIFIC_LEVEL}) is applied.
     *
     * @param respondingAuthenticationValue the response value from the meter OR null
     * @return the encrypted Value to send back to the meter
     */
    public byte[] associationEncryptionByManufacturer(final byte[] respondingAuthenticationValue) throws IOException {
        throw new UnsupportedException("High level security 2 is not supported.");
    }

    /**
     * @return the initial frameCounter
     */
    public long getInitialFrameCounter() {
        if (initialFrameCounter != null) {
            return initialFrameCounter;
        } else {
            SecureRandom generator = new SecureRandom();
            return generator.nextLong();
        }
    }

    public void setInitialFrameCounter(long frameCounter) {
        this.initialFrameCounter = frameCounter;
    }

    /**
     * Provide the handler for the receiving frameCounter
     *
     * @param respondingFrameCounterHandler the object which will handle the received frameCounter
     */
    public void setRespondingFrameCounterHandling(final RespondingFrameCounterHandler respondingFrameCounterHandler) {
        this.respondingFrameCounterHandler = respondingFrameCounterHandler;
    }

    /**
     * @return the used handler for the responding frameCounter
     */
    public RespondingFrameCounterHandler getRespondingFrameCounterHandler() {
        return this.respondingFrameCounterHandler;
    }

    public void changeEncryptionKey(byte[] newEncryptionKey) throws IOException {
        this.encryptionKey = newEncryptionKey;
    }

    public void changeAuthenticationKey(byte[] newAuthenticationKey) throws IOException {
        this.authenticationKey = newAuthenticationKey;
    }

    public byte[] getDedicatedKey() {
        if (dedicatedKey == null) {
            dedicatedKey = new byte[16];
            SecureRandom rnd = new SecureRandom();
            rnd.nextBytes(dedicatedKey);
        }
        return dedicatedKey;
    }
}
