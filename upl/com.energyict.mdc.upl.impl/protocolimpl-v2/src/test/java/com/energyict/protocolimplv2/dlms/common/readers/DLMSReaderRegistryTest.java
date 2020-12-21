package com.energyict.protocolimplv2.dlms.common.readers;

import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class DLMSReaderRegistryTest {

    @Mock
    private ObisReader<CollectedRegister, OfflineRegister, ObisCode> obisReader;
    @Mock
    private ObisReader<CollectedRegister, OfflineRegister, ObisCode> anotherObisReader;

    @Test
    public void noReaders() {
        DLMSReaderRegistry<CollectedRegister, OfflineRegister, ObisCode> reader = new DLMSReaderRegistry<>();
        Assert.assertEquals(Optional.empty(), reader.from(ObisCode.fromString("1.2.3.4.5.6")));
    }

    @Test
    public void noMatchingReader() {
        DLMSReaderRegistry<CollectedRegister, OfflineRegister, ObisCode> reader = new DLMSReaderRegistry<>();
        reader.add(obisReader);

        ObisCode o = ObisCode.fromString("1.2.3.4.5.6");
        Mockito.when(obisReader.isApplicable(o)).thenReturn(false);
        Assert.assertEquals(Optional.empty(), reader.from(o));
    }

    @Test
    public void matchingReader() {
        DLMSReaderRegistry<CollectedRegister, OfflineRegister, ObisCode> reader = new DLMSReaderRegistry<>();
        reader.add(obisReader);

        ObisCode o = ObisCode.fromString("1.2.3.4.5.6");
        Mockito.when(obisReader.isApplicable(o)).thenReturn(true);
        Assert.assertEquals(Optional.of(obisReader), reader.from(o));
    }

    @Test
    public void multiMatchingReader() {
        DLMSReaderRegistry<CollectedRegister, OfflineRegister, ObisCode> reader = new DLMSReaderRegistry<>();
        reader.add(obisReader);
        reader.add(anotherObisReader);

        ObisCode o = ObisCode.fromString("1.2.3.4.5.6");
        Mockito.when(obisReader.isApplicable(o)).thenReturn(true);
        Assert.assertEquals(Optional.of(obisReader), reader.from(o));
    }

}