package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.validation.ValidationService;

import java.time.Instant;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointTimeResolverTest {

    @Mock
    public ValidationService validationService;

    @Mock
    public ChannelsContainer channelsContainer;

    public UsagePointTimeResolver timeResolver = new UsagePointTimeResolver();

    @Before
    public void before() {
        validationService = Mockito.mock(ValidationService.class);
        channelsContainer = Mockito.mock(ChannelsContainer.class);
    }

    @Test(expected = RuntimeException.class)
    public void testAllNull() {
        Mockito.when(validationService.getLastChecked(channelsContainer)).thenReturn(Optional.empty());
        Mockito.when(channelsContainer.getStart()).thenReturn(null);

        timeResolver.resolveLastCheck(validationService, channelsContainer, null);

    }

    @Test
    public void testNullValidationServiceLastCheckedAndNullChannelLastCheckValidationService() {
        Mockito.when(validationService.getLastChecked(channelsContainer)).thenReturn(Optional.empty());
        Mockito.when(channelsContainer.getStart()).thenReturn(null);

        Instant candidate = Instant.now();
        Instant resolved = timeResolver.resolveLastCheck(validationService, channelsContainer, candidate);
        Assert.assertEquals(candidate, resolved);
    }

    @Test
    public void testNullCandidateLastCheckFromValidationService() {
        Instant now = Instant.now();
        Mockito.when(validationService.getLastChecked(channelsContainer)).thenReturn(Optional.of(now));
        Mockito.when(channelsContainer.getStart()).thenReturn(null);


        Instant resolved = timeResolver.resolveLastCheck(validationService, channelsContainer, null);
        Assert.assertEquals(now, resolved);
    }

    @Test
    public void testNullCandidateLastCheckFromChannelContainer() {
        Instant now = Instant.now();
        Mockito.when(validationService.getLastChecked(channelsContainer)).thenReturn(Optional.empty());
        Mockito.when(channelsContainer.getStart()).thenReturn(now);


        Instant resolved = timeResolver.resolveLastCheck(validationService, channelsContainer, null);
        Assert.assertEquals(now, resolved);
    }

    @Test
    public void testValidationServiceBeforeCandidate() {
        Instant validationServiceInstant = Instant.now();
        Mockito.when(validationService.getLastChecked(channelsContainer)).thenReturn(Optional.empty());
        Mockito.when(channelsContainer.getStart()).thenReturn(validationServiceInstant);

        Instant candidate = validationServiceInstant.plusMillis(100);
        Instant resolved = timeResolver.resolveLastCheck(validationService, channelsContainer, candidate);
        Assert.assertEquals(validationServiceInstant.plusMillis(1), resolved);
    }

    @Test
    public void testValidationServiceAfterCandidate() {
        Instant validationServiceInstant = Instant.now();
        Mockito.when(validationService.getLastChecked(channelsContainer)).thenReturn(Optional.empty());
        Mockito.when(channelsContainer.getStart()).thenReturn(validationServiceInstant);

        Instant candidate = validationServiceInstant.minusMillis(100);
        Instant resolved = timeResolver.resolveLastCheck(validationService, channelsContainer, candidate);
        Assert.assertEquals(candidate.plusMillis(1), resolved);
    }


}
