package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link DeviceMessageService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-11 (13:33)
 */
@Component(name = "com.energyict.mdc.protocols.api", service = {DeviceMessageService.class, InstallService.class}, property = "name=" + DeviceMessageService.COMPONENT_NAME, immediate = true)
public class DeviceMessageServiceImpl implements DeviceMessageService, InstallService {

    private volatile PropertySpecService propertySpecService;
    private Thesaurus thesaurus;
    private DataModel dataModel;
    private BundleContext bundleContext;

    // For OSGi
    @SuppressWarnings("unused")
    public DeviceMessageServiceImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public DeviceMessageServiceImpl(BundleContext bundleContext, OrmService ormService,PropertySpecService propertySpecService, NlsService nlsService) {
        super();
        this.setOrmService(ormService);
        this.setPropertySpecService(propertySpecService);
        this.setNlsService(nlsService);
        this.activate(bundleContext);
        this.install();
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.dataModel.register(this.getModule());
    }

    @Deactivate
    public void deactivate(BundleContext bundleContext){
        System.out.println("Stopping the DeviceMessageService");
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(DeviceMessageService.COMPONENT_NAME, "DeviceMessages");
        this.dataModel = dataModel;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceMessageService.COMPONENT_NAME, Layer.DOMAIN);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(DeviceMessageService.class).toInstance(DeviceMessageServiceImpl.this);
            }
        };
    }

    @Override
    public void install() {
        new Installer(this.thesaurus).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("NLS");
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