/*
 * Constants.java
 *
 * Created on 27 oktober 2005, 10:58
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

/**
 *
 * @author Koen
 */
abstract public class AbstractConstants {
    
    static public final int CONSTANTS_GAS_AGA3=0;
    static public final int CONSTANTS_GAS_AGA7=1;
    static public final int CONSTANTS_ELECTRIC=2;
    
    
    abstract protected int getConstantsType();
    
    /** Creates a new instance of Constants */
    public AbstractConstants() {
    }
    
}
