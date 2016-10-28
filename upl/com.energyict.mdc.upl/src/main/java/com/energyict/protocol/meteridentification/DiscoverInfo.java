/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DiscoverInfo.java
 *
 * Created on 16 juni 2005, 13:41
 *
 */

package com.energyict.protocol.meteridentification;

import com.energyict.dialer.core.SerialCommunicationChannel;

import java.util.List;

/**
 * @author Koen
 */
public class DiscoverInfo {

    private SerialCommunicationChannel commChannel;
    private String nodeId;
    private int baudrate;
    private List<String> passwords;

    /**
     * Creates a new instance of DiscoverInfo
     */
    public DiscoverInfo(SerialCommunicationChannel commChannel) {
        this(commChannel, null, 0);
    }

    public DiscoverInfo(SerialCommunicationChannel commChannel, String nodeId) {
        this(commChannel, nodeId, 0);
    }

    public DiscoverInfo(SerialCommunicationChannel commChannel, String nodeId, int baudrate) {
        this(commChannel, nodeId, baudrate, null);
    }

    public DiscoverInfo(SerialCommunicationChannel commChannel, String nodeId, int baudrate, List<String> passwords) {
        this.setNodeId(nodeId);
        this.setBaudrate(baudrate);
        this.setCommChannel(commChannel);
        this.setPasswords(passwords);
    }

    public String toString() {
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("DiscoverInfo: ");
        strBuff.append("nodeId=").append(getNodeId()).append(", baudrate=").append(getBaudrate()).append(", passwords:\n");
        for (int i = 0; i < getPasswords().size(); i++) {
            strBuff.append(getPasswords().get(i)).append("\n");
        }
        return strBuff.toString();
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public int getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    public SerialCommunicationChannel getCommChannel() {
        return commChannel;
    }

    public void setCommChannel(SerialCommunicationChannel commChannel) {
        this.commChannel = commChannel;
    }

    public List<String> getPasswords() {
        return passwords;
    }

    public void setPasswords(List<String> passwords) {
        this.passwords = passwords;
    }

}