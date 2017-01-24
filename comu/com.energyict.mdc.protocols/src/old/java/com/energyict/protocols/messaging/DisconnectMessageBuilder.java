package com.energyict.protocols.messaging;

import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Message builder class responsible of generating and parsing Disconnect messages.
 *
 * @author isabelle
 */
public class DisconnectMessageBuilder extends AbstractMessageBuilder {

    private static final String MESSAGETAG = "Disconnect";

    public String getDescription() {
        return MESSAGETAG;
    }

    public static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    /**
     * {@inheritDoc}
     */
    protected String getMessageContent() {
        return "<" + MESSAGETAG + "/>";
    }

    public AdvancedMessageHandler getMessageHandler(MessageBuilder builder) {
        return new DisconnectMessageHandler((DisconnectMessageBuilder) builder, getMessageNodeTag());
    }

    public static MessageBuilder fromXml(String xmlString) throws SAXException, IOException {
        MessageBuilder builder = new DisconnectMessageBuilder();
        builder.initFromXml(xmlString);
        return builder;
    }

    public static class DisconnectMessageHandler extends AbstractMessageBuilder.AdvancedMessageHandler {

        private DisconnectMessageBuilder msgBuilder;

        public DisconnectMessageHandler(DisconnectMessageBuilder builder, String messageTag) {
            super(messageTag);
            this.msgBuilder = builder;
        }

        protected AbstractMessageBuilder getMessageBuilder() {
            return msgBuilder;
        }

    }

}
