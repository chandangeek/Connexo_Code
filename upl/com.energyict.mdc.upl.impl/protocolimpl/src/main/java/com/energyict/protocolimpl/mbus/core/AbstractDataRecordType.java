/*
 * AbstractDataRecordType.java
 *
 * Created on 3 oktober 2007, 11:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import java.io.*;

/**
 *
 * @author kvds
 */
abstract public class AbstractDataRecordType {
    
    abstract protected void doParse(byte[] data) throws IOException;  
    
    /** Creates a new instance of AbstractDataRecordType */
    public AbstractDataRecordType() {
    }
    
    public void parse(byte[] data) throws IOException {
        doParse(data);
    }
    
}
