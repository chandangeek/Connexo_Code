package com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.LoggerUtils;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CasHandlerTest {

    private static final Instant NOW = Instant.now();
    private static final Instant _30_DAYS_LATTER = NOW.plus(30, ChronoUnit.DAYS);
    public static final Instant FROM_DATE = NOW;
    public static final Instant END_DATE = NOW;
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());;
    public static final String NON_VERSIONED_CAS_ID = "com.honeywell.cps.device.NonVersioned";
    public static final String DEVICE_1_NAME = "device1";
    private CasHandler toTest;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MeterConfigFaultMessageFactory faultMessageFactory;
    @Mock
    private LoggerUtils loggerUtils;
    @Mock
    private Clock clock;
    @Mock
    private Device device;
    @Mock
    private RegisteredCustomPropertySet registeredCustomPropertySet;
    @Mock
    private FaultMessage faultMessage;
    @Mock
    private CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet;

    private List<CasInfo> customPropertySetsData;

    @Before
    public void setUp() throws Exception {

        toTest = new CasHandler(customPropertySetService, thesaurus, faultMessageFactory, clock){
            @Override
            LoggerUtils getLoggerUtils(Thesaurus thesaurus, MeterConfigFaultMessageFactory faultMessageFactory) {
                return loggerUtils;
            }
        };
        customPropertySetsData = Collections.singletonList(nonVersionedCas());

        when(device.getName()).thenReturn(DEVICE_1_NAME);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void noFaultsWhenNoCasToAdd() {
        List<FaultMessage> faultMessages = toTest.addCustomPropertySetsData(device, Collections.emptyList());

        assertThat(faultMessages).isEmpty();
        verifyNoMoreInteractions(loggerUtils);
    }

    @Test
    public void logAnyNonFaultException() {

        toTest.addCustomPropertySetsData(device, customPropertySetsData);

        verify(loggerUtils).logException(eq(device), any(), any(), isA(Exception.class),
                eq(MessageSeeds.CANT_ASSIGN_VALUES_FOR_CUSTOM_ATTRIBUTE_SET), eq(NON_VERSIONED_CAS_ID));
    }

    @Test
    public void logFaultExceptionIfNoRegisteredCasForId() {
        when(customPropertySetService.findActiveCustomPropertySet(NON_VERSIONED_CAS_ID)).thenReturn(Optional.ofNullable(null));
        when(faultMessageFactory.meterConfigFaultMessageSupplier(DEVICE_1_NAME, MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE_SET, NON_VERSIONED_CAS_ID)).thenReturn(() -> faultMessage);

        toTest.addCustomPropertySetsData(device, customPropertySetsData);

        verify(loggerUtils).logSevere(eq(device), any(), any(), eq(faultMessage));
    }

    @Test
    public void addUnVersionedCustomAttributes() {
        String nonVersionedCasId = NON_VERSIONED_CAS_ID;
        prepareRegisteredCustomPropertySet(nonVersionedCasId);

        toTest.addCustomPropertySetsData(device, customPropertySetsData);

        verify(customPropertySetService).setValuesFor(eq(customPropertySet), eq(device), isA(CustomPropertySetValues.class));
    }

//    @Test
//    public void logExceptionIfAny() {
//
//        toTest.addCustomPropertySetsData(device, customPropertySetsData);
//
//        verify(loggerUtils).logException(eq(device), any(), any(), isA(Exception.class),
//                eq(MessageSeeds.CANT_ASSIGN_VALUES_FOR_CUSTOM_ATTRIBUTE_SET), eq(NON_VERSIONED_CAS_ID));
//    }
//

    private CasInfo nonVersionedCas() {
        CasInfo casInfo = new CasInfo();
        casInfo.setId(NON_VERSIONED_CAS_ID);
        Map<String, String> attributesNameValue = new HashMap<>();
        attributesNameValue.put("batteryType","AAA");
        attributesNameValue.put("batteryReplacementDate", DATE_TIME_FORMATTER.format(_30_DAYS_LATTER));
        casInfo.setAttributes(attributesNameValue);
        return casInfo;
    }

    private CasInfo versionedCas() {
        CasInfo casInfo = new CasInfo();
        casInfo.setFromDate(FROM_DATE);
        casInfo.setEndDate(END_DATE);
        Map<String, String> attributesNameValue = new HashMap<>();
        attributesNameValue.put("status","Active");
        attributesNameValue.put("iccid","111");
        attributesNameValue.put("provider","Vodafone");
        attributesNameValue.put("format","Full-size (1FF)");
        attributesNameValue.put("batchId","2222");
        attributesNameValue.put("imsi","1234567890");
        casInfo.setAttributes(attributesNameValue);
        return casInfo;
    }

    private void prepareRegisteredCustomPropertySet(String nonVersionedCasId) {
        when(customPropertySetService.findActiveCustomPropertySet(nonVersionedCasId)).thenReturn(Optional.of(registeredCustomPropertySet));
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
    }
}
