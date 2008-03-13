/*
 * HandleMessageException.java
 *
 * Created on 21 december 2007, 13:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g;

import java.io.*;

/**
 *
 * @author kvds
 */
public class HandleMessageException extends IOException {
    
    /** Creates a new instance of HandleMessageException */
    public HandleMessageException(String message) {
        super(message);
    }
    
}
