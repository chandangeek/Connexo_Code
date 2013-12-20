package com.energyict.protocols.mdc.channels;

import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.VoidComChannel;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;
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
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec("BigDecimal"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().booleanPropertySpec("Boolean"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().dateTimePropertySpec("DateTime"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().dateTimePropertySpec("MyDate"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().ean13PropertySpec("Ean13"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().ean18PropertySpec("Ean18"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().encryptedStringPropertySpec("EncryptedString"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().hexStringPropertySpec("HexString"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().largeStringPropertySpec("LargeString"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().obisCodePropertySpec("ObisCode"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().passwordPropertySpec("Password"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().spatialCoordinatesPropertySpec("SpatialCoordinates"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().stringPropertySpec("String"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec("TimeDuration"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().timeOfDayPropertySpec("TimeOfDay"));
        //references
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().codeTableReferencePropertySpec("CodeTable"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().loadProfilePropertySpec("LoadProfile"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().loadProfileTypePropertySpec("LoadProfileType"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().lookupPropertySpec("Lookup"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().timeZoneInUseReferencePropertySpec("TimeZoneInUse"));
        propertySpecs.add(RequiredPropertySpecFactory.newInstance().userFileReferencePropertySpec("UserFile"));
    }

}