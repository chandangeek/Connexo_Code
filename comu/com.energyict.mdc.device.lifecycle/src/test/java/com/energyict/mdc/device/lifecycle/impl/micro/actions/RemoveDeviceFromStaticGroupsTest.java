package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link RemoveDeviceFromStaticGroups} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-07 (12:40)
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoveDeviceFromStaticGroupsTest {

    private static final long DEVICE_ID = 97L;
    private static final long END_DEVICE_ID = 103L;

    @Mock
    private MeteringService meteringService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private AmrSystem amrSystem;
    @Mock
    private Meter endDevice;
    @Mock
    private Device device;

    @Before
    public void initializeMocks() {
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.endDevice.getId()).thenReturn(END_DEVICE_ID);
    }

    @Test
    public void testGetPropertySpecs() {
        RemoveDeviceFromStaticGroups microAction = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = microAction.getPropertySpecs(this.propertySpecService);

        // Asserts
        assertThat(propertySpecs).isEmpty();
    }

    @Test
    public void executeDoesNotFailWhenMDCSystemDoesNotExist() {
        RemoveDeviceFromStaticGroups microAction = this.getTestInstance();
        when(this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.<AmrSystem>empty());

        // Business method
        microAction.execute(this.device, Collections.emptyList());

        // Asserts
        verifyZeroInteractions(this.meteringGroupsService);
    }

    @Test
    public void executeDoesNotFailWhenKoreMeterDoesNotExist() {
        RemoveDeviceFromStaticGroups microAction = this.getTestInstance();
        when(this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(this.amrSystem));
        when(this.amrSystem.findMeter(String.valueOf(DEVICE_ID))).thenReturn(Optional.<Meter>empty());

        // Business method
        microAction.execute(this.device, Collections.emptyList());

        // Asserts
        verifyZeroInteractions(this.meteringGroupsService);
    }

    @Test
    public void executeWhenDeviceNotUsedInStaticGroups() {
        RemoveDeviceFromStaticGroups microAction = this.getTestInstance();
        when(this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(this.amrSystem));
        when(this.amrSystem.findMeter(String.valueOf(DEVICE_ID))).thenReturn(Optional.of(this.endDevice));
        when(this.meteringGroupsService.findEnumeratedEndDeviceGroupsContaining(this.endDevice)).thenReturn(Collections.emptyList());

        // Business method
        microAction.execute(this.device, Collections.emptyList());

        // Asserts
        verify(this.meteringGroupsService).findEnumeratedEndDeviceGroupsContaining(this.endDevice);
    }

    @Test
    public void executeWhenDeviceUsedInMultipleStaticGroups() {
        RemoveDeviceFromStaticGroups microAction = this.getTestInstance();
        when(this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(this.amrSystem));
        when(this.amrSystem.findMeter(String.valueOf(DEVICE_ID))).thenReturn(Optional.of(this.endDevice));
        EnumeratedEndDeviceGroup.Entry group1Entry = mock(EnumeratedEndDeviceGroup.Entry.class);
        when(group1Entry.getEndDevice()).thenReturn(this.endDevice);
        EnumeratedEndDeviceGroup group1 = mock(EnumeratedEndDeviceGroup.class);
        doReturn(Arrays.asList(group1Entry)).when(group1).getEntries();
        EnumeratedEndDeviceGroup.Entry group2Entry = mock(EnumeratedEndDeviceGroup.Entry.class);
        when(group2Entry.getEndDevice()).thenReturn(this.endDevice);
        EnumeratedEndDeviceGroup group2 = mock(EnumeratedEndDeviceGroup.class);
        doReturn(Arrays.asList(group2Entry)).when(group2).getEntries();
        when(this.meteringGroupsService.findEnumeratedEndDeviceGroupsContaining(this.endDevice)).thenReturn(Arrays.asList(group1, group2));

        // Business method
        microAction.execute(this.device, Collections.emptyList());

        // Asserts
        verify(this.meteringGroupsService).findEnumeratedEndDeviceGroupsContaining(this.endDevice);
        verify(group1).remove(group1Entry);
        verify(group2).remove(group2Entry);
    }

    private RemoveDeviceFromStaticGroups getTestInstance() {
        return new RemoveDeviceFromStaticGroups(this.meteringService, this.meteringGroupsService);
    }

}