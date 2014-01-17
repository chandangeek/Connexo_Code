package com.energyict.protocols.messaging;

import org.xml.sax.SAXException;

import java.io.IOException;


/**
 * Implementing classes of {@link MessageBuilder} are able to generate and parse messages.
 *
 * @return The {@link MessageBuilder} capable of generating and parsing messages.
 */
public interface MessageBuilder {

    /**
     * Return a readable description of the message being built
     *
     * @return
     */
    String getDescription();

    /**
     * Init the builder with the given xml
     *
     * @param xml
     */
    void initFromXml(String xml) throws IOException, SAXException;

}
