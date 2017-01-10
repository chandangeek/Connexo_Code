package com.energyict.mdc.channels;

import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.ConnectionTypeImpl;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.NumberLookup;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.StringLookup;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.HexStringPropertySpec;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class TestOfRequiredPropertiesConnectionType extends ConnectionTypeImpl {

    private final PropertySpecService propertySpecService;

    public TestOfRequiredPropertiesConnectionType(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        return new VoidComChannel();
    }

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
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory
                        .specBuilder("BigDecimal", true, this.propertySpecService::bigDecimalSpec)
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("Boolean", true, this.propertySpecService::booleanSpec)
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("DateTime", false, this.propertySpecService::dateTimeSpec)
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("MyDate", false, this.propertySpecService::dateSpec)
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("EncryptedString", true, this.propertySpecService::encryptedStringSpec)
                        .finish(),
                new HexStringPropertySpec("HexString", true),
                UPLPropertySpecFactory
                        .specBuilder("ObisCode", false, this.propertySpecService::obisCodeSpec)
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("Password", false, this.propertySpecService::passwordSpec)
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("String", true, this.propertySpecService::stringSpec)
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("TimeDuration", false, this.propertySpecService::temporalAmountSpec)
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("TimeOfDay", false, this.propertySpecService::timeSpec)
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("CodeTable", false, () -> this.propertySpecService.referenceSpec(TariffCalendar.class.getName()))
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("LoadProfile", false, () -> this.propertySpecService.referenceSpec(LoadProfile.class.getName()))
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("NumberLookup", false, () -> this.propertySpecService.referenceSpec(NumberLookup.class.getName()))
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("StringLookup", false, () -> this.propertySpecService.referenceSpec(StringLookup.class.getName()))
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("TimeZoneInUse", false, this.propertySpecService::timeZoneSpec)
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder("UserFile", false, () -> this.propertySpecService.referenceSpec(DeviceMessageFile.class.getName()))
                        .finish());
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

}