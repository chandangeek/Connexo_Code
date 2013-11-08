/*
 * AbstractCommand.java
 *
 * Created on 17 mei 2005, 16:19
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import java.io.*;
import java.util.*;
import java.text.*;

import com.energyict.cbo.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.emon.ez7.core.*;
import com.energyict.dialer.connection.ConnectionException;
/**
 *
 * @author  Koen
 */
abstract public class AbstractCommand implements GenericValue {
    
    abstract public void build() throws ConnectionException, IOException ;
    
    EZ7CommandFactory ez7CommandFactory=null;
    
    /** Creates a new instance of AbstractCommand */
    public AbstractCommand(EZ7CommandFactory ez7CommandFactory) {
        this.ez7CommandFactory=ez7CommandFactory;
    }
    
    /**
     * Getter for property ez7CommandFactory.
     * @return Value of property ez7CommandFactory.
     */
    public com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory getEz7CommandFactory() {
        return ez7CommandFactory;
    }
    
    // GenericValue interface method.
    // Used within the ObisCodeMapper...
    public int getValue(int row,int col) throws UnsupportedException {
        throw new UnsupportedException();
    }
}
