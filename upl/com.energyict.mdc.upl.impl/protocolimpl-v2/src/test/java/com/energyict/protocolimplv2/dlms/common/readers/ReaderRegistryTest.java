package com.energyict.protocolimplv2.dlms.common.readers;

import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
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
public class ReaderRegistryTest {

    @Mock
    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, AbstractDlmsProtocol> obisReader;
    @Mock
    private ObisReader<CollectedRegister, OfflineRegister, ObisCode, AbstractDlmsProtocol> anotherObisReader;

    @Test
    public void noReaders() {
        ReaderRegistry<CollectedRegister, OfflineRegister, ObisCode, AbstractDlmsProtocol> reader = new ReaderRegistry<>(new ArrayList<>());
        Assert.assertEquals(Optional.empty(), reader.from(ObisCode.fromString("1.2.3.4.5.6")));
    }

    @Test
    public void noMatchingReader() {
        List<ObisReader<CollectedRegister, OfflineRegister, ObisCode, AbstractDlmsProtocol>> list = new ArrayList<>();
        list.add(obisReader);
        ReaderRegistry<CollectedRegister, OfflineRegister, ObisCode, AbstractDlmsProtocol> reader = new ReaderRegistry<>(list);
        ObisCode o = ObisCode.fromString("1.2.3.4.5.6");
        Mockito.when(obisReader.isApplicable(o)).thenReturn(false);
        Assert.assertEquals(Optional.empty(), reader.from(o));
    }

    @Test
    public void matchingReader() {
        List<ObisReader<CollectedRegister, OfflineRegister, ObisCode, AbstractDlmsProtocol>> list = new ArrayList<>();
        list.add(obisReader);
        ReaderRegistry<CollectedRegister, OfflineRegister, ObisCode, AbstractDlmsProtocol> reader = new ReaderRegistry<>(list);

        ObisCode o = ObisCode.fromString("1.2.3.4.5.6");
        Mockito.when(obisReader.isApplicable(o)).thenReturn(true);
        Assert.assertEquals(Optional.of(obisReader), reader.from(o));
    }


    @Test
    public void multiMatchingReaderListConstructor() {
        List<ObisReader<CollectedRegister, OfflineRegister, ObisCode, AbstractDlmsProtocol>> list = new ArrayList<>();
        list.add(obisReader);
        list.add(anotherObisReader);
        ReaderRegistry<CollectedRegister, OfflineRegister, ObisCode, AbstractDlmsProtocol> reader = new ReaderRegistry<>(list);
        ObisCode o = ObisCode.fromString("1.2.3.4.5.6");
        Mockito.when(obisReader.isApplicable(o)).thenReturn(true);
        Assert.assertEquals(Optional.of(obisReader), reader.from(o));
    }
}