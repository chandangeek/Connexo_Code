package com.energyict.protocolimplv2.dlms.common.readers;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class CollectedRegisterReaderFinderTest {
    // too many mocks but this is the model to extact dlms class id. Can be refactored, yet logic is simple and straight forward
    @Mock
    private AbstractDlmsProtocol dlmsProtocol;
    @Mock
    private OfflineRegister offlineRegister;
    @Mock
    private DlmsSession dlmsSession;
    @Mock
    private DLMSMeterConfig dlmsMeterConfig;
    @Mock
    private UniversalObject ual;
    @Mock
    private DLMSReaderRegistry<CollectedRegister, OfflineRegister, ObisCode> specificReaders;
    @Mock
    private DLMSReaderRegistry<CollectedRegister, OfflineRegister, DLMSClassId> dataClassReaders;
    @Mock
    private ObisCode obisCode;
    private DLMSClassId dlmsClassId = DLMSClassId.DATA;

    // returned objects (placed here due to assignment warning (see mocking generic typed classes)
    @Mock
    private ObisReader<CollectedRegister, OfflineRegister, ObisCode> specificReader;
    @Mock
    private ObisReader<CollectedRegister, OfflineRegister, DLMSClassId> dataClassReader;

    @Before
    public void setUp() throws NotInObjectListException {
        Mockito.when(dlmsProtocol.getDlmsSession()).thenReturn(dlmsSession);
        Mockito.when(dlmsSession.getMeterConfig()).thenReturn(dlmsMeterConfig);
        Mockito.when(dlmsMeterConfig.findObject(obisCode)).thenReturn(ual);
        Mockito.when(ual.getDLMSClassId()).thenReturn(dlmsClassId);
        Mockito.when(offlineRegister.getObisCode()).thenReturn(obisCode);
    }

    @Test
    public void noReaders() {
        Mockito.when(specificReaders.from(obisCode)).thenReturn(Optional.empty());
        Mockito.when(dataClassReaders.from(dlmsClassId)).thenReturn(Optional.empty());
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?>> obisReader = new CollectedRegisterReaderFinder(specificReaders, dataClassReaders).find(dlmsProtocol, offlineRegister);
        Assert.assertFalse(obisReader.isPresent());
    }


    @Test
    public void specificReader() {
        Mockito.when(specificReaders.from(obisCode)).thenReturn(Optional.of(specificReader));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?>> obisReader = new CollectedRegisterReaderFinder(specificReaders, dataClassReaders).find(dlmsProtocol, offlineRegister);
        Assert.assertTrue(obisReader.isPresent());
    }

    @Test
    public void classReader() {
        Mockito.when(specificReaders.from(obisCode)).thenReturn(Optional.empty());
        Mockito.when(dataClassReaders.from(dlmsClassId)).thenReturn(Optional.of(dataClassReader));
        Optional<? extends ObisReader<CollectedRegister, OfflineRegister, ?>> obisReader = new CollectedRegisterReaderFinder(specificReaders, dataClassReaders).find(dlmsProtocol, offlineRegister);
        Assert.assertTrue(obisReader.isPresent());
    }

}