/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
public class MessageResult {

    public static final int MaxProtocolInfoSize = 255;

    private static final int SUCCESS = 0;
    private static final int FAILED = 1;
    private static final int QUEUED = 2;
    private static final int UNKNOWN = 3;

    int state;

    private MessageEntry messageEntry;

    private final String info;

    /**
     * Creates a new instance of MessageEntry
     */
    MessageResult(MessageEntry messageEntry, int state, final String info) {
        this.messageEntry = messageEntry;
        this.state = state;
        this.info = info;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageResult:\n");
        builder.append("\t failed=").append(isFailed()).append("\n");
        builder.append("\t messageEntry=").append(getMessageEntry()).append("\n");
        builder.append("\t queued=").append(isQueued()).append("\n");
        builder.append("\t success=").append(isSuccess()).append("\n");
        builder.append("\t unknown=").append(isUnknown()).append("\n");
        builder.append("\t Additional Info = ").append(getInfo());
        return builder.toString();
    }


    public static MessageResult createSuccess(MessageEntry messageEntry) {
        return new MessageResult(messageEntry, getSUCCESS(), "");
    }

    public static MessageResult createFailed(MessageEntry messageEntry) {
        return new MessageResult(messageEntry, getFAILED(), "");
    }

    public static MessageResult createQueued(MessageEntry messageEntry) {
        return new MessageResult(messageEntry, getQUEUED(), "");
    }

    public static MessageResult createUnknown(MessageEntry messageEntry) {
        return new MessageResult(messageEntry, getUNKNOWN(), "");
    }

    public static MessageResult createSuccess(final MessageEntry messageEntry, final String protocolInfo) {
        return new MessageResult(messageEntry, getSUCCESS(), protocolInfo);
    }

    public static MessageResult createFailed(final MessageEntry messageEntry, final String protocolInfo) {
        return new MessageResult(messageEntry, getFAILED(), protocolInfo);
    }

    public static MessageResult createQueued(final MessageEntry messageEntry, final String protocolInfo) {
        return new MessageResult(messageEntry, getQUEUED(), protocolInfo);
    }

    public static MessageResult createUnknown(final MessageEntry messageEntry, final String protocolInfo) {
        return new MessageResult(messageEntry, getUNKNOWN(), protocolInfo);
    }

    public boolean isSuccess() {
        return state == getSUCCESS();
    }

    public boolean isFailed() {
        return state == getFAILED();
    }

    public boolean isQueued() {
        return state == getQUEUED();
    }

    public boolean isUnknown() {
        return state == getUNKNOWN();
    }

    public String getInfo() {
        return info;
    }

    public MessageEntry getMessageEntry() {
        return messageEntry;
    }

    public void setMessageEntry(MessageEntry messageEntry) {
        this.messageEntry = messageEntry;
    }

    static int getSUCCESS() {
        return SUCCESS;
    }

    static int getFAILED() {
        return FAILED;
    }

    static int getQUEUED() {
        return QUEUED;
    }

    static int getUNKNOWN() {
        return UNKNOWN;
    }
}
