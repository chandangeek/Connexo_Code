/*
 * StateMachineCallBack.java
 *
 * Created on 18 juli 2006, 15:09
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.osiframework;

import java.io.*;

/**
 *
 * @author Koen
 */
public interface StateMachineCallBack {
    
    public int receiving() throws IOException;
    
}
