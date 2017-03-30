/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * UploadSegmentCommand.java
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
public class UploadSegmentCommand extends AbstractConfirmedServiceCommand {

    private int segmentNumber; // SegmentNumber is the 16 bit number of the segment to upload. The first segment is 1

    /** Creates a new instance of UploadSegmentCommand */
    public UploadSegmentCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        return "UploadSegmentCommand:\n" + "   segmentNumber=" + getSegmentNumber() + "\n";
    }

    protected int getCommandId() {
        return 0x10;
    }

    protected byte[] prepareBuild() {
        return new byte[]{(byte)(getSegmentNumber()>>8),(byte)getSegmentNumber()};
    }

    public int getSegmentNumber() {
        return segmentNumber;
    }

    public void setSegmentNumber(int segmentNumber) {
        this.segmentNumber = segmentNumber;
    }

}