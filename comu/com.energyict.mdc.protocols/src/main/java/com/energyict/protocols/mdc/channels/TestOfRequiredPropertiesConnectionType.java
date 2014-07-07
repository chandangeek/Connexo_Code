package com.energyict.protocols.mdc.channels;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.BigDecimalFactory;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.Ean13Factory;
import com.energyict.mdc.dynamic.Ean18Factory;
import com.energyict.mdc.dynamic.EncryptedStringFactory;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.LargeStringFactory;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PasswordFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.SpatialCoordinatesFactory;
import com.energyict.mdc.dynamic.StringFactory;
import com.energyict.mdc.dynamic.ThreeStateFactory;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.dynamic.TimeOfDayFactory;
import com.energyict.mdc.dynamic.TimeZoneFactory;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

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
    public Direction getDirection() {
        return Direction.OUTBOUND;
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
        PropertySpecService propertySpecService = PropertySpecService.INSTANCE.get();
        propertySpecs.add(propertySpecService.basicPropertySpec("BigDecimal", true, new BigDecimalFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("Boolean", true, new ThreeStateFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("DateTime", true, new DateAndTimeFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("MyDate", true, new DateAndTimeFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("Ean13", true, new Ean13Factory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("Ean18", true, new Ean18Factory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("EncryptedString", true, new EncryptedStringFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("HexString", true, new HexStringFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("LargeString", true, new LargeStringFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("ObisCode", true, new ObisCodeValueFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("Password", true, new PasswordFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("SpatialCoordinates", true, new SpatialCoordinatesFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("String", true, new StringFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("TimeDuration", true, new TimeDurationValueFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("TimeOfDay", true, new TimeOfDayFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec("TimeZoneInUse", true, new TimeZoneFactory()));
        //references
        propertySpecs.add(propertySpecService.referencePropertySpec("CodeTable", true, FactoryIds.CODE));
        propertySpecs.add(propertySpecService.referencePropertySpec("LoadProfile", true, FactoryIds.LOADPROFILE));
        propertySpecs.add(propertySpecService.referencePropertySpec("LoadProfileType", true, FactoryIds.LOADPROFILE_TYPE));
        propertySpecs.add(propertySpecService.referencePropertySpec("UserFile", true, FactoryIds.USERFILE));
    }

}