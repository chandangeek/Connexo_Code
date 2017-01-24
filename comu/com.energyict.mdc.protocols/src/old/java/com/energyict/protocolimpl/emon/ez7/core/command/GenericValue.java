/*
 * GenericValue.java
 *
 * Created on 20 mei 2005, 9:14
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.mdc.protocol.api.UnsupportedException;

/**
 *
 * @author Koen
 */
public interface GenericValue {

	int getValue(int col, int row) throws UnsupportedException;

}