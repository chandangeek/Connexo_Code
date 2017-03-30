/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * PresentRegisterSelectionTable.java
 *
 * Created on 28 oktober 2005, 17:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class PresentRegisterSelectionTable extends AbstractTable {

    private int[] presentDemandSelect;
    private int[] presentValueSelect;

    /** Creates a new instance of PresentRegisterSelectionTable */
    public PresentRegisterSelectionTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(27));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PresentRegisterSelectionTable: \n");
        for (int i=0;i<getPresentDemandSelect().length;i++)
            strBuff.append("    presentDemandSelect["+i+"]="+getPresentDemandSelect()[i]+"\n");
        for (int i=0;i<getPresentValueSelect().length;i++)
            strBuff.append("    presentValueSelect["+i+"]="+getPresentValueSelect()[i]+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int offset=0;
        setPresentDemandSelect(new int[getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable().getNrOfPresentDemands()]);
        for (int i=0;i<getPresentDemandSelect().length;i++) {
            getPresentDemandSelect()[i]=C12ParseUtils.getInt(tableData,offset);
            offset++;
        }

        setPresentValueSelect(new int[getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable().getNrOfPresentValues()]);
        for (int i=0;i<getPresentValueSelect().length;i++) {
            getPresentValueSelect()[i]=C12ParseUtils.getInt(tableData,offset);
            offset++;
        }

    }

    public int[] getPresentDemandSelect() {
        return presentDemandSelect;
    }

    public void setPresentDemandSelect(int[] presentDemandSelect) {
        this.presentDemandSelect = presentDemandSelect;
    }

    public int[] getPresentValueSelect() {
        return presentValueSelect;
    }

    public void setPresentValueSelect(int[] presentValueSelect) {
        this.presentValueSelect = presentValueSelect;
    }
}
