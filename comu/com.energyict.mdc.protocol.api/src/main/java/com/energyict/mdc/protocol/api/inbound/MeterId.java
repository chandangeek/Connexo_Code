/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterId.java
 *
 * Created on 23 oktober 2003, 15:28
 */

package com.energyict.mdc.protocol.api.inbound;

/**
 * @author Koen
 */
public class MeterId {

    String identification;
    String nodeId;
    String serialNr;
    String meter3letterId;
    byte[] dataDump;

    /**
     * Creates a new instance of MeterId
     */
    public MeterId(String identification, String nodeId, String serialNr, String meter3letterId) {
        this(identification, nodeId, serialNr, meter3letterId, null);
    }

    public MeterId(String identification, String nodeId, String serialNr, String meter3letterId, byte[] dataDump) {
        this.identification = identification;
        this.nodeId = nodeId;
        this.serialNr = serialNr;
        this.meter3letterId = meter3letterId;
        this.dataDump = dataDump;
    }

    /**
     * Getter for property identification.
     *
     * @return Value of property identification.
     */
    public java.lang.String getIdentification() {
        return identification;
    }

    /**
     * Setter for property identification.
     *
     * @param identification New value of property identification.
     */
    public void setIdentification(java.lang.String identification) {
        this.identification = identification;
    }

    /**
     * Getter for property nodeId.
     *
     * @return Value of property nodeId.
     */
    public java.lang.String getNodeId() {
        return nodeId;
    }

    /**
     * Setter for property nodeId.
     *
     * @param nodeId New value of property nodeId.
     */
    public void setNodeId(java.lang.String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Getter for property serialNr.
     *
     * @return Value of property serialNr.
     */
    public java.lang.String getSerialNr() {
        return serialNr;
    }

    /**
     * Setter for property serialNr.
     *
     * @param serialNr New value of property serialNr.
     */
    public void setSerialNr(java.lang.String serialNr) {
        this.serialNr = serialNr;
    }

    /**
     * Getter for property meter3letterId.
     *
     * @return Value of property meter3letterId.
     */
    public java.lang.String getMeter3letterId() {
        return meter3letterId;
    }

    public byte[] getDataDump() {
        return dataDump;
    }

}