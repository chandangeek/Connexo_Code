package com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.FaultSituationHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
@RunWith(MockitoJUnitRunner.class)
public class VersionedCasHandlerTest {

    private static final String VERSIONED_CAS_ID = "com.honeywell.cps.device.Versioned";
    private static final String DEVICE_NAME = "device1";
    private static final Instant NOW = Instant.now();
    private static final Instant _30_DAYS_AGO = NOW.minus(30, ChronoUnit.DAYS);
    private static final Instant FROM_DATE = NOW;
    private static final Instant VERSION_ID = NOW.plus(10, ChronoUnit.DAYS);
    private static final Instant END_DATE = NOW.plus(30, ChronoUnit.DAYS);
    private static final Instant RECEIVED_DATE = _30_DAYS_AGO;

    private VersionedCasHandler toTest;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Device device;
    @Mock
    private CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private AttributeUpdater attributeUpdater;
    @Mock
    private FaultSituationHandler faultSituationHandler;
    @Mock
    private Clock clock;
    @Mock
    private FaultMessage faultMessage;
    @Mock
    private CasConflictSolver casConflictSolver;

    @Before
    public void setUp(){
        toTest = prepareInstanceToTest();

        when(device.getName()).thenReturn(DEVICE_NAME);
        when(device.getLifecycleDates().getReceivedDate()).thenReturn(Optional.of(RECEIVED_DATE));
    }

    @Test
    public void doNotCreateNewVersionWhenThereAreAnyFaultsAtUpdatingAttributes() throws FaultMessage {
        CasInfo casInfo = versionedCas(null, FROM_DATE);
        when(attributeUpdater.anyFaults()).thenReturn(true);

        toTest.handleVersionedCas(casInfo);

        verifyZeroInteractions(customPropertySetService);
    }

    @Test(expected = FaultMessage.class)
    public void throwFaultWhenFromDateIsNullForCreationOfNewVersion() throws FaultMessage {
        CasInfo casInfo = versionedCas(null, null);
        when(attributeUpdater.anyFaults()).thenReturn(false);
        when(faultSituationHandler.newFault(DEVICE_NAME, MessageSeeds.START_DATE_LOWER_CREATED_DATE,
                DEVICE_NAME)).thenReturn(faultMessage);

        toTest.handleVersionedCas(casInfo);
    }

    @Test(expected = FaultMessage.class)
    public void throwFaultWhenFromDateBeforeReceivedDateForCreationOfNewVersion() throws FaultMessage {
        CasInfo casInfo = versionedCas(null, RECEIVED_DATE.minus(30, ChronoUnit.DAYS));
        when(attributeUpdater.anyFaults()).thenReturn(false);
        when(faultSituationHandler.newFault(DEVICE_NAME, MessageSeeds.START_DATE_LOWER_CREATED_DATE,
                DEVICE_NAME)).thenReturn(faultMessage);

        toTest.handleVersionedCas(casInfo);
    }

    @Test
    public void createNewVersion() throws FaultMessage {
        CasInfo casInfo = versionedCas(null, FROM_DATE);
        when(attributeUpdater.anyFaults()).thenReturn(false);
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        when(attributeUpdater.newCasValues(casInfo)).thenReturn(values);
        Range<Instant> fromToRange = Range.closedOpen(FROM_DATE, END_DATE);
        when(casConflictSolver.solveConflictsForCreate(device, customPropertySet,
                FROM_DATE, END_DATE)).thenReturn(fromToRange);

        toTest.handleVersionedCas(casInfo);

        verify(casConflictSolver).solveConflictsForCreate(device, customPropertySet,
                FROM_DATE, END_DATE);
        verify(customPropertySetService).setValuesVersionFor(customPropertySet, device, values, fromToRange);
    }

    @Test
    public void updateExistingVersionVersion() throws FaultMessage {
        CasInfo casInfo = versionedCas(null, FROM_DATE);
        when(attributeUpdater.anyFaults()).thenReturn(false);
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        when(attributeUpdater.newCasValues(casInfo)).thenReturn(values);
        Range<Instant> fromToRange = Range.closedOpen(FROM_DATE, END_DATE);
        when(casConflictSolver.solveConflictsForCreate(device, customPropertySet,
                FROM_DATE, END_DATE)).thenReturn(fromToRange);

        toTest.handleVersionedCas(casInfo);

        verify(casConflictSolver).solveConflictsForCreate(device, customPropertySet,
                FROM_DATE, END_DATE);
        verify(customPropertySetService).setValuesVersionFor(customPropertySet, device, values, fromToRange);
    }


    private CasInfo versionedCas(Instant versionId, Instant fromDate) {
        CasInfo casInfo = new CasInfo();
        casInfo.setFromDate(fromDate);
        casInfo.setEndDate(END_DATE);
        casInfo.setId(VERSIONED_CAS_ID);
        casInfo.setVersionId(versionId);
//        Map<String, String> attributesNameValue = new HashMap<>();
//        attributesNameValue.put(STRING_PROPERTY_NAME, STRING_ATTRIBUTE_VALUE);
//        attributesNameValue.put(LONG_PROPERTY_NAME, LONG_ATTRIBUTE_VALUE);
////        attributesNameValue.put("iccid", "111");
////        attributesNameValue.put("provider", "Vodafone");
////        attributesNameValue.put("format", "Full-size (1FF)");
////        attributesNameValue.put("batchId", "2222");
////        attributesNameValue.put("imsi", "1234567890");
//        casInfo.setAttributes(attributesNameValue);
        return casInfo;
    }

    private VersionedCasHandler prepareInstanceToTest() {
        return new VersionedCasHandler(device, customPropertySet, customPropertySetService, attributeUpdater, faultSituationHandler, clock){
            @Override
            CasConflictSolver getCasConflictsSolver(CustomPropertySetService customPropertySetService) {
                return casConflictSolver;
            }
        };
    }
}


//    private static final String NON_VERSIONED_CAS_ID = "com.honeywell.cps.device.NonVersioned";
//    private static final String VERSIONED_CAS_ID = "com.honeywell.cps.device.Versioned";
//    private static final String DEVICE_1_NAME = "device1";
//

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