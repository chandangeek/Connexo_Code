package com.energyict.protocols.mdc.channels;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecFactory;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.protocols.mdc.protocoltasks.ConnectionTypeImpl;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused") // Pluggable
public class TestOfRequiredPropertiesConnectionType extends ConnectionTypeImpl {

    @Override
    public boolean allowsSimultaneousConnections() {
        return true;
    }

    @Override
    public boolean supportsComWindow() {
        return false;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.allOf(ComPortType.class);
    }

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        return new VoidComChannel();
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        if (name != null) {
            for (PropertySpec spec: getPropertySpecs()){
                if (name.equals(spec.getName())){
                    return spec;
                }
            }
        }
        return null;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-09-09 10:55:38 +0200 (Mon, 09 Sep 2013) $";
    }

    @Override
    protected void addPropertySpecs (List<PropertySpec> propertySpecs) {
        PropertySpecFactory requiredPropertySpecFactory = RequiredPropertySpecFactory.newInstance();
        propertySpecs.add(requiredPropertySpecFactory.bigDecimalPropertySpec("BigDecimal"));
        propertySpecs.add(requiredPropertySpecFactory.booleanPropertySpec("Boolean"));
        propertySpecs.add(requiredPropertySpecFactory.dateTimePropertySpec("DateTime"));
        propertySpecs.add(requiredPropertySpecFactory.dateTimePropertySpec("MyDate"));
        propertySpecs.add(requiredPropertySpecFactory.ean13PropertySpec("Ean13"));
        propertySpecs.add(requiredPropertySpecFactory.ean18PropertySpec("Ean18"));
        propertySpecs.add(requiredPropertySpecFactory.encryptedStringPropertySpec("EncryptedString"));
        propertySpecs.add(requiredPropertySpecFactory.hexStringPropertySpec("HexString"));
        propertySpecs.add(requiredPropertySpecFactory.largeStringPropertySpec("LargeString"));
        propertySpecs.add(requiredPropertySpecFactory.obisCodePropertySpec("ObisCode"));
        propertySpecs.add(requiredPropertySpecFactory.passwordPropertySpec("Password"));
        propertySpecs.add(requiredPropertySpecFactory.spatialCoordinatesPropertySpec("SpatialCoordinates"));
        propertySpecs.add(requiredPropertySpecFactory.stringPropertySpec("String"));
        propertySpecs.add(requiredPropertySpecFactory.timeDurationPropertySpec("TimeDuration"));
        propertySpecs.add(requiredPropertySpecFactory.timeOfDayPropertySpec("TimeOfDay"));
        //references
        propertySpecs.add(
                requiredPropertySpecFactory.
                        referencePropertySpec(
                                "CodeTable",
                                (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.CODE.id())));
        propertySpecs.add(
                requiredPropertySpecFactory.
                        referencePropertySpec(
                                "LoadProfile",
                                (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.LOADPROFILE.id())));
        propertySpecs.add(
                requiredPropertySpecFactory.
                        referencePropertySpec(
                                "LoadProfileType",
                                (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.LOADPROFILE.id())));
        propertySpecs.add(
                requiredPropertySpecFactory.
                        referencePropertySpec(
                                "Lookup",
                                (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.LOOKUP.id())));
        propertySpecs.add(requiredPropertySpecFactory.timeZonePropertySpec("TimeZoneInUse"));
        propertySpecs.add(
                requiredPropertySpecFactory.
                        referencePropertySpec(
                                "UserFile",
                                (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.USERFILE.id())));
    }

}