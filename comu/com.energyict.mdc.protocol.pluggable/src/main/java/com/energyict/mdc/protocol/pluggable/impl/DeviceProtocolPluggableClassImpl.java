package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.license.License;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.protocol.pluggable.LicenseServer;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapterImpl;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.impl.relations.SecurityPropertySetRelationTypeSupport;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

/**
 * Defines a PluggableClass based on a {@link DeviceProtocol}.
 * <p/>
 * We are responsible for wrapping the given Pluggable with a correct Adapter
 * ({@link com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter} or
 * {@link com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolAdapter})
 * or a correct cast to {@link DeviceProtocol}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/07/12
 * Time: 9:00
 */
public final class DeviceProtocolPluggableClassImpl extends PluggableClassWrapper<DeviceProtocol> implements DeviceProtocolPluggableClass {

    private PropertySpecService propertySpecService;
    private ProtocolPluggableService protocolPluggableService;
    private SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory;
    private RelationService relationService;
    private DataModel dataModel;

    @Inject
    public DeviceProtocolPluggableClassImpl(PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory, RelationService relationService, DataModel dataModel) {
        super();
        this.propertySpecService = propertySpecService;
        this.protocolPluggableService = protocolPluggableService;
        this.securitySupportAdapterMappingFactory = securitySupportAdapterMappingFactory;
        this.relationService = relationService;
        this.dataModel = dataModel;
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
    protected DeviceProtocol newInstance(PluggableClass pluggableClass) {
        Class protocolClass = this.protocolPluggableService.loadProtocolClass(pluggableClass.getJavaClassName());
        DeviceProtocol deviceProtocol;
        try {
            if (DeviceProtocol.class.isAssignableFrom(protocolClass)) {
                deviceProtocol = (DeviceProtocol) protocolClass.newInstance();
                return deviceProtocol;
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
            return new SmartMeterProtocolAdapter((SmartMeterProtocol) protocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);
        }
        else if (protocol instanceof MeterProtocol) {
            return new MeterProtocolAdapterImpl((MeterProtocol) protocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);
        }
        else {
            throw new ProtocolCreationException(protocol.getClass());
        }
    }

    @Override
    protected void validateLicense() throws BusinessException {
        License license = LicenseServer.licenseHolder.get();
        this.checkDeviceProtocolForLicense(this.getJavaClassName(), license);
    }

    private void checkDeviceProtocolForLicense (String javaClassName, License license) throws BusinessException {
        if (!license.hasAllProtocols() && !license.hasProtocol(javaClassName)) {
            throw new BusinessException("protocolXNotAllowedByLicense", "Protocol '{0}' is not licensed", javaClassName);
        }
    }

    @Override
    public void save() throws BusinessException, SQLException {
        super.save();
        this.createRelationTypes();
    }

    private void createRelationTypes () throws SQLException, BusinessException {
        this.createSecurityPropertiesRelationType();
        this.createDialectRelationTypes();
    }

    private void createSecurityPropertiesRelationType () throws BusinessException, SQLException {
        DeviceProtocolSecurityRelationTypeCreator.createRelationType(this.dataModel, this.protocolPluggableService, this.relationService, this);
    }

    private void createDialectRelationTypes () throws BusinessException, SQLException {
        for (DeviceProtocolDialect deviceProtocolDialect : this.getDeviceProtocol().getDeviceProtocolDialects()) {
            DeviceProtocolDialectUsagePluggableClass dialectUsagePluggableClass =
                    new DeviceProtocolDialectUsagePluggableClassImpl(this, deviceProtocolDialect, this.dataModel, this.relationService);
            dialectUsagePluggableClass.findOrCreateRelationType(true);
        }
    }

    @Override
    public void delete() throws BusinessException, SQLException {
        super.delete();
        this.deleteDialectRelationTypes();
        this.deleteSecurityRelationTypes();
    }

    private void deleteDialectRelationTypes() throws BusinessException, SQLException {
        DeviceProtocolDialectUsagePluggableClassImpl deviceProtocolDialectUsagePluggableClass;
        for (DeviceProtocolDialect deviceProtocolDialect : this.newInstance().getDeviceProtocolDialects()) {
            deviceProtocolDialectUsagePluggableClass = new DeviceProtocolDialectUsagePluggableClassImpl(this, deviceProtocolDialect, this.dataModel, this.relationService);
            deviceProtocolDialectUsagePluggableClass.deleteRelationType();
        }
    }

    private void deleteSecurityRelationTypes() throws SQLException, BusinessException {
        DeviceProtocol deviceProtocol = this.newInstance();
        SecurityPropertySetRelationTypeSupport relationTypeSupport =
                new SecurityPropertySetRelationTypeSupport(
                        this.dataModel,
                        this.protocolPluggableService,
                        this.relationService,
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
    public DeviceProtocol getDeviceProtocol () {
        DeviceProtocol deviceProtocol = this.newInstance();
        deviceProtocol.addDeviceProtocolDialectProperties(this.getProperties(deviceProtocol.getPropertySpecs()));
        return deviceProtocol;
    }

    @Override
    public void notifyDelete() {
//        if (ManagerFactory.getCurrent().getMdwInterface().getDeviceTypeFactory().existsWithDeviceProtocol(this)) {
//            throw new BusinessException(
//                    "deviceProtocolXIsStillUsedByDeviceType",
//                    "The device protocol pluggable class {0} is still in use by the at least one device type",
//                    this.getName());
//        }
        // Todo: throw event that will allow the DeviceType factory to check if this protocol is still used or not
        //       until then, this method is marked as unsupported
        throw new UnsupportedOperationException("DeviceProtocolPluggableClassImpl#notifyDelete");
    }

}