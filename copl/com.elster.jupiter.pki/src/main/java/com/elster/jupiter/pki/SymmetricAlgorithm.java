package com.elster.jupiter.pki;

/**
 * The SymmetricAlgorithm represents a mapping of an identifier from the XMLSEC shipment file to a Java algorithm and key length.
 * Used by the secure shipment importer.
 */
public interface SymmetricAlgorithm {


    /**
     * The Cipher name, eg "AES/CBC/PKCS5PADDING"
     */
    public String getCipherName();

    /**
     * XMLSEC symmetric algorithm identifier, eg. "http://www.w3.org/2001/04/xmlenc#aes256-cbc"
     */
    public String getIdentifier();

    public int getKeyLength();
}
