package com.energyict.mdc.channels;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.protocol.VoidComChannel;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.ConnectionTypeImpl;

import java.util.ArrayList;
import java.util.Collections;
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
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        return new VoidComChannel();
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        return null;
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return false;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> specs = new ArrayList<>();
        specs.add(PropertySpecFactory.bigDecimalPropertySpec("BigDecimal"));
        specs.add(PropertySpecFactory.booleanPropertySpec("Boolean"));
        specs.add(PropertySpecFactory.booleanPropertySpecWithoutThreeState("NoThreeStateBoolean"));
     //   specs.add(PropertySpecFactory.customCoordinatesPropertySpec("CustomCoordinates"));
        specs.add(PropertySpecFactory.dateTimePropertySpec("DateTime"));
        specs.add(PropertySpecFactory.datePropertySpec("DateProp"));
     //   specs.add(PropertySpecFactory.ean13PropertySpec("Ean13"));
     //   specs.add(PropertySpecFactory.ean18PropertySpec("Ean18"));
     //   specs.add(PropertySpecFactory.encryptedStringPropertySpec("EncryptedString"));
//        specs.add(PropertySpecFactory.hexStringPropertySpec("HexString"));
        specs.add(PropertySpecFactory.largeStringPropertySpec("LargeString"));
        specs.add(PropertySpecFactory.obisCodePropertySpec("ObisCode"));
        specs.add(PropertySpecFactory.passwordPropertySpec("Password"));
     //   specs.add(PropertySpecFactory.spatialCoordinatesPropertySpec("SpatialCoordinates"));
        specs.add(PropertySpecFactory.stringPropertySpec("String"));
        specs.add(PropertySpecFactory.timeDurationPropertySpec("TimeDuration"));
        specs.add(PropertySpecFactory.timeOfDayPropertySpec("TimeOfDay"));
        //references
        specs.add(PropertySpecFactory.codeTableReferencePropertySpec("CodeTable"));
        specs.add(PropertySpecFactory.loadProfilePropertySpec("LoadProfile"));
        specs.add(PropertySpecFactory.loadProfileTypePropertySpecByList("LoadProfileType"));
        specs.add(PropertySpecFactory.lookupPropertySpec("Lookup"));
        specs.add(PropertySpecFactory.timeZoneInUseReferencePropertySpec("TimeZoneInUse"));
        specs.add(PropertySpecFactory.userFileReferencePropertySpec("UserFile"));
        return specs;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }
}
