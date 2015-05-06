package com.energyict.dlms.aso;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimplv2.MdcManager;

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

    public static byte[] dataTransportEncryption(SecurityContext securityContext, byte[] plainText) {
        try {
            return securityContext.dataTransportEncryption(plainText);
        } catch (UnsupportedException e) {             //Unsupported security policy
            throw MdcManager.getComServerExceptionFactory().createUnsupportedPropertyValueException("dataTransportSecurityLevel", String.valueOf(securityContext.getSecurityPolicy()));
        }
    }

    public static byte[] dataTransportGeneralEncryption(SecurityContext securityContext, byte[] request) {
           try {
               return securityContext.dataTransportGeneralEncryption(request);
           } catch (UnsupportedException e) {  //Unsupported security policy
               throw MdcManager.getComServerExceptionFactory().createUnsupportedPropertyValueException("dataTransportSecurityLevel", String.valueOf(securityContext.getSecurityPolicy()));
           } catch (IOException e) {           // Error while writing to the stream
               throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(new NestedIOException(e));
           }
       }

    public static byte[] dataTransportDecryption(SecurityContext securityContext, byte[] cipherFrame) {
        try {
            return securityContext.dataTransportDecryption(cipherFrame);
        } catch (ConnectionException e) {              //Failed to decrypt data
            throw MdcManager.getComServerExceptionFactory().createDataEncryptionException();
        } catch (DLMSConnectionException e) {          //Invalid frame counter
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(new NestedIOException(e));
        } catch (UnsupportedException e) {             //Unsupported security policy
            throw MdcManager.getComServerExceptionFactory().createUnsupportedPropertyValueException("dataTransportSecurityLevel", String.valueOf(securityContext.getSecurityPolicy()));
        }
    }

    public static byte[] dataTransportGeneralDecryption(SecurityContext securityContext, byte[] securedResponse) {
        try {
            return securityContext.dataTransportGeneralDecryption(securedResponse);
        } catch (ConnectionException e) {              //Failed to decrypt data
            throw MdcManager.getComServerExceptionFactory().createDataEncryptionException(e);
        } catch (DLMSConnectionException e) {          //Invalid frame counter
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(new NestedIOException(e));
        } catch (UnsupportedException e) {             //Unsupported security policy
            throw MdcManager.getComServerExceptionFactory().createUnsupportedPropertyValueException("dataTransportSecurityLevel", String.valueOf(securityContext.getSecurityPolicy()));
        }
    }
}