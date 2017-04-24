/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.xml;

import com.energyict.mdc.protocol.api.DeviceProtocolCache;

import javax.xml.bind.annotation.XmlElement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

/**
 * Adapter class for {@link DeviceProtocolCache} to enable xml marshalling.
 *
 * TODO verify of the plain old ObjectOutputStreams are sufficient, it used to be different in version prior to Connexo.
 *
 * @author sva
 * @since 12/05/14 - 15:40
 */
public class DeviceProtocolCacheXmlAdaptation {

    @XmlElement
    public String base64EncodedCache;

    public DeviceProtocolCacheXmlAdaptation() {
        super();
    }

    public DeviceProtocolCacheXmlAdaptation(DeviceProtocolCache deviceProtocolCache) {
        this();
        this.base64EncodedCache = serializeCacheObject(deviceProtocolCache);
    }

    public DeviceProtocolCache unmarshallDeviceProtocolCache() {
        if (base64EncodedCache == null) {
            return null;
        } else {
            return deSerializeCacheObject(this.base64EncodedCache);
        }
    }

    private static String serializeCacheObject(DeviceProtocolCache cacheObject) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            ObjectOutputStream objectStream = new ServerObjectOutputStream(out);
            ObjectOutputStream objectStream = new ObjectOutputStream(out);
            objectStream.writeObject(cacheObject);
            return new String(Base64.getEncoder().encode(out.toByteArray()));
        } catch (IOException e) {
            return null;
        }
    }

    private static DeviceProtocolCache deSerializeCacheObject(String base64EncodedCache) {
        try {
            InputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(base64EncodedCache));
//            ObjectInputStream deSerializer = new ServerObjectInputStream(in);
            ObjectInputStream deSerializer = new ObjectInputStream(in);
            return (DeviceProtocolCache) deSerializer.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            return null;
        }
    }
}