/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SelfReadDataUpload.java
 *
 * Created on 9 januari 2007, 11:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;




/**
 *
 * @author Koen
 */
public class SelfReadDataUpload {

    private final int SELF_READ_FILE_OBJ_NAME=1;
    private ProtocolLink protocolLink;

    // **************************************************************************************************
    // selfread header

    /* This indicates whether or not the last self read was interrupted by an EPF. If this
    flag is cleared, then the most recent self reads data record will most likely be
    corrupted. It would be wise to ignore the most recent self read if this flag is
    cleared.*/
    private boolean commitFlag; // BOOLEAN,

    /* Let n = the programmed number of records per file, according to the self read
    configuration. If numSelfReads > n, then only the most recent n self read records are
    present, due to the nature of the circular self read file; in order to find the chronologically
    first occurring records (absolute) starting byte, use the modulo function as such:
    Starting byte = 3 + [ (numSelfReads % n) * blockSize ] The variables in the above
    equation can easily be obtained from SelfReadGeneralInfo DLMS Object.
    If numSelfReads < n, then this is the actual number of self reads in the file. In this case,
    simply start with the first record after the header.    */
    private int numSelfReads; // UNSIGNED16.

    // **************************************************************************************************
    // selfread records
    private List selfReadDataRecords; // of type SelfReadDataRecord


    SelfReadGeneralInformation selfReadGeneralInformation=null;
    SelfReadRegisterConfiguration selfReadRegisterConfiguration=null;

    /**
     * Creates a new instance of SelfReadDataUpload
     */
    public SelfReadDataUpload(ProtocolLink protocolLink) {
        this.setProtocolLink(protocolLink);
    }


    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadDataUpload:\n");
        strBuff.append("   commitFlag="+isCommitFlag()+"\n");
        strBuff.append("   numSelfReads="+getNumSelfReads()+"\n");
        strBuff.append("   selfReadDataRecords="+getSelfReadDataRecords()+"\n");
        return strBuff.toString();
    }

    public void invoke() throws IOException {

        selfReadGeneralInformation = getProtocolLink().getDataDefinitionFactory().getSelfReadGeneralInformation();
        selfReadRegisterConfiguration = getProtocolLink().getDataDefinitionFactory().getSelfReadRegisterConfiguration();

        InitiateUploadResponse ior = getProtocolLink().getApplicationStateMachine().initiateUpload(SELF_READ_FILE_OBJ_NAME);
        int nrOfSegments = ior.getNrOfSegments();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int segment=0;segment<nrOfSegments;segment++) {
            UploadSegmentResponse usr = getProtocolLink().getApplicationStateMachine().uploadSegment(segment+1);
            baos.write(usr.getData());
        }


        parse(baos.toByteArray());

        TerminateLoadResponse tlr = getProtocolLink().getApplicationStateMachine().terminateLoad();

    }

    private void parse(byte[] data) throws IOException {
        int offset=0;

        // read the header
        setCommitFlag(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setNumSelfReads(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;

        // read the selfRead sets
        setSelfReadDataRecords(new ArrayList());
        for (int i=0;i<selfReadGeneralInformation.numberOfSelfReadSets();i++) {
           offset += (((getNumSelfReads()-i) % selfReadGeneralInformation.getNumRecordsPerFile()) * selfReadGeneralInformation.getBlockSize()); // blocksize of gebruik maken van selfreadrecord.size() ???
           if ((i>0) ||  (isCommitFlag()))
              getSelfReadDataRecords().add(new SelfReadDataRecord(data, offset, getProtocolLink().getProtocol().getTimeZone(), selfReadRegisterConfiguration));
        }
    }

    public ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    public void setProtocolLink(ProtocolLink protocolLink) {
        this.protocolLink = protocolLink;
    }

    public boolean isCommitFlag() {
        return commitFlag;
    }

    public void setCommitFlag(boolean commitFlag) {
        this.commitFlag = commitFlag;
    }

    public int getNumSelfReads() {
        return numSelfReads;
    }

    public void setNumSelfReads(int numSelfReads) {
        this.numSelfReads = numSelfReads;
    }

    public List getSelfReadDataRecords() {
        return selfReadDataRecords;
    }

    public void setSelfReadDataRecords(List selfReadDataRecords) {
        this.selfReadDataRecords = selfReadDataRecords;
    }



}
