/*
 * Encryption.java
 *
 * Created on 6 juli 2004, 18:47
 */

package com.energyict.protocolimpl.sampleiec1107.indigo;

import com.energyict.protocolimpl.base.Encryptor;
/**
 *
 * @author  Koen
 */
public class Encryption implements Encryptor {
    
    /** Creates a new instance of Encryption */
    public Encryption() {
    }
    
    public String encrypt(String passWord, String key) {
        return passWord;
    }
    
}
