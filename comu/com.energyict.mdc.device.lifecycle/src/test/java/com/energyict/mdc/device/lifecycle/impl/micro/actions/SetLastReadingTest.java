/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.device.data.LogBook;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SetLastReading} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-05 (09:51)
 */
@RunWith(MockitoJUnitRunner.class)
public class SetLastReadingTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;
    @Mock
    private Thesaurus thesaurus;

    @Test
    public void testGetPropertySpecsDelegatesToPropertySpecService() {
        SetLastReading microAction = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = microAction.getPropertySpecs(this.propertySpecService);

        // Asserts
        int numberOfSpecs = propertySpecs.size();
        verify(this.propertySpecService, times(numberOfSpecs))
                .specForValuesOf(any(ValueFactory.class));
    }

    @Test
    public void executeSetsLastReadingOnAllLogbooks() {
        Instant now = Instant.ofEpochSecond(97L);
        SetLastReading microAction = this.getTestInstance();
        LogBook logBook1 = mock(LogBook.class);
        LogBook.LogBookUpdater updater1 = mock(LogBook.LogBookUpdater.class);
        when(updater1.setLastReadingIfLater(any(Instant.class))).thenReturn(updater1);
        when(this.device.getLogBookUpdaterFor(logBook1)).thenReturn(updater1);
        LogBook logBook2 = mock(LogBook.class);
        LogBook.LogBookUpdater updater2 = mock(LogBook.LogBookUpdater.class);
        when(updater2.setLastReadingIfLater(any(Instant.class))).thenReturn(updater2);
        when(this.device.getLogBookUpdaterFor(logBook2)).thenReturn(updater2);
        when(this.device.getLogBooks()).thenReturn(Arrays.asList(logBook1, logBook2));
        when(this.device.getLoadProfiles()).thenReturn(Collections.emptyList());

        // Business method
        microAction.execute(this.device, now, Collections.emptyList());

        // Asserts
        verify(updater1).setLastReadingIfLater(now);
        verify(updater1).update();
        verify(updater2).setLastReadingIfLater(now);
        verify(updater2).update();
    }

    @Test
    public void executeSetsLastReadingOnAllLoadProfiles() {
        Instant now = Instant.ofEpochSecond(97L);
        SetLastReading microAction = this.getTestInstance();
        LoadProfile loadProfile1 = mock(LoadProfile.class);
        LoadProfile.LoadProfileUpdater updater1 = mock(LoadProfile.LoadProfileUpdater.class);
        when(updater1.setLastReadingIfLater(any(Instant.class))).thenReturn(updater1);
        when(this.device.getLoadProfileUpdaterFor(loadProfile1)).thenReturn(updater1);
        LoadProfile loadProfile2 = mock(LoadProfile.class);
        LoadProfile.LoadProfileUpdater updater2 = mock(LoadProfile.LoadProfileUpdater.class);
        when(updater2.setLastReadingIfLater(any(Instant.class))).thenReturn(updater2);
        when(this.device.getLoadProfileUpdaterFor(loadProfile2)).thenReturn(updater2);
        when(this.device.getLogBooks()).thenReturn(Collections.emptyList());
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2));

        // Business method
        microAction.execute(this.device, now, Collections.emptyList());

        // Asserts
        verify(updater1).setLastReadingIfLater(now);
        verify(updater1).update();
        verify(updater2).setLastReadingIfLater(now);
        verify(updater2).update();
    }

    private SetLastReading getTestInstance() {
        return new SetLastReading(thesaurus);
    }

}