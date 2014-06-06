package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import org.osgi.service.component.annotations.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 6/3/14
 * Time: 5:32 PM
 */
@Component(name = "com.energyict.protocols.mdc.services.impl.DeviceCacheMarshallingServiceImpl", service = DeviceCacheMarshallingService.class, immediate = true)
public class DeviceCacheMarshallingServiceImpl implements DeviceCacheMarshallingService {

    private static final Logger LOGGER = Logger.getLogger(DeviceCacheMarshallingServiceImpl.class.getName());
    private static final String REGEX = ":::";
    private static final int NUMBER_OF_ELEMENTS_IN_JSON_CACHE = 2;
    private static final int CLASS_NAME_INDEX = 0;
    private static final int JSON_PAYLOAD_INDEX = 1;

    @Override
    public Object unMarshallCache(String jsonCache) {
        String[] cacheElements = jsonCache.split(REGEX);
        if (cacheElements.length == NUMBER_OF_ELEMENTS_IN_JSON_CACHE) {
            try {
                JAXBContext jc = JAXBContext.newInstance(Class.forName(cacheElements[CLASS_NAME_INDEX]));
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                return unmarshaller.unmarshal(new StringReader(cacheElements[JSON_PAYLOAD_INDEX]));
            } catch (JAXBException | ClassNotFoundException e) {
                // if some unMarshalling exception occurs, then log it and shove it under the rug
                LOGGER.log(Level.WARNING, "An error occurred during the UnMarshalling of the DeviceProtocolCache: " + e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public String marshall(Object legacyCache) {
        StringWriter stringWriter = new StringWriter();
        if (legacyCache != null) {
            try {
                Class<?> cacheClass = legacyCache.getClass();
                stringWriter.append(cacheClass.getName()).append(REGEX);
                JAXBContext jc = JAXBContext.newInstance(cacheClass);
                Marshaller marshaller = jc.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.marshal(legacyCache, stringWriter);
            } catch (JAXBException e) {
                // if some marshalling exception occurs, then log it and shove it under the rug
                LOGGER.log(Level.WARNING, "An error occurred during the Marshalling of the DeviceProtocolCache: " + e.getMessage(), e);
            }
        }
        return stringWriter.toString();
    }
}
