/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataDumpFactory.java
 *
 * Created on 31 juli 2006, 10:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DataDumpFactory {

    final int DEBUG=0;

    CommandFactory commandFactory;
    private DumpCommand dumpCommand;

    /** Creates a new instance of DataDumpFactory */
    public DataDumpFactory(CommandFactory commandFactory) {
        this.commandFactory=commandFactory;
    }

    public byte[] collectHistoryLogDataBlocks() throws IOException {
        setDumpCommand(commandFactory.getDumpCommand(1, 0xFF, true, false));
        if (DEBUG>=1) System.out.println("KV_DEBUG> "+getDumpCommand());
        return doCollectDataBlocks();
    }

    public byte[] collectLoadProfileDataBlocks(int nrOfBlocks) throws IOException {
        setDumpCommand(commandFactory.getDumpCommand(nrOfBlocks-1, 0xFF, false, false));
        if (DEBUG>=1) System.out.println("KV_DEBUG> "+getDumpCommand());
        return doCollectDataBlocks();
    }

    private byte[] doCollectDataBlocks() throws IOException {
        int blockNr = 0xFF;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while(true) {
            ResponseFrame rf = commandFactory.getS200().getS200Connection().sendDataBlockAcknowledge(blockNr);
            if (rf.isAck())
                return baos.toByteArray();
            blockNr = rf.getFrameNr();
            baos.write(rf.getData());
            if (rf.isLastBlock()) {
                // KV 29082006 blijkbaar production meters geen extra frame na laatste block ack. Meter at Randy's deed dat wel...
                // Therefor, we catch the TIMEOUT reason here

                rf = commandFactory.getS200().getS200Connection().sendDataBlockAcknowledge(blockNr);

                if (rf==null) // in case of timeout
                    return baos.toByteArray();

                if (rf.isAck()) // in case frame response on last block ack
                    return baos.toByteArray();
            }
        }
    }

    public DumpCommand getDumpCommand() {
        return dumpCommand;
    }

    private void setDumpCommand(DumpCommand dumpCommand) {
        this.dumpCommand = dumpCommand;
    }



}
