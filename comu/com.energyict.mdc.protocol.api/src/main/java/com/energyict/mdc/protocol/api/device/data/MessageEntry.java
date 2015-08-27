package com.energyict.mdc.protocol.api.device.data;

public class MessageEntry {

    public final static MessageEntry EMPTY = new MessageEntry("","");

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
        this.content = content;
        this.setTrackingId(trackingId);
        this.setSerialNumber(serialNumber);
    }

    public String toString() {
        return "MessageEntry:\n" +
               "   content=" + content + "\n" +
               "   trackingId=" + trackingId + "\n";
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
        return (serialNumber != null) && (!serialNumber.trim().isEmpty());
    }

}
