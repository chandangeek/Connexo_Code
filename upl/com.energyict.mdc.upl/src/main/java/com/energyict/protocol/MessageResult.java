/*
 * MessageEntry.java
 *
 * Created on 27 juli 2007, 15:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocol;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;

/**
 * @author kvds
 */
public class MessageResult {

    public static final int MaxProtocolInfoSize = 255;

    static private final int SUCCESS = 0;
    static private final int FAILED = 1;
    static private final int QUEUED = 2;
    static private final int UNKNOWN = 3;
    private final String info;
    int state;
    private MessageEntry messageEntry;

    /**
     * Creates a new instance of MessageEntry
     */
    public MessageResult(MessageEntry messageEntry, int state, final String info) {
        this.messageEntry = messageEntry;
        this.state = state;
        this.info = info;
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

    public static int getSUCCESS() {
        return SUCCESS;
    }

    public static int getFAILED() {
        return FAILED;
    }

    public static int getQUEUED() {
        return QUEUED;
    }

    public static int getUNKNOWN() {
        return UNKNOWN;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageResult:\n");
        builder.append("\t failed=" + isFailed() + "\n");
        builder.append("\t messageEntry=" + getMessageEntry() + "\n");
        builder.append("\t queued=" + isQueued() + "\n");
        builder.append("\t success=" + isSuccess() + "\n");
        builder.append("\t unknown=" + isUnknown() + "\n");
        builder.append("\t Additional Info = " + getInfo());
        return builder.toString();
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
}
