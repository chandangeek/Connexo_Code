package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-11 (13:33)
 */
@Component(name = "com.energyict.mdc.protocols.api", service = {DeviceMessageSpecificationService.class, InstallService.class}, property = "name=" + DeviceMessageSpecificationService.COMPONENT_NAME, immediate = true)
public class DeviceMessageSpecificationServiceImpl implements DeviceMessageSpecificationService, InstallService {

    private volatile PropertySpecService propertySpecService;
    private Thesaurus thesaurus;

    // For OSGi
    @SuppressWarnings("unused")
    public DeviceMessageSpecificationServiceImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public DeviceMessageSpecificationServiceImpl(PropertySpecService propertySpecService, NlsService nlsService) {
        super();
        this.setPropertySpecService(propertySpecService);
        this.setNlsService(nlsService);
        this.install();
    }

    @Reference
    @SuppressWarnings("unused")
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceMessageSpecificationService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public void install() {
        new Installer(this.thesaurus).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("NLS", "DDC");
    }

    @Override
    public List<DeviceMessageCategory> allCategories() {
        EnumSet<DeviceMessageCategories> excluded =
                EnumSet.of(
                        DeviceMessageCategories.ACTIVITY_CALENDAR,
                        DeviceMessageCategories.FIRMWARE,
                        DeviceMessageCategories.ADVANCED_TEST,
                        DeviceMessageCategories.GENERAL,
                        DeviceMessageCategories.PRICING_INFORMATION,
                        DeviceMessageCategories.CONFIGURATION_CHANGE,
                        DeviceMessageCategories.ZIGBEE_CONFIGURATION,
                        DeviceMessageCategories.CHANNEL_CONFIGURATION,
                        DeviceMessageCategories.EIWEB_PARAMETERS);
        EnumSet<DeviceMessageCategories> included = EnumSet.complementOf(excluded);
        return included.stream().map(DeviceMessageCategoryImpl::new).collect(Collectors.toList());
    }

    @Override
    public Optional<DeviceMessageCategory> findCategoryById(int categoryId) {
        return this.allCategories().stream().filter(category -> categoryId == category.getId()).findFirst();
    }

    @Override
    public Optional<DeviceMessageSpec> findMessageSpecById(long messageSpecIdDbValue) {
        return this.allMessageSpecs().stream().filter(messageSpec -> messageSpecIdDbValue == messageSpec.getId().dbValue()).findFirst();
    }

    private List<DeviceMessageSpec> allMessageSpecs() {
        return this.allCategories().stream().flatMap(category -> category.getMessageSpecifications().stream()).collect(Collectors.toList());
    }

    private class DeviceMessageCategoryImpl implements DeviceMessageCategory {
        private final DeviceMessageCategories category;

        private DeviceMessageCategoryImpl(DeviceMessageCategories category) {
            super();
            this.category = category;
        }

        @Override
        public String getName() {
            return thesaurus.getString(this.category.getNameResourceKey(), this.category.defaultTranslation());
        }

        @Override
        public String getDescription() {
            return thesaurus.getString(this.category.getDescriptionResourceKey(), this.category.getDescriptionResourceKey());
        }

        @Override
        public int getId() {
            return this.category.ordinal();
        }

        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return this.category.getMessageSpecifications(this, propertySpecService, thesaurus);
        }

    }

}