package com.energyict.mdc.channels;

import com.energyict.cbo.*;
import com.energyict.coordinates.SpatialCoordinates;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.ean.Ean13;
import com.energyict.ean.Ean18;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.protocol.VoidComChannel;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.ConnectionTypeImpl;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;


import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("unused") // Pluggable
public class TestOfRequiredPropertiesConnectionType extends ConnectionTypeImpl {

    private final static PropertySpec<BigDecimal> BIG_DECIMAL_PROPERTY_SPEC  = PropertySpecFactory.bigDecimalPropertySpec("BigDecimal");
    private final static PropertySpec<Boolean> BOOLEAN_PROPERTY_SPEC = PropertySpecFactory.booleanPropertySpec("Boolean");
    private final static PropertySpec<Date> DATE_TIME_PROPERTY_SPEC = PropertySpecFactory.dateTimePropertySpec("DateTime");
    private final static PropertySpec<Date> DATE_PROPERTY_SPEC = PropertySpecFactory.datePropertySpec("Date");
    private final static PropertySpec<Ean13> EAN_13_PROPERTY_SPEC = PropertySpecFactory.ean13PropertySpec("Ean13");
    private final static PropertySpec<Ean18> EAN_18_PROPERTY_SPEC = PropertySpecFactory.ean18PropertySpec("Ean18");
    private final static PropertySpec<String> ENCRYPTED_STRING_PROPERTY_SPEC = PropertySpecFactory.encryptedStringPropertySpec("EncryptedString");
    private final static PropertySpec<HexString> HEX_STRING_PROPERTY_SPEC = PropertySpecFactory.hexStringPropertySpec("HexString");
    private final static PropertySpec<String> LARGE_STRING_PROPERTY_SPEC = PropertySpecFactory.largeStringPropertySpec("LargeString");
    private final static PropertySpec<ObisCode> OBIS_CODE_PROPERTY_SPEC = PropertySpecFactory.obisCodePropertySpec("ObisCode");
    private final static PropertySpec<Password> PASSWORD_PROPERTY_SPEC = PropertySpecFactory.passwordPropertySpec("Password");
    private final static PropertySpec<SpatialCoordinates> SPATIAL_COORDINATES_PROPERTY_SPEC = PropertySpecFactory.spatialCoordinatesPropertySpec("SpatialCoordinates");
    private final static PropertySpec<String> STRING_PROPERTY_SPEC = PropertySpecFactory.stringPropertySpec("String");
    private final static PropertySpec<TimeDuration> TIME_DURATION_PROPERTY_SPEC = PropertySpecFactory.timeDurationPropertySpec("TimeDuration");
    private final static PropertySpec<TimeOfDay> TIME_OF_DAY_PROPERTY_SPEC = PropertySpecFactory.timeOfDayPropertySpec("TimeOfDay");
    //references
    private final static PropertySpec<Code> CODE_PROPERTY_SPEC = PropertySpecFactory.codeTableReferencePropertySpec("CodeTable");
    private final static PropertySpec<LoadProfile> LOAD_PROFILE_PROPERTY_SPEC = PropertySpecFactory.loadProfilePropertySpec("LoadProfile");
    private final static PropertySpec<LoadProfileType> LOAD_PROFILE_TYPE_PROPERTY_SPEC = PropertySpecFactory.loadProfileTypePropertySpecByList("LoadProfileType");
    private final static PropertySpec<Lookup> LOOKUP_PROPERTY_SPEC = PropertySpecFactory.lookupPropertySpec("Lookup");
    private final static PropertySpec<TimeZoneInUse> TIME_ZONE_IN_USE_PROPERTY_SPEC = PropertySpecFactory.timeZoneInUseReferencePropertySpec("TimeZoneInUse");
    private final static PropertySpec<UserFile> USER_FILE_PROPERTY_SPEC = PropertySpecFactory.userFileReferencePropertySpec("UserFile");

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
        if (name != null){
            for (PropertySpec spec: getRequiredProperties()){
                if (name.equals(spec.getName())){
                    return spec;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return true;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> specs = new ArrayList<>();
        specs.add(BIG_DECIMAL_PROPERTY_SPEC);
        specs.add(BOOLEAN_PROPERTY_SPEC);
        specs.add(DATE_TIME_PROPERTY_SPEC);
        specs.add(DATE_PROPERTY_SPEC);
        specs.add(EAN_13_PROPERTY_SPEC);
        specs.add(EAN_18_PROPERTY_SPEC);
        specs.add(ENCRYPTED_STRING_PROPERTY_SPEC);
        specs.add(HEX_STRING_PROPERTY_SPEC);
        specs.add(LARGE_STRING_PROPERTY_SPEC);
        specs.add(OBIS_CODE_PROPERTY_SPEC);
        specs.add(PASSWORD_PROPERTY_SPEC);
        specs.add(SPATIAL_COORDINATES_PROPERTY_SPEC);
        specs.add(STRING_PROPERTY_SPEC);
        specs.add(TIME_DURATION_PROPERTY_SPEC);
        specs.add(TIME_OF_DAY_PROPERTY_SPEC);
        //references
        specs.add(CODE_PROPERTY_SPEC);
        specs.add(LOAD_PROFILE_PROPERTY_SPEC);
        specs.add(LOAD_PROFILE_TYPE_PROPERTY_SPEC);
        specs.add(LOOKUP_PROPERTY_SPEC);
        specs.add(TIME_ZONE_IN_USE_PROPERTY_SPEC);
        specs.add(USER_FILE_PROPERTY_SPEC);
        return specs;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }
}
