/*
 * Parser.java
 *
 * Created on 3 april 2007, 9:46
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core;

import java.io.*;

/**
 *
 * @author Koen
 */
public interface Parser {
    public Object val(int[] values, AbstractRegister register) throws IOException;
}
