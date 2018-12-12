/*
 * LoadProfileDataSet2Table.java
 *
 * Created on 8 november 2005, 11:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocolimpl.ansi.c12.*;
import com.energyict.protocol.*;

/**
 *
 * @author Koen
 */
public class LoadProfileDataSet2Table extends AbstractLoadProfileDataSetTable {
    
    /** Creates a new instance of LoadProfileDataSet2Table */
    public LoadProfileDataSet2Table(StandardTableFactory tableFactory) {
        super(tableFactory,65);
    }
    
    protected LoadProfileSetStatus getLoadProfileSetStatusCached() throws IOException {
        return getTableFactory().getC12ProtocolLink().getStandardTableFactory().getLoadProfileStatusTableCached().getLoadProfileSet1Status();
    }
    
}
