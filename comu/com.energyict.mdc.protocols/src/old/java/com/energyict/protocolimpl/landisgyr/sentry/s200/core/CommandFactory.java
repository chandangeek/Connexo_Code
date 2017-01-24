/*
 * CommandFactory.java
 *
 * Created on 26 juli 2006, 17:20
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import com.energyict.protocolimpl.landisgyr.sentry.s200.S200;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class CommandFactory {

    private S200 s200;

    ForceStatusCommand fsc=null;
    RevisionLevelCommand rlc=null;
    VerifyCommand vc=null;
    LookAtCommand la=null;
    BeginRecordTimeCommand brt=null;

    /** Creates a new instance of CommandFactory */
    public CommandFactory(S200 s200) {
        this.setS200(s200);
    }

    public BeginRecordTimeCommand getBeginRecordTimeCommand() throws IOException {
        if (brt == null) {
            brt = new BeginRecordTimeCommand(this);
            brt.build();
        }
        return brt;
    }

    public ForceStatusCommand getForceStatusCommand() throws IOException {
        if (fsc == null) {
            fsc = new ForceStatusCommand(this);
            fsc.build();
        }
        return fsc;
    }

    public RevisionLevelCommand getRevisionLevelCommand() throws IOException {
        if (rlc == null) {
            rlc = new RevisionLevelCommand(this);
            rlc.build();
        }
        return rlc;
    }

    public VerifyCommand getVerifyCommand() throws IOException {
        if (vc == null) {
            vc = new VerifyCommand(this);
            vc.build();
        }
        return vc;
    }

    public LookAtCommand getLookAtCommand() throws IOException {
        if (la == null) {
            la = new LookAtCommand(this);
            la.init();
        }
        return la;
    }

    public MeterDataCommand getMeterDataCommand(int meterInput) throws IOException {
        MeterDataCommand mdc = new MeterDataCommand(this);
        mdc.setMeterInput(meterInput);
        mdc.build();
        return mdc;
    }

    public QueryTimeCommand getQueryTimeCommand() throws IOException {
        QueryTimeCommand qtc = new QueryTimeCommand(this);
        qtc.build();
        return qtc;
    }

    public void hangup() throws IOException {
        OperateRelayCommand orc = new OperateRelayCommand(this);
        orc.build();
    }

    public EnterTimeCommand getEnterTimeCommand() throws IOException {
        EnterTimeCommand etc = new EnterTimeCommand(this);
        etc.build();
        return etc;
    }

    public DumpCommand getDumpCommand(int nrOfBlocks, int channels, boolean dumpHistoryLog, boolean dumpLoadControlMessageTable) throws IOException {
        DumpCommand dc = new DumpCommand(this);
        dc.setNrOfBlocks(nrOfBlocks);
        dc.setChannels(channels);
        dc.setDumpHistoryLog(dumpHistoryLog);
        dc.setDumpLoadControlMessageTable(dumpLoadControlMessageTable);
        dc.build();
        return dc;
    }


    public S200 getS200() {
        return s200;
    }

    public void setS200(S200 s200) {
        this.s200 = s200;
    }


}
