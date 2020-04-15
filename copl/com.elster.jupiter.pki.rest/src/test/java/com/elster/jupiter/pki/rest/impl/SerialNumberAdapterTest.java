package com.elster.jupiter.pki.rest.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class SerialNumberAdapterTest {

    //0x1511A03EE7442A437B888475514A41CB29D65A8F
    private BigInteger serial = new BigInteger("120281878226337081760368195578922003868746668687");

    private SerialNumberAdapter adapter = new SerialNumberAdapter();


    @Test
    public void testSerialNumberMarshall() {
        assertEquals("0x1511A03EE7442A437B888475514A41CB29D65A8F", adapter.marshal(serial));
    }

    @Test
    public void testSerialNumberUnMarshall() {
        //with 0x ...
        assertEquals(serial, adapter.unmarshal("0x1511A03EE7442A437B888475514A41CB29D65A8F"));

        //with 0X ...
        assertEquals(serial, adapter.unmarshal("0X1511A03EE7442A437B888475514A41CB29D65A8F"));

        //without 0x..
        assertEquals(serial, adapter.unmarshal("1511A03EE7442A437B888475514A41CB29D65A8F"));
    }


}