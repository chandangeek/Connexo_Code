package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ResourceHelper {

    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public ResourceHelper(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public DeviceType findDeviceTypeByIdOrThrowException(long id) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(id);
        if (deviceType == null) {
            throw new WebApplicationException("No device type with id " + id, Response.Status.NOT_FOUND);
        }
        return deviceType;
     }

    public DeviceConfiguration findDeviceConfigurationForDeviceTypeOrThrowException(DeviceType deviceType, long deviceConfigurationId) {
        for (DeviceConfiguration deviceConfiguration : deviceType.getConfigurations()) {
            if (deviceConfiguration.getId()==deviceConfigurationId) {
                return deviceConfiguration;
            }
        }
        throw new WebApplicationException("No such device configuration for the device type", Response.status(Response.Status.NOT_FOUND).entity("No such device configuration for the device type").build());
    }

    public Set<Long> asIds(Collection<? extends HasId> collection) {
        HashSet<Long> ids = new HashSet<>();
        if (collection!=null) {
            for (HasId hasId : collection) {
                ids.add(hasId.getId());
            }
        }
        return ids;
    }

    public <T extends HasId> Difference<T> getDifference(Collection<T> existingObjects, Collection<T> desiredObjects) {
        DifferenceImpl<T> difference = new DifferenceImpl<>();
        Set<Long> existingObjectIds = asIds(existingObjects);
        difference.toBeCreated = new HashSet<>(desiredObjects);
        for (T desiredObject : desiredObjects) {
            if (existingObjectIds.contains(desiredObject.getId())) {
                difference.toBeCreated.remove(desiredObject);
            }
        }

        Set<Long> desiredObjectIds = asIds(desiredObjects);
        difference.toBeDeleted = new HashSet<>(existingObjects);
        for (T existingObject : existingObjects) {
            if (desiredObjectIds.contains(existingObject.getId())) {
                difference.toBeDeleted.remove(existingObject);
            }
        }

        return difference;
    }

    static interface Difference<T extends HasId> {
        public Set<T> toBeCreated();
        public Set<T> toBeDeleted();
    }

    class DifferenceImpl<T extends HasId> implements Difference<T> {
        Set<T> toBeCreated = new HashSet<>();
        Set<T> toBeDeleted = new HashSet<>();

        @Override
        public Set<T> toBeCreated() {
            return Collections.unmodifiableSet(toBeCreated);
        }

        @Override
        public Set<T> toBeDeleted() {
            return Collections.unmodifiableSet(toBeDeleted);
        }
    }
}
