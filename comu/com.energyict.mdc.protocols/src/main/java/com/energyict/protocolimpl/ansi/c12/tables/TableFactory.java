/*
 * TableFactory.java
 *
 * Created on 18 oktober 2005, 11:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ProtocolLink;

/**
 *
 * @author Koen
 */
abstract public class TableFactory {
    abstract public C12ProtocolLink getC12ProtocolLink();

    public TableFactory() {
    }


}
