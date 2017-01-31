/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CommandFactory.java
 *
 * Created on 9 augustus 2005, 13:32
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import com.energyict.protocolimpl.transdata.markv.MarkV;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author koen
 */
public class CommandFactory {

    private MarkV markV=null;


    // cached
    //IICommand iICommand=null; // meter identification
    SCCommand sCCommand=null; // meter registers scroll quantities
    HCCommand hCCommand=null; // meter registers alternate quantities
    CCCommand cCCommand=null; // meter registers communication quantities
    DCCommand dCCommand=null; // meter's channellist
    IDCommand iDCommand=null; // meter id & serial number
    ISCommand iSCommand=null; // profile & meter configuration

    //GTCommand gTCommand=null; // get meter's time date command
    //LOCommand lOCommand=null; // logoff command
    MICommand mICommand=null; // meter's model & serial number
    //RCCommand rCCommand=null; // profile data
    //RVCommand rVCommand=null; // event log
    //TICommand tICommand=null; // set time

    /** Creates a new instance of CommandFactory */
    public CommandFactory(MarkV markV) {
        this.setMarkV(markV);
    }


//
//    public IICommand getIICommand() throws IOException {
//        if (iICommand == null) {
//            iICommand = new IICommand(this);
//            iICommand.build();
//        }
//        return iICommand;
//    }
    public SCCommand getSCCommand() throws IOException {
        if (sCCommand == null) {
            sCCommand = new SCCommand(this);
            sCCommand.build();
        }
        return sCCommand;
    }
    public HCCommand getHCCommand() throws IOException {
        if (hCCommand == null) {
            hCCommand = new HCCommand(this);
            hCCommand.build();
        }
        return hCCommand;
    }
    public CCCommand getCCCommand() throws IOException {
        if (cCCommand == null) {
            cCCommand = new CCCommand(this);
            cCCommand.build();
        }
        return cCCommand;
    }
    public DCCommand getDCCommand() throws IOException {
        if (dCCommand == null) {
            dCCommand = new DCCommand(this);
            dCCommand.build();
        }
        return dCCommand;
    }
    public IDCommand getIDCommand() throws IOException {
        if (iDCommand == null) {
            iDCommand = new IDCommand(this);
            iDCommand.build();
        }
        return iDCommand;
    }
    public ISCommand getISCommand() throws IOException {
        if (iSCommand == null) {
            iSCommand = new ISCommand(this);
            iSCommand.build();
        }
        return iSCommand;
    }
    public MICommand getMICommand() throws IOException {
        if (mICommand == null) {
            mICommand = new MICommand(this);
            mICommand.build();
        }
        return mICommand;
    }
    public RCCommand getRCCommand(int nrOfRecords) throws IOException {
        RCCommand rCCommand = new RCCommand(this);
        rCCommand.setNrOfRecords(nrOfRecords);
        rCCommand.build();
        return rCCommand;
    }
    public RVCommand getRVCommand(int nrOfRecords) throws IOException {
        RVCommand rVCommand = new RVCommand(this);
        rVCommand.setNrOfRecords(nrOfRecords);
        rVCommand.build();
        return rVCommand;
    }
    public void issueTICommand() throws IOException {
        TICommand tICommand = new TICommand(this);
        tICommand.build();
    }

    public void issueTCCommand(Date nextDialin) throws IOException {
        TCCommand tCCommand = new TCCommand(this);
        tCCommand.setNextDialin(nextDialin);
        tCCommand.build();
    }

    // logoff
    public void issueLOCommand() throws IOException {
        LOCommand lOCommand = new LOCommand(this);
        lOCommand.build();
    }

    // get time
    public GTCommand getGTCommand() throws IOException {
        GTCommand gTCommand = new GTCommand(this);
        gTCommand.build();
        return gTCommand;
    }
    public GCCommand getGCCommand() throws IOException {
        GCCommand gCCommand = new GCCommand(this);
        gCCommand.build();
        return gCCommand;
    }

    public MarkV getMarkV() {
        return markV;
    }

    public void setMarkV(MarkV markV) {
        this.markV = markV;
    }


}
