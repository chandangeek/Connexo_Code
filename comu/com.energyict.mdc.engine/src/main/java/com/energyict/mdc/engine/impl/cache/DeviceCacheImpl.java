package com.energyict.mdc.engine.impl.cache;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
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

    private static final int DEFLATION_BUFFER_SIZE = 4096;
    private final Logger logger = Logger.getLogger(DeviceCacheImpl.class.getName());

    private final DataModel dataModel;
    private final ProtocolPluggableService protocolPluggableService;
    private byte[] simpleCache;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_IS_REQUIRED_FOR_CACHE + "}")
    private Reference<Device> device = ValueReference.absent();
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    public DeviceCacheImpl(DataModel dataModel, ProtocolPluggableService protocolPluggableService) {
        this.dataModel = dataModel;
        this.protocolPluggableService = protocolPluggableService;
    }

    public DeviceCacheImpl initialize(Device device, DeviceProtocolCache deviceProtocolCache) {
        this.device.set(device);
        setCacheObject(deviceProtocolCache);
        return this;
    }

    @Override
    public void save() {
        Save.CREATE.save(dataModel, this);
    }

    @Override
    public void update() {
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
        if (bytes != null) {
            String completeCache = new String(bytes);
            return this.protocolPluggableService
                        .unMarshallDeviceProtocolCache(completeCache)
                        .map(DeviceProtocolCache.class::cast)
                        .orElse(null);
        }
        else {
            return null;
        }
    }

    public byte[] marshal(DeviceProtocolCache deviceProtocolCache) {
        return this.protocolPluggableService.marshallDeviceProtocolCache(deviceProtocolCache).getBytes();
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
                    byte[] tempBytes = new byte[DEFLATION_BUFFER_SIZE];
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