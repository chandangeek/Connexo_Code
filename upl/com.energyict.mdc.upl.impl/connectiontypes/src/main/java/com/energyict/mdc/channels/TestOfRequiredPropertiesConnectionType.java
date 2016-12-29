package com.energyict.mdc.channels;

import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.ConnectionTypeImpl;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.NumberLookup;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.cpo.MdwToUplPropertySpecAdapter;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused") // Pluggable
public class TestOfRequiredPropertiesConnectionType extends ConnectionTypeImpl {

    private static final PropertySpec BIG_DECIMAL_PROPERTY_SPEC = UPLPropertySpecFactory.bigDecimal("BigDecimal", true);
    private static final PropertySpec BOOLEAN_PROPERTY_SPEC = UPLPropertySpecFactory.booleanValue("Boolean", true);
    private static final PropertySpec DATE_TIME_PROPERTY_SPEC = UPLPropertySpecFactory.dateTime("DateTime");
    private static final PropertySpec DATE_PROPERTY_SPEC = UPLPropertySpecFactory.date("MyDate");
    private static final PropertySpec EAN_13_PROPERTY_SPEC = MdwToUplPropertySpecAdapter.adapt(PropertySpecFactory.ean13PropertySpec("Ean13"));
    private static final PropertySpec EAN_18_PROPERTY_SPEC = MdwToUplPropertySpecAdapter.adapt(PropertySpecFactory.ean18PropertySpec("Ean18"));
    private static final PropertySpec ENCRYPTED_STRING_PROPERTY_SPEC = UPLPropertySpecFactory.encryptedString("EncryptedString", true);
    private static final PropertySpec HEX_STRING_PROPERTY_SPEC = UPLPropertySpecFactory.hexString("HexString", true);
//    private static final PropertySpec LARGE_STRING_PROPERTY_SPEC = UPLPropertySpecFactory.largeStringPropertySpec("LargeString");
    private static final PropertySpec OBIS_CODE_PROPERTY_SPEC = MdwToUplPropertySpecAdapter.adapt(PropertySpecFactory.obisCodePropertySpec("ObisCode"));
    private static final PropertySpec PASSWORD_PROPERTY_SPEC = UPLPropertySpecFactory.password("Password");
//    private static final PropertySpec SPATIAL_COORDINATES_PROPERTY_SPEC = UPLPropertySpecFactory.spatialCoordinatesPropertySpec("SpatialCoordinates");
    private static final PropertySpec STRING_PROPERTY_SPEC = UPLPropertySpecFactory.string("String", true);
    private static final PropertySpec TIME_DURATION_PROPERTY_SPEC = UPLPropertySpecFactory.temporalAmount("TimeDuration");
    private static final PropertySpec TIME_OF_DAY_PROPERTY_SPEC = UPLPropertySpecFactory.time("TimeOfDay");
    //references
    private static final PropertySpec CODE_PROPERTY_SPEC = UPLPropertySpecFactory.reference("CodeTable", TariffCalendar.class);
    private static final PropertySpec LOAD_PROFILE_PROPERTY_SPEC = UPLPropertySpecFactory.reference("LoadProfile", LoadProfile.class);
//    private static final PropertySpec LOAD_PROFILE_TYPE_PROPERTY_SPEC = UPLPropertySpecFactory.loadProfileTypePropertySpecByList("LoadProfileType");
    private static final PropertySpec LOOKUP_PROPERTY_SPEC = UPLPropertySpecFactory.reference("Lookup", NumberLookup.class);
    private static final PropertySpec TIME_ZONE_IN_USE_PROPERTY_SPEC = UPLPropertySpecFactory.timeZone("TimeZoneInUse");
    private static final PropertySpec USER_FILE_PROPERTY_SPEC = UPLPropertySpecFactory.reference("UserFile", DeviceMessageFile.class);

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
    public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                    BIG_DECIMAL_PROPERTY_SPEC,
                    BOOLEAN_PROPERTY_SPEC,
                    DATE_TIME_PROPERTY_SPEC,
                    DATE_PROPERTY_SPEC,
                    EAN_13_PROPERTY_SPEC,
                    EAN_18_PROPERTY_SPEC,
                    ENCRYPTED_STRING_PROPERTY_SPEC,
                    HEX_STRING_PROPERTY_SPEC,
//                  LARGE_STRING_PROPERTY_SPEC,
                    OBIS_CODE_PROPERTY_SPEC,
                    PASSWORD_PROPERTY_SPEC,
//                    SPATIAL_COORDINATES_PROPERTY_SPEC,
                    STRING_PROPERTY_SPEC,
                    TIME_DURATION_PROPERTY_SPEC,
                    TIME_OF_DAY_PROPERTY_SPEC,
                    CODE_PROPERTY_SPEC,
                    LOAD_PROFILE_PROPERTY_SPEC,
//                    LOAD_PROFILE_TYPE_PROPERTY_SPEC,
                    LOOKUP_PROPERTY_SPEC,
                    TIME_ZONE_IN_USE_PROPERTY_SPEC,
                    USER_FILE_PROPERTY_SPEC);
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