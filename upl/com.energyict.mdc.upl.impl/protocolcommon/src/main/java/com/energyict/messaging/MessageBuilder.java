package com.energyict.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.shadow.OldDeviceMessageShadow;
import org.xml.sax.SAXException;

import java.io.IOException;


/**
 * Implementing classes of {@link MessageBuilder} are able to generate and parse messages.
 *
 * @return The {@link MessageBuilder} capable of generating and parsing messages.
 */
public interface MessageBuilder {

    /**
     * Build an rtu message shadow based on this builders configuration
     *
     * @return a DeviceMessageShadow object
     * @throws BusinessException
     */
    OldDeviceMessageShadow build() throws BusinessException;

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
