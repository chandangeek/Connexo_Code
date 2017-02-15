/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Message builder class responsible of generating and parsing Connect messages.
 *
 * @author isabelle
 */
public class ConnectMessageBuilder extends AbstractMessageBuilder {

    private static final String MESSAGETAG = "Connect";

    public String getDescription() {
        return MESSAGETAG;
    }

    public static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    protected String getMessageContent() {
        return "<" + MESSAGETAG + "/>";
    }

    public AdvancedMessageHandler getMessageHandler(MessageBuilder builder) {
        return new ConnectMessageHandler((ConnectMessageBuilder) builder, getMessageNodeTag());
    }

    /**
     * Returns a fully initialized CosemMethodMessageBuilder starting from the given xml String
     *
     * @param xmlString string to initialize the messagebuilder
     * @throws SAXException, IOException if the given string is wrong xml
     *          or if the message is not a 'CosemMethod' message
     * @return a initialized CosemMethodMessageBuilder if the message is a correct 'CosemMethod' message
     */
    public static MessageBuilder fromXml(String xmlString) throws SAXException, IOException {
        MessageBuilder builder = new ConnectMessageBuilder();
        builder.initFromXml(xmlString);
        return builder;
    }

    public static class ConnectMessageHandler extends AbstractMessageBuilder.AdvancedMessageHandler {

        private ConnectMessageBuilder msgBuilder;

        public ConnectMessageHandler(ConnectMessageBuilder builder, String messageTag) {
            super(messageTag);
            this.msgBuilder = builder;
        }

        protected AbstractMessageBuilder getMessageBuilder() {
            return msgBuilder;
        }

    }

}