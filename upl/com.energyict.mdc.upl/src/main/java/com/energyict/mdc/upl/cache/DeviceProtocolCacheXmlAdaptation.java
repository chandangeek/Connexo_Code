package com.energyict.mdc.upl.cache;

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
            ObjectOutputStream objectStream = new StubbingObjectOutputStream(out);
            objectStream.writeObject(cacheObject);
            Base64.Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error when serializing the device protocol cache: " + e.getMessage());
        }
    }

    private static DeviceProtocolCache deSerializeCacheObject(String base64EncodedCache) {
        try {
            InputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(base64EncodedCache));
            ObjectInputStream deSerializer = new ServerObjectInputStream(in);
            return (DeviceProtocolCache) deSerializer.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            e.printStackTrace();
            throw new RuntimeException("Error when deserializing the device protocol cache: " + e.getMessage());
        }
    }

}