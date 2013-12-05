/*
 * MessageEntry.java
 *
 * Created on 27 juli 2007, 15:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.mdc.protocol.api.device.data;

/**
 * @author kvds
 */
public class MessageEntry {

    private String content;
    private String trackingId;
    private String serialNumber;

    /**
     * Creates a new instance of MessageEntry
     */
    public MessageEntry(String content, String trackingId) {
        this(content, trackingId, "");
    }

    public MessageEntry(String content, String trackingId, String serialNumber) {
        this.setContent(content);
        this.setTrackingId(trackingId);
        this.setSerialNumber(serialNumber);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuilder builder = new StringBuilder();
        builder.append("MessageEntry:\n");
        builder.append("   content=").append(getContent()).append("\n");
        builder.append("   trackingId=").append(getTrackingId()).append("\n");
        return builder.toString();
    }

    public String getContent() {
        return content;
    }

    private void setContent(String content) {
        this.content = content;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public boolean hasSerialNumber() {
        return (getSerialNumber() != null) && (!getSerialNumber().trim().isEmpty());
    }

}
