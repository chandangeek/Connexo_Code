/*
 * MessageEntry.java
 *
 * Created on 27 juli 2007, 15:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;

/**
 * @author kvds
 */
public class MessageEntry {

    public final static MessageEntry EMPTY = new MessageEntry("","");

    private final String content;
    private final String trackingId;
    private String serialNumber;

    public static MessageEntry empty() {
        return new MessageEntry("", "");
    }

    public static Builder fromContent(String content) {
        return new Builder(content);
    }

    public static Builder from(Messaging messaging, MessageTag tag) {
        return fromContent(messaging.writeTag(tag));
    }

    private MessageEntry(String content, String trackingId) {
        this(content, trackingId, "");
    }

    private MessageEntry(String content, String trackingId, String serialNumber) {
        this.content = content;
        this.trackingId = trackingId;
        this.serialNumber = serialNumber;
    }

    public String toString() {
        return "MessageEntry:\n" +
                "   content=" + getContent() + "\n" +
                "   trackingId=" + getTrackingId() + "\n";
    }

    public String getContent() {
        return content;
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

    public static final class Builder {
        private final String content;
        private String trackingId = "";
        private String serialNumber = "";

        private Builder(String content) {
            this.content = content;
        }

        public Builder andMessage(OfflineDeviceMessage message) {
            return this.trackingId(message.getTrackingId());
        }

        public Builder trackingId(String trackingId) {
            this.trackingId = trackingId;
            return this;
        }

        public Builder serialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        public MessageEntry finish() {
            return new MessageEntry(this.content, this.trackingId, this.serialNumber);
        }
    }

}