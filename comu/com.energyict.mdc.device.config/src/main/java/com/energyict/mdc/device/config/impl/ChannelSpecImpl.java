package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.cpo.PersistentNamedObject;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdw.amr.RegisterMappingFactory;
import com.energyict.mdw.amr.RegisterMappingFactoryProvider;
import com.energyict.mdw.core.AuditTrailFactoryProvider;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdw.core.DeviceCollectionMethodType;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdw.core.DeviceConfigurationFactory;
import com.energyict.mdw.core.DeviceConfigurationFactoryProvider;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdw.core.LoadProfileSpecFactory;
import com.energyict.mdw.core.LoadProfileSpecFactoryProvider;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.energyict.mdc.device.config.Phenomenon;
import com.energyict.mdw.core.PhenomenonFactory;
import com.energyict.mdw.core.PhenomenonFactoryProvider;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;
import com.energyict.mdw.core.VirtualMeterType;
import com.energyict.mdw.core.VirtualMeterTypeFactory;
import com.energyict.mdw.core.VirtualMeterTypeFactoryProvider;
import com.energyict.mdw.shadow.ChannelShadow;
import com.energyict.mdw.shadow.ChannelSpecShadow;
import com.energyict.mdw.shadow.VirtualMeterTypeFieldShadow;
import com.energyict.mdw.shadow.VirtualMeterTypeShadow;
import com.energyict.mdw.xml.ChannelSpecCommand;
import com.energyict.mdw.xml.Command;
import com.energyict.mdc.common.ObisCode;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static com.energyict.util.Equality.equalityHoldsFor;

/**
 * Copyrights EnergyICT
 * Date: 7/11/12
 * Time: 13:22
 */
public class ChannelSpecImpl extends PersistentNamedObject<ChannelSpec> implements ChannelSpec {

    private DeviceConfiguration deviceConfiguration;
    private RegisterMapping registerMapping;
    private String overruledObisCodeString;
    private ObisCode overruledObisCode;
    private int nbrOfFractionDigits;
    private BigDecimal overflow;
//    private int phenomenonId;
    private Phenomenon phenomenon;
    private ReadingMethod readingMethod;
    private MultiplierMode multiplierMode;
    private BigDecimal multiplier;
    private ValueCalculationMethod valueCalculationMethod;
    private LoadProfileSpec loadProfileSpec;
    private TimeDuration interval;
    private ProductSpec productSpec;

    ChannelSpecImpl(int id) {
        super(id);
    }

    ChannelSpecImpl(ResultSet resultSet) throws SQLException {
        doLoad(resultSet);
    }

    @Override
    public RegisterMapping getRtuRegisterMapping() {
        if (registerMapping == null) {
            registerMapping = RegisterMappingFactoryProvider.instance.get().getRegisterMappingFactory().find(rtuRegisterMappingId);
        }
        return registerMapping;
    }

    @Override
    public ObisCode getDeviceObisCode() {
        if (overruledObisCode != null) {
            return overruledObisCode;
        }
        return getObisCode();
    }

    @Override
    public ObisCode getObisCode() {
        return getRtuRegisterMapping().getObisCode();
    }

    @Override
    public int getNbrOfFractionDigits() {
        return nbrOfFractionDigits;
    }

    @Override
    public BigDecimal getOverflow() {
        return overflow;
    }

    @Override
    public Phenomenon getPhenomenon() {
        return PhenomenonFactoryProvider.instance.get().getPhenomenonFactory().find(phenomenonId);
    }

    @Override
    public ReadingMethod getReadingMethod() {
        return readingMethod;
    }

    @Override
    public MultiplierMode getMultiplierMode() {
        return multiplierMode;
    }

    @Override
    public BigDecimal getMultiplier() {
        return multiplier;
    }

    @Override
    public ValueCalculationMethod getValueCalculationMethod() {
        return valueCalculationMethod;
    }

    @Override
    public LoadProfileSpec getLoadProfileSpec() {
        if (loadProfileSpecId > 0) {
            return LoadProfileSpecFactoryProvider.instance.get().getLoadProfileSpecFactory().find(loadProfileSpecId);
        }
        return null;

    }

    @Override
    public DeviceConfiguration getDeviceConfig() {
        return DeviceConfigurationFactoryProvider.instance.get().getDeviceConfigurationFactory().find(deviceConfigId);
    }

    @Override
    public TimeDuration getInterval() {
        return (getLoadProfileSpec() != null ? getLoadProfileSpec().getInterval() : interval);
    }


    @Override
    public int getLoadProfileSpecId() {
        return loadProfileSpecId;
    }

    @Override
    public int getRegisterMappingId() {
        return rtuRegisterMappingId;
    }

    @Override
    public ChannelSpecShadow getShadow() {
        return new ChannelSpecShadow(this);
    }

    @Override
    public void update(final ChannelSpecShadow shadow) throws BusinessException, SQLException {
        execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws BusinessException, SQLException {
                doUpdate(shadow);
                return null;
            }
        });

    }

    @Override
    public ChannelShadow newChannelShadow() {
        ChannelShadow shadow = new ChannelShadow();
        shadow.setName(getName());
        shadow.setChannelSpecId(getId());
        if (loadProfileSpecId == 0) {
            shadow.setInterval(getInterval());
        } else {
            shadow.setInterval(getLoadProfileSpec().getLoadProfileType().getInterval());
        }
        return shadow;
    }

    private void doUpdate(ChannelSpecShadow shadow) throws BusinessException, SQLException {
        validateUpdate(shadow);
        String fieldToUpdate = getName();
        boolean intervalChange = !(Equality.equalityHoldsFor(this.getInterval()).and(shadow.getInterval()));
        boolean nameChange = !(Equality.equalityHoldsFor(this.getName())).and(shadow.getName());
        copy(shadow);
        post();
        if (intervalChange) {
            updateChannelIntervals();
        }
        if (nameChange) {
            updateChannelName(fieldToUpdate);
        }
        updateLPSpecVMType(fieldToUpdate);
        updated();
    }

    private void updateChannelName(String oldName) throws SQLException {
        EndDeviceChannelFactory endDeviceChannelFactory = ServerChannelFactoryProvider.instance.get().getServerChannelFactory();
        endDeviceChannelFactory.updateChannelNames(this, oldName);
    }

    private void updateChannelIntervals() throws SQLException {
        EndDeviceChannelFactory endDeviceChannelFactory = ServerChannelFactoryProvider.instance.get().getServerChannelFactory();
        endDeviceChannelFactory.updateChannelIntervals(this);
    }

    private void validateUpdate(ChannelSpecShadow shadow) throws BusinessException {
        validate(shadow);
        if (shadow.getDeviceConfigId() != this.deviceConfigId) {
            throw new BusinessException("cannotUpdateDeviceConfigOfChannelSpec", "Cannot update Device config");
        }
        validateUniqueness(shadow.getName(), shadow.getDeviceConfigId(), this.getId());
        DeviceConfiguration deviceConfig = getDeviceConfig();
        if (deviceConfig.getActive()) {
            if (shadow.getRtuRegisterMappingId() != this.rtuRegisterMappingId) {
                throw new BusinessException("cannotUpdateRegisterMappingBecauseDeviceConfigXIsActive", "Cannot update Register mapping because device config {0} is active", deviceConfig.getName());
            }
            if (shadow.getLoadProfileTypeId() > 0) {
                LoadProfileSpec spec = getLoadProfileSpecForType(deviceConfig, shadow.getLoadProfileTypeId());
                if (spec == null) {
                    throw new BusinessException("noLoadProfileSpecOfType", "Cannot find Load profile spec for LoadProfileType {0}", shadow.getLoadProfileTypeId());
                }
                if (spec.getId() != this.loadProfileSpecId) {
                    throw new BusinessException("cannotUpdateLoadProfileSpecBecauseDeviceConfigXIsActive", "Cannot update Load profile spec because device config '{0}' is active", deviceConfig.getName());
                }
            } else {
                if (shadow.getLoadProfileTypeId() != this.loadProfileSpecId) {
                    throw new BusinessException("cannotUpdateLoadProfileSpecBecauseDeviceConfigXIsActive", "Cannot update Load profile spec because device config '{0}' is active", deviceConfig.getName());
                }
            }
        }
    }

    private LoadProfileSpec getLoadProfileSpecForType(DeviceConfiguration deviceConfig, int loadProfileTypeId) {
        for (LoadProfileSpec loadProfileSpec : deviceConfig.getLoadProfileSpecs()) {
            if (loadProfileSpec.getLoadProfileType().getId() == loadProfileTypeId) {
                return loadProfileSpec;
            }
        }
        return null;
    }

    @Override
    protected void postNew() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void post() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void doDelete() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void validateDelete() throws SQLException, BusinessException {
        super.validateDelete();
        if (getDeviceConfig().getActive()) {
            throw new BusinessException("cannotDeleteChannelSpecDeviceConfigIsActive", "Cannot delete Channel spec because its device config is still active");
        }
    }

    @Override
    protected String[] getColumns() {
        return ChannelSpecFactoryImpl.COLUMNS;
    }

    @Override
    protected String getTableName() {
        return ChannelSpecFactoryImpl.TABLE_NAME;
    }

    @Override
    protected void doLoad(ResultSet resultSet) throws SQLException {
        super.doLoad(resultSet);
        deviceConfigId = resultSet.getInt(3);
        rtuRegisterMappingId = resultSet.getInt(4);
        String obisCodeString = resultSet.getString(5);
        if (resultSet.wasNull()) {
            overruledObisCode = null;
        } else {
            overruledObisCode = ObisCode.fromString(obisCodeString);
        }
        nbrOfFractionDigits = resultSet.getInt(6);
        overflow = resultSet.getBigDecimal(7);
        phenomenonId = resultSet.getInt(8);
        readingMethod = ReadingMethod.fromDb(resultSet.getInt(9));
        try {
            multiplierMode = MultiplierMode.fromDb(resultSet.getInt(10));
        } catch (BusinessException e) {
            multiplierMode = MultiplierMode.CONFIGURED_ON_OBJECT;
        }
        multiplier = resultSet.getBigDecimal(11);
        valueCalculationMethod = ValueCalculationMethod.fromDb(resultSet.getInt(12));
        loadProfileSpecId = resultSet.getInt(13);
        if (loadProfileSpecId != 0) {
            interval = null;     // in case the channel is assigned to a loadprofile
        } else {
            interval = new TimeDuration(resultSet.getInt(14), resultSet.getInt(15));
        }
    }

    @Override
    protected int bindBody(PreparedStatement preparedStatement, int offset) throws SQLException {
        preparedStatement.setInt(offset++, deviceConfigId);
        preparedStatement.setInt(offset++, rtuRegisterMappingId);
        if (overruledObisCode != null) {
            preparedStatement.setString(offset++, overruledObisCode.toString());
        } else {
            preparedStatement.setNull(offset++, Types.VARCHAR);
        }
        preparedStatement.setInt(offset++, nbrOfFractionDigits);
        preparedStatement.setBigDecimal(offset++, overflow);
        preparedStatement.setInt(offset++, phenomenonId);
        preparedStatement.setInt(offset++, readingMethod.getCode());
        preparedStatement.setInt(offset++, multiplierMode.getCode());
        preparedStatement.setBigDecimal(offset++, multiplier);
        preparedStatement.setInt(offset++, valueCalculationMethod.getCode());
        if (loadProfileSpecId > 0) {
            preparedStatement.setInt(offset++, loadProfileSpecId);
        } else {
            preparedStatement.setNull(offset++, Types.INTEGER);
        }
        if (loadProfileSpecId != 0) {
            preparedStatement.setInt(offset++, 0);
            preparedStatement.setInt(offset++, TimeDuration.SECONDS);
        } else {
            preparedStatement.setInt(offset++, interval.getCount());
            preparedStatement.setInt(offset++, interval.getTimeUnitCode());
        }

        return offset;
    }

    public void init(final ChannelSpecShadow shadow) throws BusinessException, SQLException {
        execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws BusinessException, SQLException {
                doInit(shadow);
                return null;
            }
        });
    }

    private void doInit(ChannelSpecShadow shadow) throws SQLException, BusinessException {
        validateNew(shadow);
        copy(shadow);
        postNew();
        created();
        addAsFieldOnLPSpecVMType();
    }

    @Override
    protected void deleteDependents() throws SQLException, BusinessException {
        super.deleteDependents();
        deleteLPSpecVmTypField(getName());
    }

    private void deleteLPSpecVmTypField(String name) throws BusinessException, SQLException {
        if (getLoadProfileSpec() != null) {
            VirtualMeterTypeShadow virtualMeterTypeShadow = getLoadProfileSpec().getVirtualMeterType().getShadow();
            VirtualMeterTypeFieldShadow virtualMeterTypeFieldShadow = getTypeFieldShadow(virtualMeterTypeShadow.getFieldShadows(), getCompliantName(name));
            if (virtualMeterTypeFieldShadow != null) {
                virtualMeterTypeShadow.getFieldShadows().remove(virtualMeterTypeFieldShadow);
                getLoadProfileSpec().getVirtualMeterType().update(virtualMeterTypeShadow);
            }
        }
    }

    private void addAsFieldOnLPSpecVMType() throws BusinessException, SQLException {
        if (getLoadProfileSpec() != null) {
            VirtualMeterTypeShadow virtualMeterTypeShadow = getLoadProfileSpec().getVirtualMeterType().getShadow();
            VirtualMeterTypeFieldShadow virtualMeterTypeFieldShadow = createFieldShadow(getCompliantName(getName()));
            virtualMeterTypeShadow.getFieldShadows().add(virtualMeterTypeFieldShadow);
            try {
                getLoadProfileSpec().getVirtualMeterType().update(virtualMeterTypeShadow);
            } catch (BusinessException | SQLException e) {
                getVirtualMeterTypeFactory().clearFromCache(getLoadProfileSpec().getVirtualMeterType());
                throw e;
            }
        }
    }

    private VirtualMeterTypeFactory getVirtualMeterTypeFactory() {
        return VirtualMeterTypeFactoryProvider.instance.get().getVirtualMeterTypeFactory();
    }

    private VirtualMeterTypeFieldShadow createFieldShadow(String name) {
        VirtualMeterTypeFieldShadow virtualMeterTypeFieldShadow = new VirtualMeterTypeFieldShadow();
        virtualMeterTypeFieldShadow.setName(name);
        return virtualMeterTypeFieldShadow;
    }

    // Package private so LoadProfile can also access this
    static String getCompliantName(String channelSpecName) {
        String compl = "ch_" + channelSpecName.replaceAll(" ", "_").replaceAll("-", "min").replaceAll("\\+", "plus").replaceAll("[^ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_]", "");
        if (compl.length() > 30) {
            compl = compl.substring(0, 30);
        }
        return compl.toLowerCase();
    }

    private void updateLPSpecVMType(String fieldToUpdate) throws BusinessException, SQLException {
        if (getLoadProfileSpec() != null) {
            VirtualMeterType virtualMeterType = getLoadProfileSpec().getVirtualMeterType();
            VirtualMeterTypeShadow virtualMeterTypeShadow = virtualMeterType.getShadow();
            List<VirtualMeterTypeFieldShadow> virtualMeterTypeFieldShadows = virtualMeterTypeShadow.getFieldShadows();
            VirtualMeterTypeFieldShadow fieldShadow = getTypeFieldShadow(virtualMeterTypeFieldShadows, getCompliantName(fieldToUpdate));
            if (fieldShadow == null) {
                fieldShadow = createFieldShadow(getCompliantName(fieldToUpdate));
                virtualMeterTypeFieldShadows.add(fieldShadow);
            }
            fieldShadow.setName(getCompliantName(getShadow().getName()));
            virtualMeterTypeShadow.setFieldShadows(virtualMeterTypeFieldShadows);
            virtualMeterType.update(virtualMeterTypeShadow);
            String compliantFieldName = getCompliantName(fieldToUpdate);
        }
    }

    private VirtualMeterTypeFieldShadow getTypeFieldShadow(List<VirtualMeterTypeFieldShadow> virtualMeterTypeFieldShadows, String name) {
        if (virtualMeterTypeFieldShadows != null && virtualMeterTypeFieldShadows.size() > 0) {
            for (VirtualMeterTypeFieldShadow fieldShadow : virtualMeterTypeFieldShadows) {
                if (fieldShadow.getName().equals(name)) {
                    return fieldShadow;
                }
            }
        }
        return null;
    }


    private void validateNew(ChannelSpecShadow shadow) throws BusinessException {
        validate(shadow);
        DeviceConfiguration deviceConfig = getDeviceConfigFactory().find(shadow.getDeviceConfigId());
        if (deviceConfig.getActive()) {
            throw new BusinessException("cannotAddChannelSpecDeviceConfigXIsActive", "Cannot add a channel spec because device config '{0}' is active", deviceConfig.getName());
        }
        validateUniqueness(shadow.getName(), shadow.getDeviceConfigId());
    }

    private void validate(ChannelSpecShadow shadow) throws BusinessException {
        validate(shadow.getName());
        if (shadow.getDeviceConfigId() <= 0) {
            throw new BusinessException("deviceConfigCannotBeNull", "Device Config cannot be null");
        }
        DeviceConfiguration deviceConfig = getDeviceConfigFactory().find(shadow.getDeviceConfigId());
        if (deviceConfig == null) {
            throw new BusinessException("deviceConfigXDoesNotExist", "A device configuration with id {0,number} does not exist", shadow.getDeviceConfigId());
        }
        if (shadow.getRtuRegisterMappingId() <= 0) {
            throw new BusinessException("rtuRegisterMappingCannotBeNull", "Register Mapping cannot be null");
        }
        RegisterMapping rtuRegisterMapping = getRtuRegisterMappingFactory().find(shadow.getRtuRegisterMappingId());
        if (rtuRegisterMapping == null) {
            throw new BusinessException("rtuRegisterMappingXDoesNotExist", "A register mapping with id {0,number} does not exist", shadow.getRtuRegisterMappingId());
        }
        DeviceType deviceType = deviceConfig.getDeviceType();
        if (!DeviceCollectionMethodType.COMSERVER.equals(deviceType.getDeviceCollectionMethodType()) &&
                shadow.getLoadProfileTypeId() == 0 &&
                !getServerRtuTypeFactory().findRtuRegisterMappingIds(deviceType).contains(shadow.getRtuRegisterMappingId())) {
            throw new BusinessException("cannotAddChannelSpecForMappingXBecauseRtuTypeYDoesNotHaveMapping",
                    "Cannot add channel spec for mapping '{0}' because device type '{1}' doesn't have mapping '{0}'",
                    rtuRegisterMapping.getName(), deviceType.getName());
        }
        if (shadow.getPhenomenonId() < 0) {
            throw new BusinessException("phenomenonCannotBeNull", "Phenomenon cannot be null");
        }
        Phenomenon phenomenon = getPhenomenonFactory().find(shadow.getPhenomenonId());
        if (phenomenon == null) {
            throw new BusinessException("phenomenonXDoesNotExists", "Phenomenon with id '{0}' does not exists", shadow.getPhenomenonId());
        }
        Unit rtuRegisterMappingUnit = rtuRegisterMapping.getUnit();
        if (!phenomenon.isUndefined() && !rtuRegisterMappingUnit.isUndefined()) {
            if (!phenomenon.getUnit().equalBaseUnit(rtuRegisterMappingUnit)) {
                throw new BusinessException("nonMatchingPhenomenon", "The phenomenon of the channel (\"{0}\") doesn't match the product specification of the device register mapping (\"{1}\").",
                        phenomenon, rtuRegisterMapping.getProductSpec().getPhenomenon());
            }
        }

        if (shadow.getReadingMethod() == null) {
            throw new BusinessException("readingMethodCannotBeNull", "Reading Method cannot be null");
        }
        if (shadow.getMultiplierMode() == null) {
            throw new BusinessException("multiplierModeCannotBeNull", "Multiplier mode cannot be null");
        }
        if (shadow.getValueCalculationMethod() == null) {
            throw new BusinessException("valueCalculationMethodCannotBeNull", "Value calculation method cannot be null");
        }
        if (MultiplierMode.CONFIGURED_ON_OBJECT.equals(shadow.getMultiplierMode()) && shadow.getMultiplier() == null) {
            throw new BusinessException("multiplierCannotBeNullForModeX", "Multiplier cannot be null for multiplier mode {0}", shadow.getMultiplierMode());
        }

        LoadProfileSpec loadProfileSpec = null;
        if (shadow.getLoadProfileTypeId() != 0) {
            loadProfileSpec = getLoadProfileSpecForType(deviceConfig, shadow.getLoadProfileTypeId());
            if (loadProfileSpec == null) {
                throw new BusinessException("noLoadProfileSpecOfType", "Cannot find Load profile spec for LoadProfileType {0}", shadow.getLoadProfileTypeId());
            }
            LoadProfileType loadProfileType = loadProfileSpec.getLoadProfileType();
            if (!(((ServerLoadProfileType) loadProfileType).hasMapping(shadow.getRtuRegisterMappingId()))) {
                throw new BusinessException("rtuRegisterMappingXCannotBeUsedForLoadProfileTypeY",
                        "Device register mapping '{0}' cannot be used in a load profile specification of type '{1}'",
                        rtuRegisterMapping.getName(), loadProfileType.getName());
            }
            if (((ServerLoadProfileSpec) loadProfileSpec).hasChannelSpecForMappingExcluding(shadow.getRtuRegisterMappingId(), this.getId())) {
                throw new BusinessException("cannotMapMultipleChannelSpecsToLoadProfileSpecXForMappingY",
                        "Cannot map multiple channel specifications to a load profile specification of type '{0}' for register mapping '{1}'",
                        loadProfileType.getName(), rtuRegisterMapping.getName());
            }
        } else {
            if (shadow.getInterval() == null) {
                throw new BusinessException("intervalCantBeEmpty", "The interval cannot be undefined.");
            }
            if (shadow.getInterval() == null || shadow.getInterval().getCount() <= 0) {
                throw new BusinessException("invalidChannelInterval", "\"{0}\" is not a valid value for channel interval.", shadow.getInterval());
            }
            if ((shadow.getInterval().getTimeUnitCode() == TimeDuration.DAYS || shadow.getInterval().getTimeUnitCode() == TimeDuration.MONTHS || shadow.getInterval().getTimeUnitCode() == TimeDuration.YEARS)
                    && (shadow.getInterval().getCount() != 1)) {
                throw new BusinessException("channelIntervalIllegal", "If unit of interval is greater than hours, count must be 1");
            }
            if ((shadow.getInterval().getTimeUnitCode() == TimeDuration.WEEKS)) {
                throw new BusinessException("channelIntervalInWeeks", "Interval expressed in weeks is not supported");
            }
        }
        validateForCollectionMethod(loadProfileSpec, deviceType.getDeviceCollectionMethodType(), shadow.getName());
    }

    private void validateForCollectionMethod(LoadProfileSpec spec, DeviceCollectionMethodType deviceCollectionMethodType, String name) throws BusinessException {
        if (DeviceCollectionMethodType.COMSERVER.equals(deviceCollectionMethodType)) {
            if (spec == null) {
                throw new BusinessException("loadProfileSpecForChannelXCannotBeNull",
                        "Channel '{0}' cannot have an undefined load profile specification when the collection method of its device type = ComServer", name);
            }
        }
    }

    private ServerDeviceTypeFactory getServerRtuTypeFactory() {
        return ServerDeviceTypeFactoryProvider.instance.get().getServerDeviceTypeFactory();
    }

    private LoadProfileSpecFactory getLoadProfileSpecFactory() {
        return LoadProfileSpecFactoryProvider.instance.get().getLoadProfileSpecFactory();
    }

    private void validateUniqueness(String name, int deviceConfigId) throws BusinessException {
        validateUniqueness(name, deviceConfigId, -1);
    }

    private void validateUniqueness(String name, int deviceConfigId, int excludeId) throws BusinessException {
        if (getServerChannelSpecFactory().hasFor(deviceConfigId, name, excludeId, false)) {
            throw new BusinessException("nameMustBeUniquePerDeviceConfig", "Name of channel spec must be unique per Device Config");
        }
        if (name.length() > 27) {
            if (getServerChannelSpecFactory().hasFor(deviceConfigId, name.substring(0, 27), excludeId, true)) {
                throw new BusinessException("firstXCharactersOfNameMustBeUniquePerDeviceConfig", "First {0} characters of the name of channel spec must be unique per Device Config", 27);
            }
        }
    }

    @Override
    public void validateFor(DeviceCollectionMethodType collectionMethodType) throws BusinessException {
        validateForCollectionMethod(getLoadProfileSpec(), collectionMethodType, getName());
    }

    private ServerChannelSpecFactory getServerChannelSpecFactory() {
        return ServerChannelSpecFactoryProvider.instance.get().getServerChannelSpecFactory();
    }

    private PhenomenonFactory getPhenomenonFactory() {
        return PhenomenonFactoryProvider.instance.get().getPhenomenonFactory();
    }

    private RegisterMappingFactory getRtuRegisterMappingFactory() {
        return RegisterMappingFactoryProvider.instance.get().getRegisterMappingFactory();
    }

    private DeviceConfigurationFactory getDeviceConfigFactory() {
        return DeviceConfigurationFactoryProvider.instance.get().getDeviceConfigurationFactory();
    }

    private void copy(ChannelSpecShadow shadow) {
        setName(shadow.getName());
        deviceConfigId = shadow.getDeviceConfigId();
        rtuRegisterMappingId = shadow.getRtuRegisterMappingId();
        overruledObisCode = shadow.getOverruledObisCode();
        nbrOfFractionDigits = shadow.getNumberOfFractionDigits();
        overflow = shadow.getOverflow();
        phenomenonId = shadow.getPhenomenonId();
        readingMethod = shadow.getReadingMethod();
        multiplierMode = shadow.getMultiplierMode();
        if (MultiplierMode.CONFIGURED_ON_OBJECT.equals(multiplierMode)) {
            multiplier = shadow.getMultiplier();
        } else {
            multiplier = BigDecimal.ONE;
        }
        valueCalculationMethod = shadow.getValueCalculationMethod();
        loadProfileSpecId = (shadow.getLoadProfileTypeId() == 0 ? 0 : getLoadProfileSpecForType(getDeviceConfig(), shadow.getLoadProfileTypeId()).getId());
        interval = shadow.getInterval();
        registerMapping = null;
    }

    @Override
    protected void doUpdateAuditInfo(char action) throws SQLException, BusinessException {
        AuditTrailFactoryProvider.instance.get().getAuditTrailFactory().create(getShadow(), MeteringWarehouse.FACTORYID_CHANNELSPEC, action);
    }

    @Override
    public Command<ChannelSpec> createConstructor() {
        return new ChannelSpecCommand(this);
    }

    @Override
    public boolean isExportAllowed() {
        return getId() != 0;
    }

    @Override
    public String toString() {
        return getDeviceConfig().getDeviceType() + getNameSeparator() + getDeviceConfig() + getNameSeparator() + getName();
    }

    protected String getInvalidCharacters() {
        return "./";
    }

    @Override
    protected NameIsRequiredException nameIsRequiredException(Thesaurus thesaurus) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
