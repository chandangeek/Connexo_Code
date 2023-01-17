package com.energyict.mdc.device.data.impl.pki.tasks.command;


import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.impl.pki.tasks.BpmProcessResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TriggerBpmTest {

    @Mock
    private BpmService bpmService;
    @Mock
    private BpmProcessResolver bpmProcessResolver;
    @Mock
    private SecurityAccessor securityAccessor;
    @Mock
    private BpmProcessDefinition bpmProces;
    @Mock
    private Device device;
    @Mock
    private SecurityAccessorType keyAccType;
    @Mock
    private Logger logger;


    @Test(expected = CommandErrorException.class)
    public void noProcessDefinition() throws CommandErrorException, CommandAbortException {
        String bpmProcessId = "bpmProcessId";
        Mockito.when(bpmProcessResolver.resolve(bpmProcessId)).thenReturn(Optional.empty());
        TriggerBpm triggerBpm = new TriggerBpm(bpmService, bpmProcessResolver, bpmProcessId, logger);
        triggerBpm.run(securityAccessor);
        Mockito.verify(bpmProcessResolver, Mockito.times(1)).resolve(bpmProcessId);
        Mockito.verifyNoMoreInteractions(bpmService, bpmProcessResolver);
    }

    @Test(expected = CommandAbortException.class)
    public void processAlreadyStarted() throws CommandErrorException, CommandAbortException {
        String bpmProcessId = "bpmProcessId";
        Mockito.when(bpmProcessResolver.resolve(bpmProcessId)).thenReturn(Optional.of(bpmProces));
        Mockito.when(bpmProcessResolver.canBeStarted(securityAccessor, bpmProcessId)).thenReturn(false);
        TriggerBpm triggerBpm = new TriggerBpm(bpmService, bpmProcessResolver, bpmProcessId, logger);
        triggerBpm.run(securityAccessor);
        Mockito.verify(bpmProcessResolver, Mockito.times(1)).resolve(bpmProcessId);
        Mockito.verify(bpmProcessResolver, Mockito.times(1)).canBeStarted(securityAccessor, bpmProcessId);
        Mockito.verifyNoMoreInteractions(bpmService, bpmProcessResolver);
    }

    @Test
    public void triggerProcess() throws CommandErrorException, CommandAbortException {
        String bpmProcessId = "bpmProcessId";
        Mockito.when(bpmProcessResolver.resolve(bpmProcessId)).thenReturn(Optional.of(bpmProces));
        Mockito.when(bpmProcessResolver.canBeStarted(securityAccessor, bpmProcessId)).thenReturn(true);
        Mockito.when(securityAccessor.getDevice()).thenReturn(device);
        Mockito.when(device.getmRID()).thenReturn("mrId");
        Mockito.when(securityAccessor.getSecurityAccessorType()).thenReturn(keyAccType);
        Mockito.when(keyAccType.getName()).thenReturn("keyAccType");
        TriggerBpm triggerBpm = new TriggerBpm(bpmService, bpmProcessResolver, bpmProcessId, logger);
        triggerBpm.run(securityAccessor);
        Mockito.verify(bpmProcessResolver, Mockito.times(1)).resolve(bpmProcessId);
        Mockito.verify(bpmProcessResolver, Mockito.times(1)).canBeStarted(securityAccessor, bpmProcessId);
        Map<String, Object> expectedParams = new HashMap<>();
        expectedParams.put("deviceId", securityAccessor.getDevice().getmRID());
        expectedParams.put("accessorType", securityAccessor.getSecurityAccessorType().getName());
        Mockito.verify(bpmService, Mockito.times(1)).startProcess(bpmProces, expectedParams);
        Mockito.verifyNoMoreInteractions(bpmService, bpmProcessResolver);
    }


}
