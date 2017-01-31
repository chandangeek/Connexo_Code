/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.protocol.api.DeviceProtocolCache;
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
import java.util.Optional;

/**
 * Provides an implementation for the {@link DeviceCacheMarshallingService}
 * interface to marshall {@link DeviceProtocolCacheAdapter}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-30 (16:40)
 */
@SuppressWarnings("unused")
@Component(name = "com.energyict.mdc.protocol.pluggable.adapters.cache.DeviceCacheMarshallingService", service = DeviceCacheMarshallingService.class, immediate = true)
public class DeviceProtocolAdapterDeviceCacheMarshallingService implements DeviceCacheMarshallingService {

    private static final String REGEX = ":DPA:";
    private static final int NUMBER_OF_ELEMENTS_IN_JSON_CACHE = 2;
    private static final int CLASS_NAME_INDEX = 0;
    private static final int JSON_PAYLOAD_INDEX = 1;

    @Override
    public Optional<Object> unMarshallCache(String marshalledCache) {
        String[] cacheElements = marshalledCache.split(REGEX);
        if (cacheElements.length == NUMBER_OF_ELEMENTS_IN_JSON_CACHE) {
            String className = cacheElements[CLASS_NAME_INDEX];
            String jsonPayload = cacheElements[JSON_PAYLOAD_INDEX];
            return Optional.ofNullable(this.unMarshallDeviceProtocolCache(className, jsonPayload));
        }
        else {
            // Not marshalled by this class
            throw new NotAppropriateDeviceCacheMarshallingTargetException();
        }
    }

    private DeviceProtocolCache unMarshallDeviceProtocolCache(String className, String jsonCache) {
        try {
            JAXBContext jc = JAXBContext.newInstance(Class.forName(className));
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            return (DeviceProtocolCache) unmarshaller.unmarshal(new StringReader(jsonCache));
        }
        catch (JAXBException e) {
            throw new DeviceCacheMarshallingException("Marshalling error in JAXBContext for class " + className, e);
        }
        catch (ClassNotFoundException e) {
            /* Very unlikely because the REGEX already matched but apparently my REGEW is not unique,
             * anyways, this cache was not marshalled by this class. */
            throw new NotAppropriateDeviceCacheMarshallingTargetException();
        }
    }

    @Override
    public String marshall(Object cache) {
        if (cache instanceof DeviceProtocolCacheAdapter) {
            DeviceProtocolCacheAdapter deviceProtocolCacheAdapter = (DeviceProtocolCacheAdapter) cache;
            String marshalledCache = this.marshallDeviceProtocolCache(deviceProtocolCacheAdapter);
            return cache.getClass().getName() + REGEX + marshalledCache;
        }
        else {
            throw new NotAppropriateDeviceCacheMarshallingTargetException();
        }
    }

    private String marshallDeviceProtocolCache(DeviceProtocolCacheAdapter deviceProtocolCache) {
        try {
            StringWriter stringWriter = new StringWriter();
            JAXBContext jc = JAXBContext.newInstance(deviceProtocolCache.getClass());
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(deviceProtocolCache, stringWriter);
            return stringWriter.toString();
        }
        catch (JAXBException e) {
            throw new DeviceCacheMarshallingException("Marshalling error in JAXBContext for class " + DeviceProtocolCache.class.getName(), e);
        }
    }

}