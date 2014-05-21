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
import com.energyict.mdc.engine.exceptions.SerializationException;
import com.google.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

/**
 * Functionality to handle the {@link DeviceCache} object
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 16:32
 */
public class  DeviceCacheImpl implements DeviceCache {

    private final Thesaurus thesaurus;
    private final DataModel dataModel;
    private final Clock clock;
    private byte[] simpleCache;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DEVICE_IS_REQUIRED_FOR_CACHE + "}")
    private Reference<Device> device = ValueReference.absent();
    private Date modificationDate;

    @Inject
    public DeviceCacheImpl(Thesaurus thesaurus, DataModel dataModel, Clock clock) {
        this.thesaurus = thesaurus;
        this.dataModel = dataModel;
        this.clock = clock;
    }

    public DeviceCacheImpl initialize(Device device, Serializable simpleCacheObject) {
        this.device.set(device);
        this.simpleCache = serialize(simpleCacheObject);
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
    public Serializable getSimpleCacheObject() {
        return deSerialize(this.simpleCache);
    }

    @Override
    public void setCacheObject(Serializable cacheObject) {
        this.simpleCache = serialize(cacheObject);
    }

    public Serializable deSerialize(byte[] bytes) {
        try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(b)){
                return (Serializable) objectInputStream.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw SerializationException.whenDeSerializingCacheObject(thesaurus, bytes, e.getMessage());
        }
    }

    public byte[] serialize(Serializable obj) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                o.writeObject(obj);
                return b.toByteArray();
            }
        } catch (IOException e) {
            throw SerializationException.whenSerializingCacheObject(thesaurus, obj, e.getMessage());
        }
    }
}