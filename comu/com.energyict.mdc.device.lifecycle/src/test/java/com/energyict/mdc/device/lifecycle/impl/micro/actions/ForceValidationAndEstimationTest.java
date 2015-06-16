package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.properties.*;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link EnableValidation} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-04 (17:12)
 */
@RunWith(MockitoJUnitRunner.class)
public class ForceValidationAndEstimationTest {

    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private ValidationService validationService;
    @Mock
    private ValidationEvaluator validationEvaluator;
    @Mock
    private EstimationService estimationService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;

    @Before
    public void setUp() {
        doAnswer(invocation -> ((Transaction) invocation.getArguments()[0]).perform()).when(transactionService).execute(any());
    }

    @Test
    public void testGetPropertySpecsDelegatesToPropertySpecService() {
        ForceValidationAndEstimation forceValidationAndEstimation = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = forceValidationAndEstimation.getPropertySpecs(this.propertySpecService);

        // Asserts
        int numberOfSpecs = propertySpecs.size();
        verify(this.propertySpecService, times(numberOfSpecs))
                .basicPropertySpec(
                        anyString(),
                        anyBoolean(),
                        any(ValueFactory.class));
    }

    @Test(expected = ForceValidationAndEstimation.ForceValidationAndEstimationException.class)
    public void executeForceValidationAndEstimationForDeviceForWhichValidationIsNotSet(){
        ForceValidationAndEstimation forceValidationAndEstimation = this.getTestInstance();
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(deviceValidation.isValidationActive()).thenReturn(false);
        when(this.device.forValidation()).thenReturn(deviceValidation);
        forceValidationAndEstimation.execute(this.device, Collections.emptyList());
    }

    @Test(expected = ForceValidationAndEstimation.ForceValidationAndEstimationException.class)
    public void executeForceValidationAndEstimationForDeviceForWhichEstimationIsNotSet(){
        ForceValidationAndEstimation forceValidationAndEstimation = this.getTestInstance();
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(deviceValidation.isValidationActive()).thenReturn(true);
        when(this.device.forValidation()).thenReturn(deviceValidation);
        DeviceEstimation deviceEstimation = mock(DeviceEstimation.class);
        when(deviceEstimation.isEstimationActive()).thenReturn(false);
        when(this.device.forEstimation()).thenReturn(deviceEstimation);
        forceValidationAndEstimation.execute(this.device, Collections.emptyList());
    }

    @Test
    public void executeForceValidationAndEstimation() {
        Instant now = Instant.ofEpochSecond(97L);

        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        when(validationEvaluator.isAllDataValidated(any(MeterActivation.class))).thenReturn(true);

        LoadProfile loadProfile1 = mock(LoadProfile.class);
        LoadProfile.LoadProfileUpdater updater1 = mock(LoadProfile.LoadProfileUpdater.class);
        when(updater1.setLastReadingIfLater(any(Instant.class))).thenReturn(updater1);
        when(this.device.getLoadProfileUpdaterFor(loadProfile1)).thenReturn(updater1);

        LoadProfile loadProfile2 = mock(LoadProfile.class);
        LoadProfile.LoadProfileUpdater updater2 = mock(LoadProfile.LoadProfileUpdater.class);
        when(updater2.setLastReadingIfLater(any(Instant.class))).thenReturn(updater2);
        when(this.device.getLoadProfileUpdaterFor(loadProfile2)).thenReturn(updater2);

        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2));

        ForceValidationAndEstimation forceValidationAndEstimation = this.getTestInstance();
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(deviceValidation.isValidationActive()).thenReturn(true);
        DeviceEstimation deviceEstimation = mock(DeviceEstimation.class);
        when(deviceEstimation.isEstimationActive()).thenReturn(true);
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2));
        when(this.device.forValidation()).thenReturn(deviceValidation);
        when(this.device.forEstimation()).thenReturn(deviceEstimation);
        doReturn(Optional.of(meterActivation)).when(this.device).getCurrentMeterActivation();

        ExecutableActionProperty property = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key());
        when(property.getPropertySpec()).thenReturn(propertySpec);
        when(property.getValue()).thenReturn(now);

        // Business method
        forceValidationAndEstimation.execute(this.device, Collections.singletonList(property));

        // Asserts
        verify(validationService).validate(meterActivation);
        verify(estimationService).estimate(meterActivation, meterActivation.getRange());
    }

    @Test(expected = ForceValidationAndEstimation.ForceValidationAndEstimationException.class)
    public void checkNotAllDataValid() {
        Instant now = Instant.ofEpochSecond(97L);

        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        when(validationEvaluator.isAllDataValidated(any(MeterActivation.class))).thenReturn(false);

        LoadProfile loadProfile1 = mock(LoadProfile.class);
        LoadProfile.LoadProfileUpdater updater1 = mock(LoadProfile.LoadProfileUpdater.class);
        when(updater1.setLastReadingIfLater(any(Instant.class))).thenReturn(updater1);
        when(this.device.getLoadProfileUpdaterFor(loadProfile1)).thenReturn(updater1);

        LoadProfile loadProfile2 = mock(LoadProfile.class);
        LoadProfile.LoadProfileUpdater updater2 = mock(LoadProfile.LoadProfileUpdater.class);
        when(updater2.setLastReadingIfLater(any(Instant.class))).thenReturn(updater2);
        when(this.device.getLoadProfileUpdaterFor(loadProfile2)).thenReturn(updater2);

        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2));

        ForceValidationAndEstimation forceValidationAndEstimation = this.getTestInstance();
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(deviceValidation.isValidationActive()).thenReturn(true);
        DeviceEstimation deviceEstimation = mock(DeviceEstimation.class);
        when(deviceEstimation.isEstimationActive()).thenReturn(true);
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2));
        when(this.device.forValidation()).thenReturn(deviceValidation);
        when(this.device.forEstimation()).thenReturn(deviceEstimation);
        doReturn(Optional.of(meterActivation)).when(this.device).getCurrentMeterActivation();

        ExecutableActionProperty property = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key());
        when(property.getPropertySpec()).thenReturn(propertySpec);
        when(property.getValue()).thenReturn(now);

        // Business method
        forceValidationAndEstimation.execute(this.device, Collections.singletonList(property));

    }

    public ForceValidationAndEstimation getTestInstance() {
        return new ForceValidationAndEstimation(this.threadPrincipalService, this.transactionService, this.validationService, this.estimationService);
    }

}