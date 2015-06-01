package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link SearchDomain} interface
 * that supports {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (15:32)
 */
@Component(name="com.energyict.mdc.device.search", service = SearchDomain.class, immediate = true)
public class DeviceSearchDomain implements SearchDomain {

    private volatile DeviceDataModelService deviceDataModelService;
    private volatile PropertySpecService propertySpecService;
    private volatile Clock clock;

    // For OSGi purposes
    public DeviceSearchDomain() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceSearchDomain(DeviceDataModelService deviceDataModelService, PropertySpecService propertySpecService, Clock clock) {
        this();
        this.setDeviceDataModelService(deviceDataModelService);
        this.setPropertySpecService(propertySpecService);
        this.setClock(clock);
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String getId() {
        return Device.class.getName();
    }

    @Override
    public boolean supports(Class aClass) {
        return Device.class.equals(aClass);
    }

    @Override
    public List<SearchableProperty> getProperties() {
        Thesaurus thesaurus = this.deviceDataModelService.thesaurus();
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this, this.propertySpecService, thesaurus);
        return Arrays.asList(
                new MasterResourceIdentifierSearchableProperty(this, this.propertySpecService, thesaurus),
                new SerialNumberSearchableProperty(this, this.propertySpecService, thesaurus),
                deviceTypeSearchableProperty,
                new StateNameSearchableProperty(this, deviceTypeSearchableProperty, this.propertySpecService, thesaurus));
    }

    @Override
    public Finder<Device> finderFor(List<SearchablePropertyCondition> conditions) {
        return new DeviceFinder(
                new DeviceSearchSqlBuilder(this.deviceDataModelService.dataModel(), conditions, this.clock.instant()),
                this.deviceDataModelService.dataModel());
    }

}