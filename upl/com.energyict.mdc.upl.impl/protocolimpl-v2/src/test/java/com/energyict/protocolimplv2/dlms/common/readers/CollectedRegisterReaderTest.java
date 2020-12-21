package com.energyict.protocolimplv2.dlms.common.readers;

import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RunWith(MockitoJUnitRunner.class)
public class CollectedRegisterReaderTest {

    @Mock
    private CollectedRegisterReaderFinder collectedRegisterReaderFinder;
    @Mock
    private AbstractDlmsProtocol dlmsProtocol;
    @Mock
    private CollectedRegisterBuilder collectedRegisterBuilder;

    @Mock
    private OfflineRegister offlineRegister;
    @Mock
    private RegisterValue registerValue;

    @Mock
    private CollectedRegister mockedCollectedRegister;
    @Mock
    private ObisReader<CollectedRegister, OfflineRegister, ?> reader;

    @Test
    public void noOfflineRegisters() {
        CollectedRegisterReader collectedRegisterReader = new CollectedRegisterReader(collectedRegisterReaderFinder, dlmsProtocol, collectedRegisterBuilder);
        List<CollectedRegister> collectedRegisters = collectedRegisterReader.readRegisters(new ArrayList<>());
        Assert.assertTrue(collectedRegisters.isEmpty());
    }

    @Test
    public void noReaders() {
        Mockito.when(collectedRegisterReaderFinder.find(dlmsProtocol, offlineRegister)).thenReturn(Optional.empty());
        CollectedRegisterReader collectedRegisterReader = new CollectedRegisterReader(collectedRegisterReaderFinder, dlmsProtocol, collectedRegisterBuilder);
        ArrayList<OfflineRegister> offlineRegisters = new ArrayList<>();
        offlineRegisters.add(offlineRegister);
        List<CollectedRegister> collectedRegisters = collectedRegisterReader.readRegisters(offlineRegisters);
        Assert.assertFalse(collectedRegisters.isEmpty());
        Assert.assertEquals(collectedRegisters.size(), offlineRegisters.size());
        CollectedRegister collectedRegister = collectedRegisters.get(0);
        Mockito.verify(collectedRegisterBuilder, Mockito.times(1)).createCollectedRegister(offlineRegister, ResultType.NotSupported, "No reader found");
    }

    @Test
    public void foundReader() {
        Mockito.<Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?>>>when(collectedRegisterReaderFinder.find(dlmsProtocol, offlineRegister)).thenReturn(Optional.of(reader));
        Mockito.when(reader.read(dlmsProtocol, offlineRegister)).thenReturn(mockedCollectedRegister);
        CollectedRegisterReader collectedRegisterReader = new CollectedRegisterReader(collectedRegisterReaderFinder, dlmsProtocol, collectedRegisterBuilder);
        ArrayList<OfflineRegister> offlineRegisters = new ArrayList<>();
        offlineRegisters.add(offlineRegister);
        List<CollectedRegister> collectedRegisters = collectedRegisterReader.readRegisters(offlineRegisters);
        Assert.assertFalse(collectedRegisters.isEmpty());
        Assert.assertEquals(collectedRegisters.size(), offlineRegisters.size());
        Assert.assertEquals(collectedRegisters.get(0), mockedCollectedRegister);
    }

}