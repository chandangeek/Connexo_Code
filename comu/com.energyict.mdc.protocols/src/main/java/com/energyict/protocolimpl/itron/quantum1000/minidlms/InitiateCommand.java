/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InitiateCommand.java
 *
 * Created on 4 december 2006, 14:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

/**
 *
 * @author Koen
 */
public class InitiateCommand extends AbstractCommand {
    
    
    private int dedicatedKey; // 0x00 from the trace file
    private int proposedVersionNr; // 8 bit, 0x31 in trace file
    private int proposedConformance; // 0x000F in trace file 
                             // bit 0 read conformance 
                             // bit 1 write conformance
                             // bit 2 unconfirmed write
                             // bit 3 information report
    private int proposedMaxPDUSize; // 0x01A4 in trace file
    
    /** Creates a new instance of InitiateCommand */
    public InitiateCommand(CommandFactory commandFactory) {
        super(commandFactory);
        setProposedVersionNr(0x31);
        setProposedConformance(0x000F);
        setProposedMaxPDUSize(0x01A4);
        setDedicatedKey(0x00);
    }
    
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("InitiateCommand:\n");
        strBuff.append("   dedicatedKey="+dedicatedKey+"\n");
        strBuff.append("   proposedVersionNr=0x"+Integer.toHexString(proposedVersionNr)+"\n");
        strBuff.append("   proposedConformance=0x"+Integer.toHexString(proposedConformance)+"\n");
        strBuff.append("   proposedMaxPDUSize=0x"+Integer.toHexString(proposedMaxPDUSize)+"\n");
        return strBuff.toString();
    }
    
    protected byte[] prepareInvoke() {
        
        byte[] data = new byte[8];
        data[0]=(byte)getId();
        data[1]=(byte)getDedicatedKey();
        data[2]=(byte)0x01; // response allowed
        data[3]=(byte)getProposedVersionNr();
        data[4]=(byte)(getProposedConformance()>>8);
        data[5]=(byte)(getProposedConformance());
        data[6]=(byte)(getProposedMaxPDUSize()>>8);
        data[7]=(byte)(getProposedMaxPDUSize());
        
        return data;
    }
    
    protected int getId() {
        return 0x01;
    }

    public int getDedicatedKey() {
        return dedicatedKey;
    }

    public void setDedicatedKey(int dedicatedKey) {
        this.dedicatedKey = dedicatedKey;
    }

    public int getProposedVersionNr() {
        return proposedVersionNr;
    }

    public void setProposedVersionNr(int proposedVersionNr) {
        this.proposedVersionNr = proposedVersionNr;
    }

    public int getProposedConformance() {
        return proposedConformance;
    }

    public void setProposedConformance(int proposedConformance) {
        this.proposedConformance = proposedConformance;
    }

    public int getProposedMaxPDUSize() {
        return proposedMaxPDUSize;
    }

    public void setProposedMaxPDUSize(int proposedMaxPDUSize) {
        this.proposedMaxPDUSize = proposedMaxPDUSize;
    }
}
