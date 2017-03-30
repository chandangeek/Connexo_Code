/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DimensionRegisterTable.java
 *
 * Created on 27 oktober 2005, 15:58
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
public class DimensionRegisterTable extends AbstractTable {

    private int registerFunction1Bitfield;
    private int registerFunction2Bitfield;
    private int nrOfSelfReads;
    private int nrOfSummations;
    private int nrOfDemands;
    private int nrOfCoinValues;
    private int nrOfOccur;
    private int nrOfTiers;
    private int nrOfPresentDemands;
    private int nrOfPresentValues;


    /** Creates a new instance of DimensionRegisterTable */
    public DimensionRegisterTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(20));
    }

    public String toString() {
        return "DimensionRegisterTable: registerFunction1Bitfield=0x"+Integer.toHexString(getRegisterFunction1Bitfield())+
               ", registerFunction2Bitfield=0x"+Integer.toHexString(getRegisterFunction2Bitfield())+
               ", nrOfSelfReads="+getNrOfSelfReads()+
               ", nrOfSummations="+getNrOfSummations()+
               ", nrOfDemands="+getNrOfDemands()+
               ", nrOfCoinValues="+getNrOfCoinValues()+
               ", nrOfOccur="+getNrOfOccur()+
               ", nrOfTiers="+getNrOfTiers()+
               ", nrOfPresentDemands="+getNrOfPresentDemands()+
               ", nrOfPresentValues="+getNrOfPresentValues()+"\n";

    }

    protected void parse(byte[] tableData) throws IOException {
        registerFunction1Bitfield=C12ParseUtils.getInt(tableData,0);
        registerFunction2Bitfield=C12ParseUtils.getInt(tableData,1);
        nrOfSelfReads=C12ParseUtils.getInt(tableData,2);
        nrOfSummations=C12ParseUtils.getInt(tableData,3);
        nrOfDemands=C12ParseUtils.getInt(tableData,4);
        nrOfCoinValues=C12ParseUtils.getInt(tableData,5);
        nrOfOccur=C12ParseUtils.getInt(tableData,6);
        nrOfTiers=C12ParseUtils.getInt(tableData,7);
        nrOfPresentDemands=C12ParseUtils.getInt(tableData,8);
        nrOfPresentValues=C12ParseUtils.getInt(tableData,9);
    }

    public int getRegisterFunction1Bitfield() {
        return registerFunction1Bitfield;
    }

    public void setRegisterFunction1Bitfield(int registerFunction1Bitfield) {
        this.registerFunction1Bitfield = registerFunction1Bitfield;
    }

    public int getRegisterFunction2Bitfield() {
        return registerFunction2Bitfield;
    }

    public void setRegisterFunction2Bitfield(int registerFunction2Bitfield) {
        this.registerFunction2Bitfield = registerFunction2Bitfield;
    }

    public int getNrOfSelfReads() {
        return nrOfSelfReads;
    }

    public void setNrOfSelfReads(int nrOfSelfReads) {
        this.nrOfSelfReads = nrOfSelfReads;
    }

    public int getNrOfSummations() {
        return nrOfSummations;
    }

    public void setNrOfSummations(int nrOfSummations) {
        this.nrOfSummations = nrOfSummations;
    }

    public int getNrOfDemands() {
        return nrOfDemands;
    }

    public void setNrOfDemands(int nrOfDemands) {
        this.nrOfDemands = nrOfDemands;
    }

    public int getNrOfCoinValues() {
        return nrOfCoinValues;
    }

    public void setNrOfCoinValues(int nrOfCoinValues) {
        this.nrOfCoinValues = nrOfCoinValues;
    }

    public int getNrOfOccur() {
        return nrOfOccur;
    }

    public void setNrOfOccur(int nrOfOccur) {
        this.nrOfOccur = nrOfOccur;
    }

    public int getNrOfTiers() {
        return nrOfTiers;
    }

    public void setNrOfTiers(int nrOfTiers) {
        this.nrOfTiers = nrOfTiers;
    }

    public int getNrOfPresentDemands() {
        return nrOfPresentDemands;
    }

    public void setNrOfPresentDemands(int nrOfPresentDemands) {
        this.nrOfPresentDemands = nrOfPresentDemands;
    }

    public int getNrOfPresentValues() {
        return nrOfPresentValues;
    }

    public void setNrOfPresentValues(int nrOfPresentValues) {
        this.nrOfPresentValues = nrOfPresentValues;
    }

}
