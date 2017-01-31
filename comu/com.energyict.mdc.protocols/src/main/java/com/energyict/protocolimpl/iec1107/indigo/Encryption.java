/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Encryption.java
 *
 * Created on 6 juli 2004, 18:47
 */

package com.energyict.protocolimpl.iec1107.indigo;

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
