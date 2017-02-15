/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InitiateUploadCommand.java
 *
 * Created on 4 december 2006, 15:59
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
public class InitiateUploadCommand extends AbstractConfirmedServiceCommand {



    private int dataSetID; // 16 bit Data Set Id is a numeric variable identifier from the QM110 Data Definition document.

    /** Creates a new instance of InitiateUploadCommand */
    public InitiateUploadCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        return "InitiateUploadCommand:\n" + "   dataSetID=" + getDataSetID() + "\n";
    }

    protected int getCommandId() {
        return 0x12;
    }

    protected byte[] prepareBuild() {
        return new byte[]{(byte)(getDataSetID()>>8),(byte)(getDataSetID())};
    }

    public int getDataSetID() {
        return dataSetID;
    }

    public void setDataSetID(int dataSetID) {
        this.dataSetID = dataSetID;
    }

}
