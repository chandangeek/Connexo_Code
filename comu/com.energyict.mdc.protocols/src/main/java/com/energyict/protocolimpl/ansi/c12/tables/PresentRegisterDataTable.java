/*
 * PresentRegisterTable.java
 *
 * Created on 28 oktober 2005, 17:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.base.FirmwareVersion;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class PresentRegisterDataTable extends AbstractTable {

    private PresentDemand[] presentDemands;
    private Number[] presentValues;

    /** Creates a new instance of PresentRegisterTable */
    public PresentRegisterDataTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(28));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PresentRegisterDataTable: \n");
        for (int i=0;i<getPresentDemands().length;i++) {
            strBuff.append("    presentDemands["+i+"]="+getPresentDemands()[i]+"\n");
        }
        for (int i=0;i<getPresentValues().length;i++) {
            strBuff.append("    presentValues["+i+"]="+getPresentValues()[i]+"\n");
        }

        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {

        int offset=0;
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        boolean saveflag=getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable().isTimeRemainingFlag();
        setPresentDemands(new PresentDemand[getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable().getNrOfPresentDemands()]);
        for (int i=0;i<getPresentDemands().length;i++) {
            getPresentDemands()[i] = new PresentDemand(tableData, offset, getTableFactory());
            offset+=PresentDemand.getSize(tableFactory);

            if ((tableFactory.getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.ge.kv.GEKV")==0) &&
                (i==0)) {
                FirmwareVersion fw = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getManufacturerIdentificationTable().getFirmwareVersion();
                FirmwareVersion fw2CheckAgainst = new FirmwareVersion("5.2");
                if (fw.equal(fw2CheckAgainst) || fw.before(fw2CheckAgainst))
                    getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable().setTimeRemainingFlag(false);
            }
        }

        getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable().setTimeRemainingFlag(saveflag);
        setPresentValues(new Number[getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable().getNrOfPresentValues()]);
        for (int i=0;i<getPresentValues().length;i++) {
            getPresentValues()[i] = C12ParseUtils.getNumberFromNonInteger(tableData, offset, getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getNonIntFormat1(),dataOrder);
            offset+=C12ParseUtils.getNonIntegerSize(getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getNonIntFormat1());
        }
    }

    public PresentDemand[] getPresentDemands() {
        return presentDemands;
    }

    public void setPresentDemands(PresentDemand[] presentDemands) {
        this.presentDemands = presentDemands;
    }

    public Number[] getPresentValues() {
        return presentValues;
    }

    public void setPresentValues(Number[] presentValues) {
        this.presentValues = presentValues;
    }
}
