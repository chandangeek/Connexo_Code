/*
 * ProtocolLink.java
 *
 * Created on 1 december 2006, 13:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.base.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Koen
 */
public interface ProtocolLink {
    public AbstractProtocol getProtocol();
    public ApplicationStateMachine getApplicationStateMachine();
    public MiniDLMSConnection getMiniDLMSConnection();
    public DataDefinitionFactory getDataDefinitionFactory();
}
