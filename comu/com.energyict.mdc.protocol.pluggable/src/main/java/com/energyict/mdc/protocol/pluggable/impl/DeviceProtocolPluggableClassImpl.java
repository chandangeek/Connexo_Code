package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolNotAllowedByLicenseException;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.MeterProtocolAdapterImpl;
import com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.impl.relations.SecurityPropertySetRelationTypeSupport;
import com.google.common.base.Optional;

import java.util.List;
import javax.inject.Inject;

/**
 * Defines a PluggableClass based on a {@link DeviceProtocol}.
 * <p/>
 * We are responsible for wrapping the given Pluggable with a correct Adapter
 * ({@link com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter} or
 * {@link com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter})
 * or a correct cast to {@link DeviceProtocol}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/07/12
 * Time: 9:00
 */
@HasValidProperties(groups = { Save.Update.class, Save.Create.class })
public final class DeviceProtocolPluggableClassImpl extends PluggableClassWrapper<DeviceProtocol> implements DeviceProtocolPluggableClass {

    private PropertySpecService propertySpecService;
    private ProtocolPluggableService protocolPluggableService;
    private SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory;
    private RelationService relationService;
    private DataModel dataModel;
    private IssueService issueService;
    private DeviceCacheMarshallingService deviceCacheMarshallingService;
    private final LicenseService licenseService;
    private final LicensedProtocolService licensedProtocolService;

    @Inject
    public DeviceProtocolPluggableClassImpl(EventService eventService, PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory, RelationService relationService, DataModel dataModel, Thesaurus thesaurus, IssueService issueService, DeviceCacheMarshallingService deviceCacheMarshallingService, LicenseService licenseService, LicensedProtocolService licensedProtocolService) {
        super(eventService, thesaurus);
        this.propertySpecService = propertySpecService;
        this.protocolPluggableService = protocolPluggableService;
        this.securitySupportAdapterMappingFactory = securitySupportAdapterMappingFactory;
        this.relationService = relationService;
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.deviceCacheMarshallingService = deviceCacheMarshallingService;
        this.licenseService = licenseService;
        this.licensedProtocolService = licensedProtocolService;
    }

    static DeviceProtocolPluggableClassImpl from (DataModel dataModel, PluggableClass pluggableClass) {
        return dataModel.getInstance(DeviceProtocolPluggableClassImpl.class).initializeFrom(pluggableClass);
    }

    DeviceProtocolPluggableClassImpl initializeFrom (PluggableClass pluggableClass) {
        this.setPluggableClass(pluggableClass);
        return this;
    }

    @Override
    protected Discriminator discriminator() {
        return Discriminator.DEVICEPROTOCOL;
    }

    @Override
    public TypedProperties getProperties() {
        return super.getProperties();
    }

    @Override
    protected DeviceProtocol newInstance(PluggableClass pluggableClass) {
        Class<?> protocolClass = this.protocolPluggableService.loadProtocolClass(pluggableClass.getJavaClassName());
        DeviceProtocol deviceProtocol;
        try {
            if (DeviceProtocol.class.isAssignableFrom(protocolClass)) {
                deviceProtocol = (DeviceProtocol) protocolClass.newInstance();
            }
            else {
                // Must be a lecagy pluggable class
                deviceProtocol = this.checkForProtocolWrappers(protocolClass.newInstance());
            }
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new ProtocolCreationException(pluggableClass.getJavaClassName());
        }
        deviceProtocol.setPropertySpecService(this.propertySpecService);
        return deviceProtocol;
    }

    /**
     * Check if the specified protocol needs a Protocol adapter
     * and return the appropriate adapter to create a {@link DeviceProtocol}.
     *
     * @param protocol the instantiated protocol
     * @throws ProtocolCreationException if and only if the given protocol does not implement: <ul>
     * <li>{@link SmartMeterProtocol}</li>
     * <li>{@link MeterProtocol}</li>
     * </ul>
     */
    private DeviceProtocol checkForProtocolWrappers(Object protocol) {
        if (protocol instanceof SmartMeterProtocol) {
            return new SmartMeterProtocolAdapter((SmartMeterProtocol) protocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel, issueService, deviceCacheMarshallingService);
        }
        else if (protocol instanceof MeterProtocol) {
            return new MeterProtocolAdapterImpl((MeterProtocol) protocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel, issueService, deviceCacheMarshallingService);
        }
        else {
            throw new ProtocolCreationException(protocol.getClass());
        }
    }

    @Override
    protected void validateLicense() {
        Optional<License> licenseForApplicationMdc = this.licenseService.getLicenseForApplication("MDC");
        if(licenseForApplicationMdc.isPresent()){
            this.checkDeviceProtocolForLicense(this.getJavaClassName(), licenseForApplicationMdc.get());
        }
    }

    private void checkDeviceProtocolForLicense (String javaClassName, License license) {
        if (!this.licensedProtocolService.isValidJavaClassName(javaClassName, license)) {
            throw new ProtocolNotAllowedByLicenseException(this.getThesaurus(), javaClassName);
        }
    }

    @Override
    public void save() {
        Save.action(this.getId()).validate(dataModel, this);
        super.save();
        this.createRelationTypes();
    }

    private void createRelationTypes () {
        this.createSecurityPropertiesRelationType();
        this.createDialectRelationTypes();
    }

    private void createSecurityPropertiesRelationType () {
        DeviceProtocolSecurityRelationTypeCreator.createRelationType(this.dataModel, this.protocolPluggableService, this.relationService, this, this.propertySpecService);
    }

    private void createDialectRelationTypes () {
        for (DeviceProtocolDialect deviceProtocolDialect : this.getDeviceProtocol().getDeviceProtocolDialects()) {
            DeviceProtocolDialectUsagePluggableClass dialectUsagePluggableClass =
                    new DeviceProtocolDialectUsagePluggableClassImpl(this, deviceProtocolDialect, this.dataModel, this.relationService, this.propertySpecService);
            dialectUsagePluggableClass.findOrCreateRelationType(true);
        }
    }

    @Override
    public void delete() {
        super.delete();
        this.deleteDialectRelationTypes();
        this.deleteSecurityRelationTypes();
    }

    private void deleteDialectRelationTypes() {
        DeviceProtocolDialectUsagePluggableClassImpl deviceProtocolDialectUsagePluggableClass;
        for (DeviceProtocolDialect deviceProtocolDialect : this.newInstance().getDeviceProtocolDialects()) {
            deviceProtocolDialectUsagePluggableClass = new DeviceProtocolDialectUsagePluggableClassImpl(this, deviceProtocolDialect, this.dataModel, this.relationService, this.propertySpecService);
            deviceProtocolDialectUsagePluggableClass.deleteRelationType();
        }
    }

    private void deleteSecurityRelationTypes() {
        DeviceProtocol deviceProtocol = this.newInstance();
        SecurityPropertySetRelationTypeSupport relationTypeSupport =
                new SecurityPropertySetRelationTypeSupport(
                        this.dataModel,
                        this.protocolPluggableService,
                        this.relationService,
                        this.propertySpecService,
                        deviceProtocol,
                        this);
        relationTypeSupport.deleteRelationType();
    }

    @Override
    public PluggableClassType getPluggableClassType () {
        return PluggableClassType.DeviceProtocol;
    }

    @Override
    public TypedProperties getProperties(List<PropertySpec> propertySpecs) {
        return super.getProperties(propertySpecs);
    }

    @Override
    public String getVersion() {
        return super.getVersion();
    }

    @Override
    public DeviceProtocol getDeviceProtocol () {
        DeviceProtocol deviceProtocol = this.newInstance();
        deviceProtocol.addDeviceProtocolDialectProperties(this.getProperties(deviceProtocol.getPropertySpecs()));
        return deviceProtocol;
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.DEVICEPROTOCOL;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.DEVICEPROTOCOL;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.DEVICEPROTOCOL;
    }

}