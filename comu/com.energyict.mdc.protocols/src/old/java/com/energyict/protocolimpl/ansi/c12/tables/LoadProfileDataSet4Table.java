/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LoadProfileDataSet4Table.java
 *
 * Created on 8 november 2005, 11:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class LoadProfileDataSet4Table extends AbstractLoadProfileDataSetTable {

    /** Creates a new instance of LoadProfileDataSet4Table */
    public LoadProfileDataSet4Table(StandardTableFactory tableFactory) {
        super(tableFactory,67);
    }

    protected LoadProfileSetStatus getLoadProfileSetStatusCached() throws IOException {
        return getTableFactory().getC12ProtocolLink().getStandardTableFactory().getLoadProfileStatusTableCached().getLoadProfileSet1Status();
    }

}
