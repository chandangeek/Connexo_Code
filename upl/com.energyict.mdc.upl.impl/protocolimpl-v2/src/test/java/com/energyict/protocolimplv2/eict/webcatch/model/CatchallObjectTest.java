package com.energyict.protocolimplv2.eict.webcatch.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CatchallObjectTest {

    @Test
    public void test() throws IOException {
        CatchallObject catchallObject = new ObjectMapper().readValue(new ServletInputStreamCorrectOrder(), CatchallObject.class);
        System.out.println(catchallObject.toString());
        assertEquals("1.0", catchallObject.getVersion());
        assertEquals("B229K3X0B1", catchallObject.getSerial());
        assertEquals("10.113.40.47", catchallObject.getIp());
        assertEquals("1658751300", catchallObject.getUtcstamp());
        assertEquals(4, catchallObject.getDevices().size());
        assertEquals(new BigDecimal("1070715.000"), catchallObject.getDevices().get(0).getValues().get(0).getValue());
        assertEquals(new BigDecimal("50054.000"), catchallObject.getDevices().get(1).getValues().get(0).getValue());
        assertEquals(new BigDecimal("4.000"), catchallObject.getDevices().get(2).getValues().get(0).getValue());
        assertEquals(new BigDecimal("2.000"), catchallObject.getDevices().get(3).getValues().get(0).getValue());
    }
}
