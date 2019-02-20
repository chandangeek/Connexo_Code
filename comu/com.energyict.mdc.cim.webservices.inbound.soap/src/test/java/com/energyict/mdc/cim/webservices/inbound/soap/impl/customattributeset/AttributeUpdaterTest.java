package com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.LongFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.FaultSituationHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.device.data.Device;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AttributeUpdaterTest {
    private static final String CAS_ID = "com.honeywell.cps.device.Cas";
    private static final String STRING_PROPERTY_NAME = "stringPropertyName";
    private static final String STRING_ATTRIBUTE_VALUE = "stringValue";
    private static final String LONG_PROPERTY_NAME = "longPropertyName";
    private static final String LONG_ATTRIBUTE_VALUE = "10";
    private static final String WRONG_LONG_ATTRIBUTE_VALUE = "10a11";

    private AttributeUpdater toTest;
    @Mock
    private Device device;
    @Mock
    private FaultSituationHandler exceptionHandler;
    @Mock
    private CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet;

    @Before
    public void setUp() {
        toTest = new AttributeUpdater(exceptionHandler, device, customPropertySet);

        List<PropertySpec> propertySpecs = Arrays.asList(
            preparePropertySpec(STRING_PROPERTY_NAME, new StringFactory()),
            preparePropertySpec(LONG_PROPERTY_NAME, new LongFactory()));
        when(customPropertySet.getPropertySpecs()).thenReturn(propertySpecs);
    }

    @Test
    public void newCasValues() {
        CasInfo newCasInfo = prepareCasInfo(STRING_ATTRIBUTE_VALUE, LONG_ATTRIBUTE_VALUE);

        CustomPropertySetValues values = toTest.newCasValues(newCasInfo);

        assertThat(values.getProperty(STRING_PROPERTY_NAME)).isEqualTo(STRING_ATTRIBUTE_VALUE);
        assertThat(values.getProperty(LONG_PROPERTY_NAME)).isEqualTo(Long.valueOf(LONG_ATTRIBUTE_VALUE));
    }

    @Test
    public void updateCasValues() {
        CasInfo newCasInfo = prepareCasInfo(STRING_ATTRIBUTE_VALUE, LONG_ATTRIBUTE_VALUE);
        CustomPropertySetValues values = CustomPropertySetValues.empty();

        toTest.updateCasValues(newCasInfo, values);

        assertThat(values.getProperty(STRING_PROPERTY_NAME)).isEqualTo(STRING_ATTRIBUTE_VALUE);
        assertThat(values.getProperty(LONG_PROPERTY_NAME)).isEqualTo(Long.valueOf(LONG_ATTRIBUTE_VALUE));
    }

    @Test
    public void handleExceptionIfValueCantBeConverted() {
        CasInfo newCasInfo = prepareCasInfo(STRING_ATTRIBUTE_VALUE, WRONG_LONG_ATTRIBUTE_VALUE);

        CustomPropertySetValues values = CustomPropertySetValues.empty();

        toTest.updateCasValues(newCasInfo, values);

        assertThat(values.getProperty(STRING_PROPERTY_NAME)).isEqualTo(STRING_ATTRIBUTE_VALUE);
        assertThat(values.getProperty(LONG_PROPERTY_NAME)).isNull();
        verify(exceptionHandler).logException(eq(device), isA(Exception.class),
                eq(MessageSeeds.CANT_CONVERT_VALUE_OF_CUSTOM_ATTRIBUTE), eq(WRONG_LONG_ATTRIBUTE_VALUE),
                        eq(LONG_PROPERTY_NAME), eq(CAS_ID));
    }

    @Test
    public void handleExceptionIfNoPropertySpec() {
        CasInfo newCasInfo = prepareCasInfo(STRING_ATTRIBUTE_VALUE, LONG_ATTRIBUTE_VALUE);
        when(customPropertySet.getPropertySpecs()).thenReturn(Collections.emptyList());
        CustomPropertySetValues values = CustomPropertySetValues.empty();

        toTest.updateCasValues(newCasInfo, values);

        assertThat(values.getProperty(STRING_PROPERTY_NAME)).isNull();
        assertThat(values.getProperty(LONG_PROPERTY_NAME)).isNull();
        verify(exceptionHandler).logSevere(eq(device),
                eq(MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE),
                eq(STRING_PROPERTY_NAME), eq(CAS_ID));
        verify(exceptionHandler).logSevere(eq(device),
                eq(MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE),
                eq(LONG_PROPERTY_NAME), eq(CAS_ID));
    }

    private CasInfo prepareCasInfo(String stringAttributeValue, String integerAttributeValue) {
        CasInfo casInfo = new CasInfo();
        casInfo.setId(CAS_ID);
        Map<String, String> attributesNameValue = new HashMap<>();
        attributesNameValue.put(STRING_PROPERTY_NAME, stringAttributeValue);
        attributesNameValue.put(LONG_PROPERTY_NAME, integerAttributeValue);
        casInfo.setAttributes(attributesNameValue);
        return casInfo;
    }

    private PropertySpec preparePropertySpec(String propertyName, ValueFactory valueFactory) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(propertyName);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        return propertySpec;
    }
}


//    private static final String NON_VERSIONED_CAS_ID = "com.honeywell.cps.device.NonVersioned";
//    private static final String VERSIONED_CAS_ID = "com.honeywell.cps.device.Versioned";
//    private static final String DEVICE_1_NAME = "device1";
//
//    private static final Instant NOW = Instant.now();
//    private static final Instant _30_DAYS_LATTER = NOW.plus(30, ChronoUnit.DAYS);
//    private static final Instant FROM_DATE = NOW;
//    private static final Instant END_DATE = NOW;
//    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

//    private CasInfo nonVersionedCas() {
//        CasInfo casInfo = new CasInfo();
//        casInfo.setId(NON_VERSIONED_CAS_ID);
//        Map<String, String> attributesNameValue = new HashMap<>();
//        attributesNameValue.put("batteryType", "AAA");
//        attributesNameValue.put("batteryReplacementDate", DATE_TIME_FORMATTER.format(_30_DAYS_LATTER));
//        casInfo.setAttributes(attributesNameValue);
//        return casInfo;
//    }
//
//    private CasInfo versionedCas() {
//        CasInfo casInfo = new CasInfo();
//        casInfo.setFromDate(FROM_DATE);
//        casInfo.setEndDate(END_DATE);
//        casInfo.setId(VERSIONED_CAS_ID);
//        Map<String, String> attributesNameValue = new HashMap<>();
//        attributesNameValue.put(STRING_PROPERTY_NAME, STRING_ATTRIBUTE_VALUE);
//        attributesNameValue.put(LONG_PROPERTY_NAME, LONG_ATTRIBUTE_VALUE);
////        attributesNameValue.put("iccid", "111");
////        attributesNameValue.put("provider", "Vodafone");
////        attributesNameValue.put("format", "Full-size (1FF)");
////        attributesNameValue.put("batchId", "2222");
////        attributesNameValue.put("imsi", "1234567890");
//        casInfo.setAttributes(attributesNameValue);
//        return casInfo;
//    }