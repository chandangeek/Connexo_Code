package com.energyict.mdc.device.data.impl.pki.tasks.command;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.metering.MeterActivation;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;

import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeviceFilterTest {

    @Mock
    private Logger logger;
    @Mock
    private SecurityAccessor secAccesor;
    @Mock
    private Device device;
    @Mock
    private Stage stage;
    @Mock
    private MeterActivation meterActivation;

    @Test(expected = CommandAbortException.class)
    public void notAttachedToDevice() throws CommandErrorException, CommandAbortException {
        DeviceFilter deviceFilter = new DeviceFilter(Instant.now());
        deviceFilter.run(secAccesor);
        Mockito.verify(secAccesor, Mockito.times(1)).isEditable();
        Mockito.verify(secAccesor, Mockito.times(1)).getDevice();
        Mockito.verifyNoMoreInteractions(secAccesor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notKnownDeviceStage() throws CommandErrorException, CommandAbortException {
        Mockito.when(secAccesor.getDevice()).thenReturn(device);
        Mockito.when(device.getStage()).thenReturn(stage);
        Mockito.when(stage.getName()).thenReturn("no known stage");
        DeviceFilter deviceFilter = new DeviceFilter(Instant.now());
        deviceFilter.run(secAccesor);
        Mockito.verify(secAccesor, Mockito.times(1)).getDevice();
        Mockito.verify(device, Mockito.times(1)).getStage();
        Mockito.verify(stage, Mockito.times(1)).getName();
        Mockito.verifyNoMoreInteractions(secAccesor, device, stage);
    }

    @Test(expected = CommandAbortException.class)
    public void notActivatedDeviceStage() throws CommandErrorException, CommandAbortException {
        Mockito.when(secAccesor.getDevice()).thenReturn(device);
        Mockito.when(device.getStage()).thenReturn(stage);
        Mockito.when(stage.getName()).thenReturn("mtr.enddevicestage.preoperational");
        DeviceFilter deviceFilter = new DeviceFilter(Instant.now());
        deviceFilter.run(secAccesor);
        Mockito.verify(secAccesor, Mockito.times(1)).getDevice();
        Mockito.verify(device, Mockito.times(1)).getStage();
        Mockito.verify(stage, Mockito.times(1)).getName();
        Mockito.verifyNoMoreInteractions(secAccesor, device, stage);
    }

    @Test(expected = CommandAbortException.class)
    public void noMeterActivation() throws CommandErrorException, CommandAbortException {
        Mockito.when(secAccesor.getDevice()).thenReturn(device);
        Mockito.when(device.getStage()).thenReturn(stage);
        Mockito.when(stage.getName()).thenReturn("mtr.enddevicestage.operational");
        Mockito.when(device.getCurrentMeterActivation()).thenReturn(Optional.empty());
        DeviceFilter deviceFilter = new DeviceFilter(Instant.now());
        deviceFilter.run(secAccesor);
        Mockito.verify(secAccesor, Mockito.times(1)).getDevice();
        Mockito.verify(device, Mockito.times(1)).getStage();
        Mockito.verify(stage, Mockito.times(1)).getName();
        Mockito.verify(device, Mockito.times(1)).getCurrentMeterActivation();
        Mockito.verifyNoMoreInteractions(secAccesor, device, stage);
    }

    @Test(expected = CommandAbortException.class)
    public void meterNotActiveInTimeFrame() throws CommandErrorException, CommandAbortException {
        Mockito.when(secAccesor.getDevice()).thenReturn(device);
        Mockito.when(device.getStage()).thenReturn(stage);
        Mockito.when(stage.getName()).thenReturn("mtr.enddevicestage.operational");
        Mockito.<Optional<? extends MeterActivation>>when(device.getCurrentMeterActivation()).thenReturn(Optional.of(meterActivation));
        Instant aTime = Instant.now();
        Mockito.when(meterActivation.isEffectiveAt(aTime)).thenReturn(false);
        DeviceFilter secAccFilter = new DeviceFilter(aTime);
        secAccFilter.run(secAccesor);
        Mockito.verify(secAccesor, Mockito.times(1)).getDevice();
        Mockito.verify(device, Mockito.times(1)).getStage();
        Mockito.verify(stage, Mockito.times(1)).getName();
        Mockito.verify(device, Mockito.times(1)).getCurrentMeterActivation();
        Mockito.verify(meterActivation, Mockito.times(1)).isEffectiveAt(aTime);
        Mockito.verifyNoMoreInteractions(secAccesor, device, stage, meterActivation);
    }


    @Test
    public void allOk() throws CommandErrorException, CommandAbortException {
        Mockito.when(secAccesor.getDevice()).thenReturn(device);
        Mockito.when(device.getStage()).thenReturn(stage);
        Mockito.when(stage.getName()).thenReturn("mtr.enddevicestage.operational");
        Mockito.<Optional<? extends MeterActivation>>when(device.getCurrentMeterActivation()).thenReturn(Optional.of(meterActivation));
        Instant aTime = Instant.now();
        Mockito.when(meterActivation.isEffectiveAt(aTime)).thenReturn(true);
        DeviceFilter secAccFilter = new DeviceFilter(aTime);
        secAccFilter.run(secAccesor);
        Mockito.verify(secAccesor, Mockito.times(1)).getDevice();
        Mockito.verify(device, Mockito.times(1)).getStage();
        Mockito.verify(stage, Mockito.times(1)).getName();
        Mockito.verify(device, Mockito.times(1)).getCurrentMeterActivation();
        Mockito.verify(meterActivation, Mockito.times(1)).isEffectiveAt(aTime);
        Mockito.verifyNoMoreInteractions(secAccesor, device, stage, meterActivation);
    }
}
