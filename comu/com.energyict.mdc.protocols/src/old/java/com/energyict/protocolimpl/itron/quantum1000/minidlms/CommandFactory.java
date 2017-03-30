/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CommandFactory.java
 *
 * Created on 1 december 2006, 13:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class CommandFactory {

    private ProtocolLink protocolLink;

    /** Creates a new instance of CommandFactory */
    public CommandFactory(ProtocolLink protocolLink) {
        this.protocolLink=protocolLink;
    }

    public InitiateResponse initiate() throws IOException {
        InitiateCommand ic = new InitiateCommand(this);
        ic.invoke();
        return (InitiateResponse)ic.getResponse();
    }

    public void abort() throws IOException {
        Abort a = new Abort(this);
        a.invoke();
    }

    public ReadCommand read(int variableName) throws IOException {
        ReadCommand rc = new ReadCommand(this);
        rc.setVariableName(variableName);
        //rc.invoke();
        //return (ReadReply)rc.getResponse();
        return rc;
    }

    public ReadIndexedCommand readIndexed(int variableName,int index, int indexTag) throws IOException {
        ReadIndexedCommand rc = new ReadIndexedCommand(this);
        rc.setVariableName(variableName);
        rc.setIndex(index);
        rc.setIndexTag(indexTag);
        //rc.invoke();
        //return (ReadReply)rc.getResponse();
        return rc;
    }

    public ReadIndexedWithRangeCommand readIndexedWithRange(int variableName,int index, int range, int indexRangeTag) throws IOException {
        ReadIndexedWithRangeCommand rc = new ReadIndexedWithRangeCommand(this);
        rc.setVariableName(variableName);
        rc.setIndex(index);
        rc.setIndexRangeTag(indexRangeTag);
        rc.setRange(range);
        //rc.invoke();
        //return (ReadReply)rc.getResponse();
        return rc;
    }

    public WriteCommand write(int variableName,byte[] data) throws IOException {
        WriteCommand wc = new WriteCommand(this);
        wc.setVariableName(variableName);
        wc.setData(data);
        return wc;
    }

    public WriteIndexedCommand writeIndexed(int variableName, int index, byte[] data) throws IOException {
        WriteIndexedCommand wc = new WriteIndexedCommand(this);
        wc.setVariableName(variableName);
        wc.setIndex(index);
        wc.setData(data);
        return wc;
    }

    public WriteIndexedWithRangeCommand writeIndexedWithRange(int variableName, int index, int range, byte[] data) throws IOException {
        WriteIndexedWithRangeCommand wc = new WriteIndexedWithRangeCommand(this);
        wc.setVariableName(variableName);
        wc.setIndex(index);
        wc.setRange(range);
        wc.setData(data);
        return wc;
    }

    public InitiateUploadCommand initiateUpload(int dataSetId)  throws IOException {
       InitiateUploadCommand iuc = new InitiateUploadCommand(this);
       iuc.setDataSetID(dataSetId);
       return iuc;
    }

    public UploadSegmentCommand uploadSegment(int segmentNumber)  throws IOException {
       UploadSegmentCommand usc = new UploadSegmentCommand(this);
       usc.setSegmentNumber(segmentNumber);
       return usc;
    }

    public TerminateLoadCommand terminateLoad()  throws IOException {
       TerminateLoadCommand tlc = new TerminateLoadCommand(this);
       return tlc;
    }

    public ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    public void setProtocolLink(ProtocolLink protocolLink) {
        this.protocolLink = protocolLink;
    }


}
