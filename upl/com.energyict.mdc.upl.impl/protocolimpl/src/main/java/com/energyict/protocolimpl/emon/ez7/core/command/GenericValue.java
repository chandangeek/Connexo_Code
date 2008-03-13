/*
 * GenericValue.java
 *
 * Created on 20 mei 2005, 9:14
 */

package com.energyict.protocolimpl.emon.ez7.core.command;


/**
 *
 * @author  Koen
 */
public interface GenericValue {
    public int getValue(int row,int col) throws com.energyict.protocol.UnsupportedException;   
}
