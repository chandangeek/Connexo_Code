/*
 * ConstantsTable.java
 *
 * Created on 26 oktober 2005, 17:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.PartialReadInfo;
import com.energyict.protocolimpl.base.FirmwareVersion;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ConstantsTable extends AbstractTable {

    private AbstractConstants[] constants;

    /** Creates a new instance of ConstantsTable */
    public ConstantsTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(15));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ConstantsTable: \n");
        for (int i=0;i<getConstants().length;i++) {
            strBuff.append("constants["+i+"]="+getConstants()[i]);
        }
        return strBuff.toString();
    }

    protected void prepareBuild() throws IOException {
        // override to provide extra functionality...
        int niFormat1 = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getNonIntFormat1();
        // overrule for GEKV
        if (getTableFactory().getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.ge.kv.GEKV")==0) {
            FirmwareVersion fw = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getManufacturerIdentificationTable().getFirmwareVersion();
            FirmwareVersion fw2CheckAgainst = new FirmwareVersion("5.2");
            if (fw.equal(fw2CheckAgainst) || fw.before(fw2CheckAgainst))
                niFormat1 = C12ParseUtils.FORMAT_INT64;
        }
        int niFormat2 = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getNonIntFormat2();

        ActualSourcesLimitingTable aslt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable();
        int constantsSelector = aslt.getConstantsSelector();

        int size=0;
        switch(constantsSelector) {
            case AbstractConstants.CONSTANTS_GAS_AGA3: {
                size=GasConstantsAGA3.getSize(niFormat2)*aslt.getMaxNrOfEntriesConstants();
            } break; // AbstractConstants.CONSTANTS_GAS_AGA3

            case AbstractConstants.CONSTANTS_GAS_AGA7: {
                size=GasConstantsAGA7.getSize(niFormat2)*aslt.getMaxNrOfEntriesConstants();
            } break; // AbstractConstants.CONSTANTS_GAS_AGA7

            case AbstractConstants.CONSTANTS_ELECTRIC: {
                size=ElectricConstants.getSize(niFormat1, aslt)*aslt.getMaxNrOfEntriesConstants();
            } break; // AbstractConstants.CONSTANTS_ELECTRIC

            case 3: {
                if (getTableFactory().getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.ge.kv.GEKV")==0) {
                    size=ElectricConstants.getSize(niFormat1, aslt)*aslt.getMaxNrOfEntriesConstants();
                }
                else
                    throw new IOException("ConstantsTable, parse, invalid constants selector "+constantsSelector+", cannot continue!");
            } break; //

            default:
                throw new IOException("ConstantsTable, parse, invalid constants selector "+constantsSelector+", cannot continue!");
        } // switch(constantsSelector)


        PartialReadInfo partialReadInfo = new PartialReadInfo(0,size);
        setPartialReadInfo(partialReadInfo);
    }


    protected void parse(byte[] tableData) throws IOException {
        int offset=0;
        int niFormat1 = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getNonIntFormat1();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        // overrule for GEKV
        if (getTableFactory().getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.ge.kv.GEKV")==0) {
            FirmwareVersion fw = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getManufacturerIdentificationTable().getFirmwareVersion();
            FirmwareVersion fw2CheckAgainst = new FirmwareVersion("5.2");
            if (fw.equal(fw2CheckAgainst) || fw.before(fw2CheckAgainst))
                niFormat1 = C12ParseUtils.FORMAT_INT64;
        }

        int niFormat2 = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getNonIntFormat2();
        ActualSourcesLimitingTable aslt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable();
        setConstants(new AbstractConstants[aslt.getMaxNrOfEntriesConstants()]);
        int constantsSelector = aslt.getConstantsSelector();
        for (int i=0;i<getConstants().length;i++) {

            switch(constantsSelector) {
                case AbstractConstants.CONSTANTS_GAS_AGA3: {
                    getConstants()[i] = new GasConstantsAGA3(tableData, offset, niFormat2, dataOrder);
                    offset+=GasConstantsAGA3.getSize(niFormat2);
                } break; // AbstractConstants.CONSTANTS_GAS_AGA3

                case AbstractConstants.CONSTANTS_GAS_AGA7: {
                    getConstants()[i] = new GasConstantsAGA7(tableData, offset, niFormat2, dataOrder);
                    offset+=GasConstantsAGA7.getSize(niFormat2);
                } break; // AbstractConstants.CONSTANTS_GAS_AGA7

                case AbstractConstants.CONSTANTS_ELECTRIC: {
                    getConstants()[i] = new ElectricConstants(tableData, offset, niFormat1, aslt, dataOrder);
                    offset+=ElectricConstants.getSize(niFormat1, aslt);
                } break; // AbstractConstants.CONSTANTS_ELECTRIC

                case 3: {
                    if (getTableFactory().getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.ge.kv.GEKV")==0) {
                        getConstants()[i] = new ElectricConstants(tableData, offset, niFormat1, aslt, dataOrder);
                        offset+=ElectricConstants.getSize(niFormat1, aslt);
                    }
                    else
                        throw new IOException("ConstantsTable, parse, invalid constants selector "+constantsSelector+", cannot continue!");
                } break; //

                default:
                    throw new IOException("ConstantsTable, parse, invalid constants selector "+constantsSelector+", cannot continue!");
            } // switch(constantsSelector)
        } // for (int i=0;i<constants.length;i++)
    } // protected void parse(byte[] tableData) throws IOException

    public AbstractConstants[] getConstants() {
        return constants;
    }

    public void setConstants(AbstractConstants[] constants) {
        this.constants = constants;
    }
} // public class ConstantsTable extends AbstractTable
