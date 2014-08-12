package com.energyict.mdc.engine.impl.cache;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.exceptions.MessageSeeds;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Functionality to handle the {@link DeviceCache} object
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 16:32
 */
public class DeviceCacheImpl implements DeviceCache {

    private final Logger logger = Logger.getLogger(DeviceCacheImpl.class.getName());

    private static final String REGEX = ":-:-:";
    private static final int NUMBER_OF_ELEMENTS_IN_JSON_CACHE = 2;
    private static final int CLASS_NAME_INDEX = 0;
    private static final int JSON_PAYLOAD_INDEX = 1;

    private final Thesaurus thesaurus;
    private final DataModel dataModel;
    private final Clock clock;
    private final ProtocolPluggableService protocolPluggableService;
    private byte[] simpleCache;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DEVICE_IS_REQUIRED_FOR_CACHE + "}")
    private Reference<Device> device = ValueReference.absent();

    private Date modificationDate;

    @Inject
    public DeviceCacheImpl(Thesaurus thesaurus, DataModel dataModel, Clock clock, ProtocolPluggableService protocolPluggableService) {
        this.thesaurus = thesaurus;
        this.dataModel = dataModel;
        this.clock = clock;
        this.protocolPluggableService = protocolPluggableService;
    }

    public DeviceCacheImpl initialize(Device device, DeviceProtocolCache deviceProtocolCache) {
        this.device.set(device);
        setCacheObject(deviceProtocolCache);
        return this;
    }

    @Override
    public void save() {
        this.modificationDate = this.clock.now();
        Save.CREATE.save(dataModel, this);
    }

    @Override
    public void update() {
        this.modificationDate = this.clock.now();
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public void delete() {
        this.dataModel.remove(this);
    }


    @Override
    public long getDeviceId() {
        return this.device.get().getId();
    }

    @Override
    public DeviceProtocolCache getSimpleCacheObject() {
        return unMarshal(getDeflatedContent(this.simpleCache));
    }

    @Override
    public void setCacheObject(DeviceProtocolCache deviceProtocolCache) {
        this.simpleCache = getZippedContent(marshal(deviceProtocolCache));
    }

    public DeviceProtocolCache unMarshal(byte[] bytes) {
        DeviceProtocolCache deviceProtocolCache = null;
        if (bytes != null) {
            String completeCache = new String(bytes);
            String[] cacheElements = completeCache.split(REGEX);
            if (cacheElements.length == NUMBER_OF_ELEMENTS_IN_JSON_CACHE) {
                deviceProtocolCache = this.protocolPluggableService.unMarshalDeviceProtocolCache(
                        cacheElements[CLASS_NAME_INDEX],
                        cacheElements[JSON_PAYLOAD_INDEX]);
            }
        }
        return deviceProtocolCache;
    }

    public byte[] marshal(DeviceProtocolCache deviceProtocolCache) {
        String jsonCache = this.protocolPluggableService.marshalDeviceProtocolCache(deviceProtocolCache);
        return (deviceProtocolCache.getClass().getName() + REGEX + jsonCache).getBytes();
    }

    private byte[] getZippedContent(byte[] jsonCacheBytes) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gz = new GZIPOutputStream(byteArrayOutputStream, jsonCacheBytes.length)) {
                gz.write(jsonCacheBytes, 0, jsonCacheBytes.length);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return new byte[0];
    }

    private byte[] getDeflatedContent(byte[] zippedContent) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zippedContent)) {
            try (GZIPInputStream gzi = new GZIPInputStream(byteArrayInputStream)) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    int len;
                    byte[] tempBytes = new byte[4096];
                    while ((len = gzi.read(tempBytes, 0, tempBytes.length)) > 0) {
                        baos.write(tempBytes, 0, len);
                    }
                    return baos.toByteArray();
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return new byte[0];
    }
}