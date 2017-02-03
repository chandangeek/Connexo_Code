/*
 * DemandControlTable.java
 *
 * Created on 26 oktober 2005, 15:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DemandControlTable extends AbstractTable {

    
    private IntervalControl[] intervalControl;
    private int resetExclusion; // 8 bit Number of minutes after demand reset to exclude additional reset action
    private int powerfailRecognitionTime; // 8 bit Number of seconds after a powerfailure to valid powerfailure
    private int powerfailExclusion; // 8 bit Number of minutes after a powerfailure to inhibit demand calculations
    private int coldLoadPickup; // 8 bit Number of minutes after a powerfailure to provide cold load pickup function
    
    
    /** Creates a new instance of DemandControlTable */
    public DemandControlTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(13));
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DemandControlTable:\n");
        strBuff.append("    resetExclusion="+getResetExclusion()+", powerfailRecognitionTime="+getPowerfailRecognitionTime()+", powerfailExclusion="+getPowerfailExclusion()+", coldLoadPickup="+getColdLoadPickup()+"\n");
        for(int i=0;i<getIntervalControl().length;i++) {
            strBuff.append("    intervalControl["+i+"]="+getIntervalControl()[i]);
        }
        strBuff.append("\n");
        return  strBuff.toString();
    }
    
    protected void parse(byte[] tableData) throws IOException {
        ActualSourcesLimitingTable aslt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable();
        int offset=0;
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        
        if (aslt.isResetExcludeFlag()) {
            setResetExclusion(C12ParseUtils.getInt(tableData,offset));
            offset++;
        }
        
        if ((getTableFactory().getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.ge.kv.GEKV")==0) || 
            (aslt.isPowerfailExcludeFlag())) {
            setPowerfailRecognitionTime(C12ParseUtils.getInt(tableData,offset));
            offset++;
            setPowerfailExclusion(C12ParseUtils.getInt(tableData,offset));
            offset++;
            setColdLoadPickup(C12ParseUtils.getInt(tableData,offset));       
            offset++;
        }
        
        setIntervalControl(new IntervalControl[aslt.getMaxNrOfEntriesDemandControl()]);
        for(int i=0;i<getIntervalControl().length;i++) {
            getIntervalControl()[i] = new IntervalControl(ProtocolUtils.getSubArray2(tableData,offset,IntervalControl.SIZE), aslt.isSlidingDemand(),dataOrder);
            offset+=IntervalControl.SIZE;
        }
    }

    public IntervalControl[] getIntervalControl() {
        return intervalControl;
    }

    public void setIntervalControl(IntervalControl[] intervalControl) {
        this.intervalControl = intervalControl;
    }

    public int getResetExclusion() {
        return resetExclusion;
    }

    public void setResetExclusion(int resetExclusion) {
        this.resetExclusion = resetExclusion;
    }

    public int getPowerfailRecognitionTime() {
        return powerfailRecognitionTime;
    }

    public void setPowerfailRecognitionTime(int powerfailRecognitionTime) {
        this.powerfailRecognitionTime = powerfailRecognitionTime;
    }

    public int getPowerfailExclusion() {
        return powerfailExclusion;
    }

    public void setPowerfailExclusion(int powerfailExclusion) {
        this.powerfailExclusion = powerfailExclusion;
    }

    public int getColdLoadPickup() {
        return coldLoadPickup;
    }

    public void setColdLoadPickup(int coldLoadPickup) {
        this.coldLoadPickup = coldLoadPickup;
    }
}
