/*
 * RangeCoder.java
 *
 * Created on 4 oktober 2007, 10:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import java.math.BigDecimal;

/**
 * 
 * @author kvds
 */
public interface RangeCoder {

	BigDecimal calcMultiplier(int coding);

}
