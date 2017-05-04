package com.energyict.dlms.aso;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.exceptions.DataEncryptionException;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.upl.io.ConnectionCommunicationException;

import com.energyict.dlms.DLMSConnectionException;

import java.io.IOException;

/**
 * Helper class to assist/support the encryption/decryption of request/response(s) using SecurityContext object.<br/>
 * This helper should only be used for V2 protocols. Basically, all declared exceptions, thrown by the underlying SecurityContext methods,
 * will be wrapped into corresponding ComServerRuntimeExceptions.
 *
 * @author sva
 * @since 6/05/2015 - 11:06
 */
public class SecurityContextV2EncryptionHandler {

    /**
     * Service specific encryption (global or dedicated ciphering)
     */
    public static byte[] dataTransportEncryption(SecurityContext securityContext, byte[] plainText) {
        try {
            return securityContext.dataTransportEncryption(plainText);
        } catch (UnsupportedException e) {             //Unsupported security policy
            throw DeviceConfigurationException.unsupportedPropertyValue("dataTransportSecurityLevel", String.valueOf(securityContext.getSecurityPolicy().getDataTransportSecurityLevel()));
        }
    }

    /**
     * Service specific decryption (global or dedicated ciphering)
     */
    public static byte[] dataTransportDecryption(SecurityContext securityContext, byte[] cipherFrame) throws DLMSConnectionException {
        try {
            return securityContext.dataTransportDecryption(cipherFrame);
        } catch (ConnectionException e) {              //Failed to decrypt data
            throw DataEncryptionException.dataEncryptionException(e);
        } catch (UnsupportedException e) {             //Unsupported security policy
            throw DeviceConfigurationException.unsupportedPropertyValue("dataTransportSecurityLevel", String.valueOf(securityContext.getSecurityPolicy().getDataTransportSecurityLevel()));
        } catch (ProtocolException e) {
            throw ConnectionCommunicationException.unexpectedIOException(e);
        }
    }

    /**
     * General-global or general-dedicated encryption.
     */
    public static byte[] dataTransportGeneralGloOrDedEncryption(SecurityContext securityContext, byte[] request) {
        try {
            return securityContext.dataTransportGeneralGloOrDedEncryption(request);
        } catch (UnsupportedException e) {  //Unsupported security policy
            throw DeviceConfigurationException.unsupportedPropertyValue("dataTransportSecurityLevel", String.valueOf(securityContext.getSecurityPolicy().getDataTransportSecurityLevel()));
        } catch (IOException e) {           // Error while writing to the stream
            throw ConnectionCommunicationException.unexpectedIOException(e);
        }
    }

    /**
     * General-global or general-dedicated decryption.
     */
    public static byte[] dataTransportGeneralGloOrDedDecryption(SecurityContext securityContext, byte[] securedResponse) throws DLMSConnectionException {
        try {
            return securityContext.dataTransportGeneralGloOrDedDecryption(securedResponse);
        } catch (ConnectionException e) {              //Failed to decrypt data
            throw DataEncryptionException.dataEncryptionException(e);
        } catch (UnsupportedException e) {             //Unsupported security policy
            throw DeviceConfigurationException.unsupportedPropertyValue("dataTransportSecurityLevel", String.valueOf(securityContext.getSecurityPolicy().getDataTransportSecurityLevel()));
        } catch (ProtocolException e) {
            throw ConnectionCommunicationException.unexpectedIOException(e);
        }
    }

    /**
     * General-ciphering encryption
     */
    public static byte[] dataTransportGeneralEncryption(SecurityContext securityContext, byte[] request) {
        try {
            return securityContext.dataTransportGeneralEncryption(request);
        } catch (UnsupportedException e) {  //Unsupported security policy
            throw DeviceConfigurationException.unsupportedPropertyValue("dataTransportSecurityLevel", String.valueOf(securityContext.getSecurityPolicy().getDataTransportSecurityLevel()));
        } catch (IOException e) {           // Error while writing to the stream
            throw ConnectionCommunicationException.unexpectedIOException(e);
        }
    }

    /**
     * General-ciphering decryption
     */
    public static byte[] dataTransportGeneralDecryption(SecurityContext securityContext, byte[] securedResponse) throws DLMSConnectionException {
        try {
            return securityContext.dataTransportGeneralDecryption(securedResponse);
        } catch (ConnectionException e) {              //Failed to decrypt data
            throw DataEncryptionException.dataEncryptionException(e);
        } catch (UnsupportedException e) {             //Unsupported security policy
            throw DeviceConfigurationException.unsupportedPropertyValue("dataTransportSecurityLevel", String.valueOf(securityContext.getSecurityPolicy().getDataTransportSecurityLevel()));
        } catch (ProtocolException e) {
            throw ConnectionCommunicationException.unexpectedIOException(e);
        }
    }

    public static byte[] applyGeneralSigning(SecurityContext securityContext, byte[] securedRequest) {
        try {
            return securityContext.applyGeneralSigning(securedRequest);
        } catch (UnsupportedException e) {  //Unsupported security policy
            throw DeviceConfigurationException.unsupportedPropertyValue("dataTransportSecurityLevel", String.valueOf(securityContext.getSecurityPolicy().getDataTransportSecurityLevel()));
        }
    }

    public static byte[] unwrapGeneralSigning(SecurityContext securityContext, byte[] securedRequest) {
        try {
            return securityContext.unwrapGeneralSigning(securedRequest);
        } catch (UnsupportedException e) {  //Unsupported security policy
            throw DeviceConfigurationException.unsupportedPropertyValue("dataTransportSecurityLevel", String.valueOf(securityContext.getSecurityPolicy().getDataTransportSecurityLevel()));
        } catch (IOException e) {           // Error while writing to the stream
            throw ConnectionCommunicationException.unexpectedIOException(e);
        }
    }
}