package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;
    @Mock
    private Thesaurus thesaurus;

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
                .specForValuesOf(any(ValueFactory.class));
    }

    @Test
    public void executeForceValidationAndEstimationForDeviceForWhichValidationIsNotSet() {
        ForceValidationAndEstimation forceValidationAndEstimation = this.getTestInstance();
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(deviceValidation.isValidationActive()).thenReturn(false);
        when(this.device.forValidation()).thenReturn(deviceValidation);
        forceValidationAndEstimation.execute(this.device, Instant.now(), Collections.emptyList());

        verify(validationService, never()).validate(any(Set.class), any(ChannelsContainer.class));
        verify(estimationService, never()).estimate(eq(QualityCodeSystem.MDC), any(ChannelsContainer.class), any(Range.class));
    }

    @Test
    public void executeForceValidationAndEstimationForDeviceForWhichEstimationIsNotSet() {
        ForceValidationAndEstimation forceValidationAndEstimation = this.getTestInstance();
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(deviceValidation.isValidationActive()).thenReturn(true);
        when(this.device.forValidation()).thenReturn(deviceValidation);
        DeviceEstimation deviceEstimation = mock(DeviceEstimation.class);
        when(deviceEstimation.isEstimationActive()).thenReturn(false);
        when(this.device.forEstimation()).thenReturn(deviceEstimation);
        forceValidationAndEstimation.execute(this.device, Instant.now(), Collections.emptyList());

        verify(validationService, never()).validate(any(Set.class), any(ChannelsContainer.class));
        verify(estimationService, never()).estimate(eq(QualityCodeSystem.MDC), any(ChannelsContainer.class), any(Range.class));
    }

    @Test
    public void executeForceValidationAndEstimation() {
        Instant now = Instant.ofEpochSecond(97L);

        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        when(validationEvaluator.isAllDataValidated(any(ChannelsContainer.class))).thenReturn(true);

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
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2));
        when(this.device.forValidation()).thenReturn(deviceValidation);
        when(this.device.forEstimation()).thenReturn(deviceEstimation);
        doReturn(Optional.of(meterActivation)).when(this.device).getCurrentMeterActivation();

        // Business method
        forceValidationAndEstimation.execute(this.device, now, Collections.emptyList());

        // Asserts
        verify(validationService).validate(anySetOf(QualityCodeSystem.class), eq(channelsContainer));
        verify(estimationService).estimate(QualityCodeSystem.MDC, channelsContainer, meterActivation.getRange());
    }

    @Test(expected = ForceValidationAndEstimation.ForceValidationAndEstimationException.class)
    public void checkNotAllDataValid() {
        Instant now = Instant.ofEpochSecond(97L);

        when(validationService.getEvaluator()).thenReturn(validationEvaluator);

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
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        Channel channel = mock(Channel.class, Answers.RETURNS_DEEP_STUBS.get());
        when(channelsContainer.getChannels()).thenReturn(Collections.singletonList(channel));
        when(channel.findReadingQualities()
                .ofQualitySystem(QualityCodeSystem.MDC)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .actual()
                .anyMatch())
                .thenReturn(true);
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2));
        when(this.device.forValidation()).thenReturn(deviceValidation);
        when(this.device.forEstimation()).thenReturn(deviceEstimation);
        doReturn(Optional.of(meterActivation)).when(this.device).getCurrentMeterActivation();

        // Business method
        forceValidationAndEstimation.execute(this.device, now, Collections.emptyList());
    }

    public ForceValidationAndEstimation getTestInstance() {
        return new ForceValidationAndEstimation(thesaurus, this.validationService, this.estimationService);
    }

}
