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
public class SecAccFilterTest {

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

    @Test(expected = CommandErrorException.class)
    public void notEditableSecurityAccessor() throws CommandErrorException, CommandAbortException {
        Mockito.when(secAccesor.isEditable()).thenReturn(false);
        SecAccFilter secAccFilter = new SecAccFilter();
        secAccFilter.run(secAccesor);
        Mockito.verify(secAccesor, Mockito.times(1)).isEditable();
        Mockito.verifyNoMoreInteractions(secAccesor);
    }

    @Test
    public void editableSecurityAccessor() throws CommandErrorException, CommandAbortException {
        Mockito.when(secAccesor.isEditable()).thenReturn(true);
        SecAccFilter secAccFilter = new SecAccFilter();
        secAccFilter.run(secAccesor);
        Mockito.verify(secAccesor, Mockito.times(1)).isEditable();
        Mockito.verifyNoMoreInteractions(secAccesor, device, stage, meterActivation);
    }


}
