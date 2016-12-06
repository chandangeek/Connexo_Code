package com.energyict.mdc.channels;

import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.VoidComChannel;
import com.energyict.mdc.tasks.ConnectionTypeImpl;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.cbo.HexString;
import com.energyict.cbo.Password;
import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.TimeOfDay;
import com.energyict.coordinates.SpatialCoordinates;
import com.energyict.ean.Ean13;
import com.energyict.ean.Ean18;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.LoadProfileType;
import com.energyict.mdw.core.Lookup;
import com.energyict.mdw.core.TimeZoneInUse;
import com.energyict.mdw.core.UserFile;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused") // Pluggable
public class TestOfRequiredPropertiesConnectionType extends ConnectionTypeImpl {

    private final static PropertySpec<BigDecimal> BIG_DECIMAL_PROPERTY_SPEC = UPLPropertySpecFactory.bigDecimal("BigDecimal", true);
    private final static PropertySpec<Boolean> BOOLEAN_PROPERTY_SPEC = UPLPropertySpecFactory.booleanValue("Boolean", true);
    private final static PropertySpec<Date> DATE_TIME_PROPERTY_SPEC = UPLPropertySpecFactory.dateTimePropertySpec("DateTime");
    private final static PropertySpec<Date> DATE_PROPERTY_SPEC = UPLPropertySpecFactory.datePropertySpec("MyDate");
    private final static PropertySpec<Ean13> EAN_13_PROPERTY_SPEC = UPLPropertySpecFactory.ean13PropertySpec("Ean13");
    private final static PropertySpec<Ean18> EAN_18_PROPERTY_SPEC = UPLPropertySpecFactory.ean18PropertySpec("Ean18");
    private final static PropertySpec<String> ENCRYPTED_STRING_PROPERTY_SPEC = UPLPropertySpecFactory.encryptedStringPropertySpec("EncryptedString");
    private final static PropertySpec<HexString> HEX_STRING_PROPERTY_SPEC = UPLPropertySpecFactory.hexString("HexString", true);
    private final static PropertySpec<String> LARGE_STRING_PROPERTY_SPEC = UPLPropertySpecFactory.largeStringPropertySpec("LargeString");
    private final static PropertySpec<ObisCode> OBIS_CODE_PROPERTY_SPEC = UPLPropertySpecFactory.obisCodePropertySpec("ObisCode");
    private final static PropertySpec<Password> PASSWORD_PROPERTY_SPEC = UPLPropertySpecFactory.passwordPropertySpec("Password");
    private final static PropertySpec<SpatialCoordinates> SPATIAL_COORDINATES_PROPERTY_SPEC = UPLPropertySpecFactory.spatialCoordinatesPropertySpec("SpatialCoordinates");
    private final static PropertySpec<String> STRING_PROPERTY_SPEC = UPLPropertySpecFactory.string("String", true);
    private final static PropertySpec<TimeDuration> TIME_DURATION_PROPERTY_SPEC = UPLPropertySpecFactory.timeDurationPropertySpec("TimeDuration");
    private final static PropertySpec<TimeOfDay> TIME_OF_DAY_PROPERTY_SPEC = UPLPropertySpecFactory.timeOfDayPropertySpec("TimeOfDay");
    //references
    private final static PropertySpec<Code> CODE_PROPERTY_SPEC = UPLPropertySpecFactory.codeTableReferencePropertySpec("CodeTable");
    private final static PropertySpec<LoadProfile> LOAD_PROFILE_PROPERTY_SPEC = UPLPropertySpecFactory.loadProfilePropertySpec("LoadProfile");
    private final static PropertySpec<LoadProfileType> LOAD_PROFILE_TYPE_PROPERTY_SPEC = UPLPropertySpecFactory.loadProfileTypePropertySpecByList("LoadProfileType");
    private final static PropertySpec<Lookup> LOOKUP_PROPERTY_SPEC = UPLPropertySpecFactory.lookupPropertySpec("Lookup");
    private final static PropertySpec<TimeZoneInUse> TIME_ZONE_IN_USE_PROPERTY_SPEC = UPLPropertySpecFactory.timeZoneInUseReferencePropertySpec("TimeZoneInUse");
    private final static PropertySpec<UserFile> USER_FILE_PROPERTY_SPEC = UPLPropertySpecFactory.userFileReferencePropertySpec("UserFile");

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
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(BIG_DECIMAL_PROPERTY_SPEC);
        propertySpecs.add(BOOLEAN_PROPERTY_SPEC);
        propertySpecs.add(DATE_TIME_PROPERTY_SPEC);
        propertySpecs.add(DATE_PROPERTY_SPEC);
        propertySpecs.add(EAN_13_PROPERTY_SPEC);
        propertySpecs.add(EAN_18_PROPERTY_SPEC);
        propertySpecs.add(ENCRYPTED_STRING_PROPERTY_SPEC);
        propertySpecs.add(HEX_STRING_PROPERTY_SPEC);
        propertySpecs.add(LARGE_STRING_PROPERTY_SPEC);
        propertySpecs.add(OBIS_CODE_PROPERTY_SPEC);
        propertySpecs.add(PASSWORD_PROPERTY_SPEC);
        propertySpecs.add(SPATIAL_COORDINATES_PROPERTY_SPEC);
        propertySpecs.add(STRING_PROPERTY_SPEC);
        propertySpecs.add(TIME_DURATION_PROPERTY_SPEC);
        propertySpecs.add(TIME_OF_DAY_PROPERTY_SPEC);
        //references
        propertySpecs.add(CODE_PROPERTY_SPEC);
        propertySpecs.add(LOAD_PROFILE_PROPERTY_SPEC);
        propertySpecs.add(LOAD_PROFILE_TYPE_PROPERTY_SPEC);
        propertySpecs.add(LOOKUP_PROPERTY_SPEC);
        propertySpecs.add(TIME_ZONE_IN_USE_PROPERTY_SPEC);
        propertySpecs.add(USER_FILE_PROPERTY_SPEC);
        return propertySpecs;
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
