package com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.FaultSituationHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.LoggerUtils;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    private static final String NON_VERSIONED_CAS_ID = "com.honeywell.cps.device.NonVersioned";
    private static final String VERSIONED_CAS_ID = "com.honeywell.cps.device.Versioned";
    private static final String DEVICE_NAME = "device1";
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
    private FaultMessage faultMessage;
    @Mock
    private VersionedCasHandler versionedCasHandler;


    @Mock
    private AttributeUpdater attributeUpdater;

    @Before
    public void setUp() throws Exception {

        toTest = prepareInstanceToTest();
        when(device.getName()).thenReturn(DEVICE_NAME);
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
        List<CasInfo> customPropertySetsData = Collections.singletonList(prepareCasInfo(NON_VERSIONED_CAS_ID));

        toTest.addCustomPropertySetsData(device, customPropertySetsData);

        verify(loggerUtils).logException(eq(device), any(), any(), isA(Exception.class),
                eq(MessageSeeds.CANT_ASSIGN_VALUES_FOR_CUSTOM_ATTRIBUTE_SET), eq(NON_VERSIONED_CAS_ID));
    }

    @Test
    public void logFaultExceptionIfNoRegisteredCasForId() {
        List<CasInfo> customPropertySetsData = Collections.singletonList(prepareCasInfo(NON_VERSIONED_CAS_ID));
        when(customPropertySetService.findActiveCustomPropertySet(NON_VERSIONED_CAS_ID)).thenReturn(Optional.ofNullable(null));
        when(faultMessageFactory.meterConfigFaultMessageSupplier(DEVICE_NAME, MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE_SET, NON_VERSIONED_CAS_ID)).thenReturn(() -> faultMessage);

        toTest.addCustomPropertySetsData(device, customPropertySetsData);

        verify(loggerUtils).logSevere(eq(device), any(), any(), eq(faultMessage));
    }

    @Test
    public void addUnVersionedCustomAttributes() {
        String nonVersionedCasId = NON_VERSIONED_CAS_ID;
        CustomPropertySet customPropertySet = prepareRegisteredCustomPropertySet(nonVersionedCasId, false);
        CasInfo nonVersionedCas = prepareCasInfo(NON_VERSIONED_CAS_ID);
        List<CasInfo> customPropertySetsData = Collections.singletonList(nonVersionedCas);
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.empty();
        when(attributeUpdater.newCasValues(nonVersionedCas)).thenReturn(customPropertySetValues);

        toTest.addCustomPropertySetsData(device, customPropertySetsData);

        verify(customPropertySetService).setValuesFor(customPropertySet, device, customPropertySetValues);
    }

    @Test
    public void addVersionedCustomAttributes() throws FaultMessage {
        CasInfo versionedCasInfo = prepareCasInfo(VERSIONED_CAS_ID);
        List<CasInfo> customPropertySetsData = Collections.singletonList(versionedCasInfo);
        prepareRegisteredCustomPropertySet(VERSIONED_CAS_ID, true);

        toTest.addCustomPropertySetsData(device, customPropertySetsData);

        verify(versionedCasHandler).handleVersionedCas(versionedCasInfo);
    }

    @Test
    public void addVersionedAndUnVersionedCustomAttributes() throws FaultMessage {
        CustomPropertySet customPropertySet = prepareRegisteredCustomPropertySet(NON_VERSIONED_CAS_ID, false);
        prepareRegisteredCustomPropertySet(VERSIONED_CAS_ID, true);
        CasInfo nonVersionedCas = prepareCasInfo(NON_VERSIONED_CAS_ID);
        CasInfo versionedCasInfo = prepareCasInfo(VERSIONED_CAS_ID);
        List<CasInfo> customPropertySetsData = Arrays.asList(nonVersionedCas, versionedCasInfo);
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.empty();
        when(attributeUpdater.newCasValues(nonVersionedCas)).thenReturn(customPropertySetValues);

        toTest.addCustomPropertySetsData(device, customPropertySetsData);

        verify(customPropertySetService).setValuesFor(customPropertySet, device, customPropertySetValues);
        verify(versionedCasHandler).handleVersionedCas(versionedCasInfo);
    }


    private CasInfo prepareCasInfo(String casId) {
        CasInfo casInfo = new CasInfo();
        casInfo.setId(casId);
        return casInfo;
    }

    private CustomPropertySet<Device, ? extends PersistentDomainExtension> prepareRegisteredCustomPropertySet(String casId, boolean versioned) {
        CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet = mock(CustomPropertySet.class);
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);

        when(customPropertySet.isVersioned()).thenReturn(versioned);
        when(customPropertySetService.findActiveCustomPropertySet(casId)).thenReturn(Optional.of(registeredCustomPropertySet));
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        return customPropertySet;
    }

    private CasHandler prepareInstanceToTest() {
        return new CasHandler(customPropertySetService, thesaurus, faultMessageFactory, clock){
            @Override
            LoggerUtils getLoggerUtils(Thesaurus thesaurus, MeterConfigFaultMessageFactory faultMessageFactory) {
                return loggerUtils;
            }
            @Override
            VersionedCasHandler getVersionedCasHandler(Device device, FaultSituationHandler faultSituationHandler, CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet, AttributeUpdater attributeUpdater) {
                return versionedCasHandler;
            }
            @Override
            AttributeUpdater getAttributeUpdater(Device device, FaultSituationHandler faultSituationHandler, CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet) {
                return attributeUpdater;
            }
        };
    }
}
