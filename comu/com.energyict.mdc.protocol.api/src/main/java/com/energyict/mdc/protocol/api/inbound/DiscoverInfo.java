/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DiscoverInfo.java
 *
 * Created on 16 juni 2005, 13:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.mdc.protocol.api.inbound;

import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;

import java.util.List;

/**
 * @author Koen
 */
public class DiscoverInfo {

    private SerialCommunicationChannel commChannel;
    private String nodeId;
    private int baudrate;
    private List passwords;

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

    public DiscoverInfo(SerialCommunicationChannel commChannel, String nodeId, int baudrate, List passwords) {
        this.setNodeId(nodeId);
        this.setBaudrate(baudrate);
        this.setCommChannel(commChannel);
        this.setPasswords(passwords);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DiscoverInfo: ");
        strBuff.append("nodeId=" + getNodeId() + ", baudrate=" + getBaudrate() + ", passwords:\n");
        for (int i = 0; i < getPasswords().size(); i++) {
            strBuff.append((String) getPasswords().get(i) + "\n");
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

    public List getPasswords() {
        return passwords;
    }

    public void setPasswords(List passwords) {
        this.passwords = passwords;
    }


}
