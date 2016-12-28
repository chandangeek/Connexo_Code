package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.coronis.waveflowDLMS.a1800.ProfileDataReader;
import com.energyict.protocolimpl.dlms.common.ObisCodePropertySpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class A1800 extends AbstractDLMS {
    /**
     * Predefined obiscodes for the A1800 meter
     */
    private static final ObisCode LOAD_PROFILE_PULSES = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode LOAD_PROFILE_ENG_CUMM = ObisCode.fromString("1.0.99.1.1.255");
    private static final ObisCode LOAD_PROFILE_ENG_ADV = ObisCode.fromString("1.0.99.1.2.255");
    private static final ObisCode METER_SERIAL_NUMBER = ObisCode.fromString("1.0.96.1.0.255");

    public static final ObisCode LOG_PROFILE = ObisCode.fromString("1.1.99.98.0.255");
    public static final ObisCode OBJECT_LIST = ObisCode.fromString("0.0.40.0.0.255");
    public static final String SCALE_FACTOR = "Scale factor";
    public static final String MULTIPLIER = "Multiplier";

    private Map<ObisCode, ObjectEntry> objectEntries = null;
    private boolean applyMultiplier = false;

    public A1800(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        ProfileDataReader profileDataReader = new ProfileDataReader(this);
        return profileDataReader.getProfileData(lastReading, includeEvents);
    }

    public boolean isApplyMultiplier() {
        return applyMultiplier;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(new ObisCodePropertySpec("LoadProfileObisCode", false));
        propertySpecs.add(this.stringSpec(PROPERTY_LP_MULTIPLIER, false));
        return propertySpecs;
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        super.setProperties(properties);
        setLoadProfileObisCode(ObisCode.fromString(properties.getTypedProperty("LoadProfileObisCode", LOAD_PROFILE_PULSES.toString())));
        applyMultiplier = !"0".equals(properties.getProperty(PROPERTY_LP_MULTIPLIER, "0"));

        String password = getInfoTypePassword();
        if (password != null) {
            StringBuilder strbuild = new StringBuilder();
            // pad with zeros...
            for (int i = 0; i < password.length(); i++) {
                strbuild.append("0").append(password.substring(i, i + 1));
            }
            setInfoTypePassword(strbuild.toString());
        }
    }

    @Override
    protected ObisCode getSerialNumberObisCodeForPairingRequest() {
        return METER_SERIAL_NUMBER;
    }

    @Override
    protected ObisCode getUtilityIdObiscode() {
        return METER_SERIAL_NUMBER;
    }

    @Override
    protected PairingMeterId getPairingMeterId() {
        return PairingMeterId.A1800;
    }

    @Override
    protected Map<ObisCode, ObjectEntry> getObjectEntries() {
        /*
         * Lazy initialize the map with objectEntries
         */
        if (objectEntries == null) {
            objectEntries = new HashMap<>();

            objectEntries.put(METER_SERIAL_NUMBER, new ObjectEntry("Meter serial number", 1));

            objectEntries.put(ObisCode.fromString("1.1.1.8.0.255"), new ObjectEntry("Import active energy total", 3));
            objectEntries.put(ObisCode.fromString("1.1.1.8.1.255"), new ObjectEntry("Import active energy tariff 1", 3));
            objectEntries.put(ObisCode.fromString("1.1.1.8.2.255"), new ObjectEntry("Import active energy tariff 2", 3));
            objectEntries.put(ObisCode.fromString("1.1.1.8.3.255"), new ObjectEntry("Import active energy tariff 3", 3));
            objectEntries.put(ObisCode.fromString("1.1.1.8.4.255"), new ObjectEntry("Import active energy tariff 4", 3));

            objectEntries.put(ObisCode.fromString("1.1.2.8.0.255"), new ObjectEntry("Export active energy total", 3));
            objectEntries.put(ObisCode.fromString("1.1.2.8.1.255"), new ObjectEntry("Export active energy tariff 1", 3));
            objectEntries.put(ObisCode.fromString("1.1.2.8.2.255"), new ObjectEntry("Export active energy tariff 2", 3));
            objectEntries.put(ObisCode.fromString("1.1.2.8.3.255"), new ObjectEntry("Export active energy tariff 3", 3));
            objectEntries.put(ObisCode.fromString("1.1.2.8.4.255"), new ObjectEntry("Export active energy tariff 4", 3));

            objectEntries.put(ObisCode.fromString("1.1.9.6.1.255"), new ObjectEntry("Apparent maximum 1", 4));
            objectEntries.put(ObisCode.fromString("1.1.32.7.0.255"), new ObjectEntry("Phase 1 instantaneous voltage", 3));
            objectEntries.put(ObisCode.fromString("1.1.52.7.0.255"), new ObjectEntry("Phase 2 instantaneous voltage", 3));
            objectEntries.put(ObisCode.fromString("1.1.72.7.0.255"), new ObjectEntry("Phase 3 instantaneous voltage", 3));
            objectEntries.put(ObisCode.fromString("1.1.31.7.0.255"), new ObjectEntry("Phase 1 instantaneous current", 3));
            objectEntries.put(ObisCode.fromString("1.1.51.7.0.255"), new ObjectEntry("Phase 2 instantaneous current", 3));
            objectEntries.put(ObisCode.fromString("1.1.71.7.0.255"), new ObjectEntry("Phase 3 instantaneous current", 3));

            objectEntries.put(ObisCode.fromString("1.1.81.7.40.255"), new ObjectEntry("Phase angle 1 instantaneous current", 3));
            objectEntries.put(ObisCode.fromString("1.1.81.7.50.255"), new ObjectEntry("Phase angle 2 instantaneous current", 3));
            objectEntries.put(ObisCode.fromString("1.1.81.7.60.255"), new ObjectEntry("Phase angle 3 instantaneous current", 3));

            objectEntries.put(ObisCode.fromString("1.1.130.7.0.255"), new ObjectEntry("Apparent power vectorial instantaneous current", 3));
            objectEntries.put(ObisCode.fromString("1.1.142.7.0.255"), new ObjectEntry("Apparent power arithmetic instantaneous current", 3));
            objectEntries.put(ObisCode.fromString("1.1.128.7.0.255"), new ObjectEntry("Import power instantaneous value", 3));

            objectEntries.put(ObisCode.fromString("1.1.13.7.0.255"), new ObjectEntry("Instantaneous value of Aggregate power factor vectorial calculation", 3));
            objectEntries.put(ObisCode.fromString("1.1.143.7.0.255"), new ObjectEntry("Instantaneous value of Aggregate power factor arithmetic calculation", 3));

            objectEntries.put(ObisCode.fromString("1.1.0.4.2.255"), new ObjectEntry("CT ratio", 1));
            objectEntries.put(ObisCode.fromString("1.1.0.4.3.255"), new ObjectEntry("VT ratio", 1));

            objectEntries.put(ObisCode.fromString("0.0.97.97.0.255"), new ObjectEntry("Displayed Error Byte 1", 1));
            objectEntries.put(ObisCode.fromString("0.0.97.97.1.255"), new ObjectEntry("Displayed Error Byte 2", 1));
            objectEntries.put(ObisCode.fromString("0.0.97.97.2.255"), new ObjectEntry("Displayed Error Byte 3", 1));
            objectEntries.put(ObisCode.fromString("0.0.97.97.3.255"), new ObjectEntry("Displayed Warning Byte 1", 1));
            objectEntries.put(ObisCode.fromString("0.0.97.97.4.255"), new ObjectEntry("Displayed Warning Byte 2", 1));
            objectEntries.put(ObisCode.fromString("0.0.97.97.5.255"), new ObjectEntry("Displayed Warning Byte 3", 1));

            objectEntries.put(ObisCode.fromString("1.1.1.6.0.255"), new ObjectEntry("Import active maximum demand (current billing period)", 4));
            objectEntries.put(ObisCode.fromString("1.1.1.6.1.255"), new ObjectEntry("Import active maximum demand rate 1 (current billing period)", 4));
            objectEntries.put(ObisCode.fromString("1.1.1.6.2.255"), new ObjectEntry("Import active maximum demand rate 2 (current billing period)", 4));
            objectEntries.put(ObisCode.fromString("1.1.1.6.3.255"), new ObjectEntry("Import active maximum demand rate 3 (current billing period)", 4));
            objectEntries.put(ObisCode.fromString("1.1.1.6.4.255"), new ObjectEntry("Import active maximum demand rate 4 (current billing period)", 4));

            objectEntries.put(ObisCode.fromString("0.0.98.1.0.255"), new ObjectEntry("Historical registers self reads total", 7));
            objectEntries.put(ObisCode.fromString("0.0.98.1.1.255"), new ObjectEntry("Historical registers self reads total rate 1", 7));
            objectEntries.put(ObisCode.fromString("0.0.98.1.2.255"), new ObjectEntry("Historical registers self reads total rate 2", 7));
            objectEntries.put(ObisCode.fromString("0.0.98.1.3.255"), new ObjectEntry("Historical registers self reads total rate 3", 7));
            objectEntries.put(ObisCode.fromString("0.0.98.1.4.255"), new ObjectEntry("Historical registers self reads total rate 4", 7));

            objectEntries.put(ObisCode.fromString("0.0.98.2.0.255"), new ObjectEntry("Historical registers previous billing total", 7));
            objectEntries.put(ObisCode.fromString("0.0.98.2.1.255"), new ObjectEntry("Historical registers previous billing total rate 1", 7));
            objectEntries.put(ObisCode.fromString("0.0.98.2.2.255"), new ObjectEntry("Historical registers previous billing total rate 2", 7));
            objectEntries.put(ObisCode.fromString("0.0.98.2.3.255"), new ObjectEntry("Historical registers previous billing total rate 3", 7));
            objectEntries.put(ObisCode.fromString("0.0.98.2.4.255"), new ObjectEntry("Historical registers previous billing total rate 4", 7));

            objectEntries.put(ObisCode.fromString("0.1.10.0.1.255"), new ObjectEntry("Demand reset action", 9));

            objectEntries.put(ObisCode.fromString("1.1.96.132.2.255"), new ObjectEntry(SCALE_FACTOR, 1));
            objectEntries.put(ObisCode.fromString("1.1.96.132.1.255"), new ObjectEntry(MULTIPLIER, 1));

            objectEntries.put(LOG_PROFILE, new ObjectEntry("Logbook", 7));
            objectEntries.put(LOAD_PROFILE_PULSES, new ObjectEntry("Load profile puls values", 7));
            objectEntries.put(LOAD_PROFILE_ENG_CUMM, new ObjectEntry("Load profile cumulative engineering values", 7));
            objectEntries.put(LOAD_PROFILE_ENG_ADV, new ObjectEntry("Load profile advance engineering values", 7));
            objectEntries.put(OBJECT_LIST, new ObjectEntry("Object list", 15));
        }
        return objectEntries;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public Logger getLogger() {
        return super.getLogger();
    }

    @Override
    public int getInfoTypeProtocolRetriesProperty() {
        return super.getInfoTypeProtocolRetriesProperty();
    }

}