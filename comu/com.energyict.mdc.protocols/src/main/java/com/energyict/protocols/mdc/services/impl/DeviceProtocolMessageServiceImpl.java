package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageCategories;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.UnableToCreateProtocolInstance;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link DeviceProtocolMessageService} interface
 * and registers as a OSGi component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 16:08
 */
@Component(name = "com.energyict.mdc.service.deviceprotocolmessage", service = DeviceProtocolMessageService.class)
public class DeviceProtocolMessageServiceImpl implements DeviceProtocolMessageService {

    private static final Map<String, InstanceFactory> uplFactories = new ConcurrentHashMap<>();

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile ProtocolPluggableService protocolPluggableService;

    // For OSGi purposes
    public DeviceProtocolMessageServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceProtocolMessageServiceImpl(PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, Thesaurus thesaurus) {
        this.setPropertySpecService(propertySpecService);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setThesaurus(thesaurus);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public Set<DeviceMessageCategory> allMessageCategories() {
        return Stream
                .of(DeviceMessageCategories.values())
                .map(this::toDeviceMessageCategory)
                .collect(Collectors.toSet());
    }

    private DeviceMessageCategory toDeviceMessageCategory(DeviceMessageCategories category) {
        return new UPLDeviceMessageCategory(category, this.thesaurus, this.propertySpecService, this.protocolPluggableService);
    }

    @Override
    public Object createDeviceProtocolMessagesFor(String className) {
        try {
            return uplFactories
                    .computeIfAbsent(className, ConstructorBasedUplServiceInjection::from)
                    .newInstance();
        } catch (UnableToCreateProtocolInstance e) {
            throw DeviceProtocolAdapterCodingExceptions
                    .deviceMessageConverterClassCreationFailure(MessageSeeds.DEVICE_MESSAGE_CONVERTER_CREATION_FAILURE, e, className);
        }
    }

    private static class UPLDeviceMessageCategory implements DeviceMessageCategory {
        private final DeviceMessageCategories category;
        private final int deviceMessageCategoryId;
        private final Thesaurus thesaurus;
        private final PropertySpecService propertySpecService;
        private final ProtocolPluggableService protocolPluggableService;

        UPLDeviceMessageCategory(DeviceMessageCategories category, Thesaurus thesaurus, PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService) {
            super();
            this.category = category;
            this.thesaurus = thesaurus;
            this.propertySpecService = propertySpecService;
            this.protocolPluggableService = protocolPluggableService;
            this.deviceMessageCategoryId = this.category.ordinal();
        }

        @Override
        public String getNameResourceKey() {
            return this.category.getNameResourceKey();
        }

        @Override
        public String getName() {
            return thesaurus.getString(this.category.getNameResourceKey(), this.category.getDefaultFormat());
        }

        @Override
        public String getDescription() {
            return thesaurus.getString(this.category.getDescriptionResourceKey(), this.category.getDescriptionResourceKey());
        }

        @Override
        public int getId() {
            return deviceMessageCategoryId;
        }

        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return this.category
                    .getMessageSpecifications(this.toConnexo(), this.propertySpecService, this.thesaurus)
                    .stream()
                    .map(this.protocolPluggableService::adapt)
                    .collect(Collectors.toList());
        }

        private com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory toConnexo() {
            return new ConnexoDeviceMessageCategory(this.category, this.thesaurus, this.propertySpecService);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UPLDeviceMessageCategory)) {
                return false;
            }

            UPLDeviceMessageCategory that = (UPLDeviceMessageCategory) o;

            return deviceMessageCategoryId == that.deviceMessageCategoryId;
        }

        @Override
        public int hashCode() {
            int result = category.hashCode();
            result = 31 * result + deviceMessageCategoryId;
            return result;
        }
    }

    private static class ConnexoDeviceMessageCategory implements com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory {
        private final DeviceMessageCategories category;
        private final Thesaurus thesaurus;
        private final PropertySpecService propertySpecService;

        ConnexoDeviceMessageCategory(DeviceMessageCategories category, Thesaurus thesaurus, PropertySpecService propertySpecService) {
            this.category = category;
            this.thesaurus = thesaurus;
            this.propertySpecService = propertySpecService;
        }

        @Override
        public String getName() {
            return this.thesaurus.getString(this.category.getNameResourceKey(), this.category.getNameResourceKey());
        }

        @Override
        public String getDescription() {
            return this.thesaurus.getString(this.category.getDescriptionResourceKey(), this.category.getDescriptionResourceKey());
        }

        @Override
        public int getId() {
            return this.category.ordinal();
        }

        @Override
        public List<com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec> getMessageSpecifications() {
            return this.category.getMessageSpecifications(this, this.propertySpecService, this.thesaurus);
        }
    }

}