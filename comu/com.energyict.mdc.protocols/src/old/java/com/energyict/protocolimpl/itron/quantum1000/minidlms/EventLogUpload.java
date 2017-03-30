/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EventLogUpload.java
 *
 * Created on 3 januari 2007, 11:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Koen
 */
public class EventLogUpload {

    private final int EVENT_LOG_OBJ_NAME=23;

    private List eventRecordTypes; // of type EventRecordType
    private ProtocolLink protocolLink;

    /**
     * Creates a new instance of EventLogUpload
     */
    public EventLogUpload(ProtocolLink protocolLink) {
        this.setProtocolLink(protocolLink);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EventLogUpload:\n");
        for (int i=0;i<getEventRecordTypes().size();i++) {
            strBuff.append("   eventRecordTypes("+i+")="+(EventRecordType)getEventRecordTypes().get(i)+"\n");
        }
        return strBuff.toString();
    }

    public void invoke() throws IOException {

        InitiateUploadResponse ior = getProtocolLink().getApplicationStateMachine().initiateUpload(EVENT_LOG_OBJ_NAME);
        int nrOfSegments = ior.getNrOfSegments();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int segment=0;segment<nrOfSegments;segment++) {
            UploadSegmentResponse usr = getProtocolLink().getApplicationStateMachine().uploadSegment(segment+1);
            baos.write(usr.getData());
        }


        parse(baos.toByteArray());

        TerminateLoadResponse tlr = getProtocolLink().getApplicationStateMachine().terminateLoad();

    }

    private final int HEADER_SIZE=16;
    private void parse(byte[] data) throws IOException {

        int offset=HEADER_SIZE; // skip HEADER_SIZE header bytes
        int nrOfEventRecords = (data.length-HEADER_SIZE) / EventRecordType.size();
        setEventRecordTypes(new ArrayList());
        for (int i = 0;i<nrOfEventRecords;i++) {
            getEventRecordTypes().add(new EventRecordType(data,offset,getProtocolLink().getProtocol().getTimeZone()));
            offset+=EventRecordType.size();
        }

    }

    public ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    public void setProtocolLink(ProtocolLink protocolLink) {
        this.protocolLink = protocolLink;
    }

    public List getEventRecordTypes() {
        return eventRecordTypes;
    }

    public void setEventRecordTypes(List eventRecordTypes) {
        this.eventRecordTypes = eventRecordTypes;
    }



} // public class EventLogUpload
