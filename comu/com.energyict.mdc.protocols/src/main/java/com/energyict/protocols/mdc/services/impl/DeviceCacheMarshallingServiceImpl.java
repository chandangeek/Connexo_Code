/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingException;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.NotAppropriateDeviceCacheMarshallingTargetException;

import org.osgi.service.component.annotations.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.energyict.protocols.mdc.services.impl.DeviceCacheMarshallingServiceImpl", service = DeviceCacheMarshallingService.class, immediate = true)
public class DeviceCacheMarshallingServiceImpl implements DeviceCacheMarshallingService {

    private static final Logger LOGGER = Logger.getLogger(DeviceCacheMarshallingServiceImpl.class.getName());
    private static final String REGEX = ":::";
    private static final int NUMBER_OF_ELEMENTS_IN_JSON_CACHE = 2;
    private static final int CLASS_NAME_INDEX = 0;
    private static final int JSON_PAYLOAD_INDEX = 1;

    private volatile Map<Class, JAXBContext> jaxbContextCache = new ConcurrentHashMap<>();

    @Override
    public Optional<Object> unMarshallCache(String marshalledCache) {
        String[] cacheElements = marshalledCache.split(REGEX);
        if (cacheElements.length == NUMBER_OF_ELEMENTS_IN_JSON_CACHE) {
            String className = cacheElements[CLASS_NAME_INDEX];
            String jsonPayload = cacheElements[JSON_PAYLOAD_INDEX];
            try {
                JAXBContext jc = getJaxbContext(Class.forName(className));
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                return Optional.of(unmarshaller.unmarshal(new StringReader(jsonPayload)));
            }
            catch (JAXBException e) {
                // if some unMarshalling exception occurs, then log it and shove it under the rug
                LOGGER.log(Level.SEVERE, "An error occurred during the UnMarshalling of the DeviceProtocolCache: " + e.getMessage(), e);
                throw new DeviceCacheMarshallingException("Marshalling error in JAXBContext for class " + className, e);
            }
            catch (ClassNotFoundException e) {
                // Not for one of my classes
                throw new NotAppropriateDeviceCacheMarshallingTargetException();
            }
        }
        else {
            throw new NotAppropriateDeviceCacheMarshallingTargetException();
        }
    }

    private static class WrappingException extends RuntimeException {
        public WrappingException(Throwable cause) {
            super(cause);
        }
    }

    private JAXBContext getJaxbContext(Class<?> aClass) throws JAXBException, ClassNotFoundException {

        try {
            return jaxbContextCache.computeIfAbsent(aClass, (clazz) -> {
                try {
                    return JAXBContext.newInstance(clazz);
                } catch (JAXBException e) {
                    throw new WrappingException(e);
                }
            });
        } catch (WrappingException e) {
            throw (JAXBException) e.getCause();
        }
    }

    @Override
    public String marshall(Object cache) {
        Class<?> cacheClass = cache.getClass();
        String className = cacheClass.getName();
        try {
            Class.forName(className);   // Try to load the class to check if the cache is created/managed by this bundle
            StringWriter stringWriter = new StringWriter();
            stringWriter.append(className).append(REGEX);
            JAXBContext jc = getJaxbContext(cacheClass);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(cache, stringWriter);
            return stringWriter.toString();
        }
        catch (JAXBException e) {
            // if some marshalling exception occurs, then log it and shove it under the rug
            LOGGER.log(Level.SEVERE, "An error occurred during the Marshalling of the DeviceProtocolCache: " + e.getMessage(), e);
            throw new DeviceCacheMarshallingException("Marshalling error in JAXBContext for class " + className, e);
        }
        catch (ClassNotFoundException e) {
            // Not for one of my classes
            throw new NotAppropriateDeviceCacheMarshallingTargetException();
        }
    }

}