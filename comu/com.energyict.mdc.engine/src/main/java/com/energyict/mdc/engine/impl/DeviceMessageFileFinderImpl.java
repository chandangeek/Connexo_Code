package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link DeviceMessageFileFinder} interface
 * that redirects to the {@link DeviceConfigurationService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (15:33)
 */
@Component(name = "com.energyict.mdc.upl.messages.legacy.finder", service = {DeviceMessageFileFinder.class})
@SuppressWarnings("unused")
public class DeviceMessageFileFinderImpl implements DeviceMessageFileFinder {
    private volatile DeviceConfigurationService service;

    // For OSGi purposes
    public DeviceMessageFileFinderImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceMessageFileFinderImpl(DeviceConfigurationService service) {
        this();
        this.setService(service);
    }

    @Activate
    public void activate() {
        Services.deviceMessageFileFinder(this);
    }

    @Deactivate
    public void deactivate() {
        Services.deviceMessageFileFinder(null);
    }

    @Reference
    public void setService(DeviceConfigurationService service) {
        this.service = service;
    }

    @Override
    public Optional<DeviceMessageFile> from(String identifier) {
        try {
            return this.from(Long.parseLong(identifier));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Optional<DeviceMessageFile> from(long identifier) {
        return this.service.findAllDeviceTypes()
                .stream()
                .map(DeviceType::getDeviceMessageFiles)
                .flatMap(Collection::stream)
                .filter(each -> each.getId() == identifier)
                .map(DeviceMessageFile.class::cast)
                .findAny();
    }

    @Override
    public List<? extends DeviceMessageFile> fromName(String name) {
        return this.service.findAllDeviceTypes()
                .stream()
                .map(DeviceType::getDeviceMessageFiles)
                .flatMap(Collection::stream)
                .filter(each -> each.getName().equals(name))
                .map(DeviceMessageFile.class::cast)
                .collect(Collectors.toList());
    }
}