package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Header;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Shipment;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.WrapKey;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.exception.ImportFailedException;

import org.w3._2001._04.xmlenc_.CipherDataType;
import org.w3._2001._04.xmlenc_.EncryptedKeyType;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransportKeysTest {

    @Mock
    public Shipment shipment;

    @Mock
    public Header header;

    @Mock
    public WrapKey key1;

    @Mock
    public WrapKey key2;

    @Mock
    public EncryptedKeyType encryptedKeyType;

    @Mock
    public CipherDataType cipherData;

    byte[] cipherDataBytes = "cipherBytes".getBytes();

    @Test(expected = ImportFailedException.class)
    public void testNullShipment() {
        new TransportKeys(null);
    }

    @Test(expected = ImportFailedException.class)
    public void testNullHeader() {
        new TransportKeys(shipment);
    }

    @Test(expected = ImportFailedException.class)
    public void testNullKeys() {
        Mockito.when(shipment.getHeader()).thenReturn(header);
        Mockito.when(header.getWrapKey()).thenReturn(null);
        new TransportKeys(shipment);
    }

    @Test(expected = ImportFailedException.class)
    public void testSingleKeyNoBytesAvailableForLabel() {
        String key1Label = "key1";
        Mockito.when(this.key1.getLabel()).thenReturn(key1Label);
        Mockito.when(this.key1.getSymmetricKey()).thenReturn(encryptedKeyType);
        Mockito.when(this.encryptedKeyType.getCipherData()).thenReturn(cipherData);
        List<WrapKey> keys = Arrays.asList(this.key1);

        Mockito.when(shipment.getHeader()).thenReturn(header);
        Mockito.when(header.getWrapKey()).thenReturn(keys);
        new TransportKeys(shipment);

    }

    @Test(expected = ImportFailedException.class)
    public void testSingleKeyNoCipherDataAvailableForLabel() {
        String key1Label = "key1";
        Mockito.when(this.key1.getLabel()).thenReturn(key1Label);
        Mockito.when(this.key1.getSymmetricKey()).thenReturn(encryptedKeyType);
        List<WrapKey> keys = Arrays.asList(this.key1);

        Mockito.when(shipment.getHeader()).thenReturn(header);
        Mockito.when(header.getWrapKey()).thenReturn(keys);
        new TransportKeys(shipment);
    }

    @Test(expected = ImportFailedException.class)
    public void testSingleKeyEncKeyAvailableForLabel() {
        String key1Label = "key1";
        Mockito.when(this.key1.getLabel()).thenReturn(key1Label);
        List<WrapKey> keys = Arrays.asList(this.key1);

        Mockito.when(shipment.getHeader()).thenReturn(header);
        Mockito.when(header.getWrapKey()).thenReturn(keys);
        new TransportKeys(shipment);
    }

    @Test
    public void testSingleKey() {
        String key1Label = "key1";
        Mockito.when(this.key1.getLabel()).thenReturn(key1Label);
        Mockito.when(this.key1.getSymmetricKey()).thenReturn(encryptedKeyType);
        Mockito.when(this.encryptedKeyType.getCipherData()).thenReturn(cipherData);
        Mockito.when(this.cipherData.getCipherValue()).thenReturn(cipherDataBytes);
        List<WrapKey> keys = Arrays.asList(this.key1);

        Mockito.when(shipment.getHeader()).thenReturn(header);
        Mockito.when(header.getWrapKey()).thenReturn(keys);
        TransportKeys transportKeys = new TransportKeys(shipment);

        Assert.assertEquals(this.key1, transportKeys.get(key1Label));
        Assert.assertEquals(this.cipherDataBytes, transportKeys.getBytes(key1Label));
    }

    @Test
    public void testMultipleKey() {
        String key1Label1 = "key1";
        Mockito.when(this.key1.getLabel()).thenReturn(key1Label1);
        Mockito.when(this.key1.getSymmetricKey()).thenReturn(encryptedKeyType);
        Mockito.when(this.encryptedKeyType.getCipherData()).thenReturn(cipherData);
        Mockito.when(this.cipherData.getCipherValue()).thenReturn(cipherDataBytes);


        String key1Label2 = "key2";
        Mockito.when(this.key2.getLabel()).thenReturn(key1Label2);
        Mockito.when(this.key2.getSymmetricKey()).thenReturn(encryptedKeyType);
        Mockito.when(this.encryptedKeyType.getCipherData()).thenReturn(cipherData);
        Mockito.when(this.cipherData.getCipherValue()).thenReturn(cipherDataBytes);

        List<WrapKey> keys = Arrays.asList(this.key1, this.key2);

        Mockito.when(shipment.getHeader()).thenReturn(header);
        Mockito.when(header.getWrapKey()).thenReturn(keys);
        TransportKeys transportKeys = new TransportKeys(shipment);

        Assert.assertEquals(this.key1, transportKeys.get(key1Label1));
        Assert.assertEquals(this.cipherDataBytes, transportKeys.getBytes(key1Label1));

        Assert.assertEquals(this.key2, transportKeys.get(key1Label2));
    }

    @Test(expected = ImportFailedException.class)
    public void testMultipleKeyNoMatchFoundOnGet() {
        String key1Label1 = "key1";
        Mockito.when(this.key1.getLabel()).thenReturn(key1Label1);
        Mockito.when(this.key1.getSymmetricKey()).thenReturn(encryptedKeyType);
        Mockito.when(this.encryptedKeyType.getCipherData()).thenReturn(cipherData);
        Mockito.when(this.cipherData.getCipherValue()).thenReturn(cipherDataBytes);


        String key1Label2 = "key2";
        Mockito.when(this.key2.getLabel()).thenReturn(key1Label2);
        Mockito.when(this.key2.getSymmetricKey()).thenReturn(encryptedKeyType);
        Mockito.when(this.encryptedKeyType.getCipherData()).thenReturn(cipherData);
        Mockito.when(this.cipherData.getCipherValue()).thenReturn(cipherDataBytes);

        List<WrapKey> keys = Arrays.asList(this.key1, this.key2);

        Mockito.when(shipment.getHeader()).thenReturn(header);
        Mockito.when(header.getWrapKey()).thenReturn(keys);
        TransportKeys transportKeys = new TransportKeys(shipment);

        Assert.assertEquals(this.key1, transportKeys.get("xxx"));
    }


}
