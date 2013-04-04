package com.energyict.dlms;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Copyrights EnergyICT
 * Date: 10/9/12
 * Time: 3:21 PM
 */
public class InvokeIdAndPriorityTest {

    @Test
    public void testGetServiceClass() throws Exception {
        assertNotNull(new InvokeIdAndPriority((byte) 0x00).getServiceClass());
        assertNotNull(new InvokeIdAndPriority((byte) 0x40).getServiceClass());
        assertEquals(InvokeIdAndPriority.ServiceClass.UNCONFIRMED, new InvokeIdAndPriority((byte) 0x00).getServiceClass());
        assertEquals(InvokeIdAndPriority.ServiceClass.CONFIRMED, new InvokeIdAndPriority((byte) 0x40).getServiceClass());
    }

    @Test(expected = DLMSConnectionException.class)
    public void testSetServiceClassNull() throws Exception {
        new InvokeIdAndPriority((byte) 0x00).setServiceClass(null);
    }

    @Test(expected = DLMSConnectionException.class)
    public void testSetServiceClassInvalid() throws Exception {
        new InvokeIdAndPriority((byte) 0x00).setServiceClass(7);
    }

    @Test
    public void testSetCorrectServiceClassValue() throws Exception {
        InvokeIdAndPriority iiap;

        // Test setter starting from unconfirmed
        iiap = new InvokeIdAndPriority((byte) 0x00);
        assertEquals(InvokeIdAndPriority.ServiceClass.UNCONFIRMED, iiap.getServiceClass());

        iiap.setServiceClass(1);
        assertEquals(InvokeIdAndPriority.ServiceClass.CONFIRMED, iiap.getServiceClass());

        iiap.setServiceClass(0);
        assertEquals(InvokeIdAndPriority.ServiceClass.UNCONFIRMED, iiap.getServiceClass());

        // Test setter starting from confirmed
        iiap = new InvokeIdAndPriority((byte) 0x40);
        assertEquals(InvokeIdAndPriority.ServiceClass.CONFIRMED, iiap.getServiceClass());

        iiap.setServiceClass(0);
        assertEquals(InvokeIdAndPriority.ServiceClass.UNCONFIRMED, iiap.getServiceClass());

        iiap.setServiceClass(1);
        assertEquals(InvokeIdAndPriority.ServiceClass.CONFIRMED, iiap.getServiceClass());
    }

    @Test
    public void testSetCorrectServiceClassEnum() throws Exception {
        InvokeIdAndPriority iiap;

        // Test setter starting from unconfirmed
        iiap = new InvokeIdAndPriority((byte) 0x00);
        assertEquals(InvokeIdAndPriority.ServiceClass.UNCONFIRMED, iiap.getServiceClass());

        iiap.setServiceClass(InvokeIdAndPriority.ServiceClass.CONFIRMED);
        assertEquals(InvokeIdAndPriority.ServiceClass.CONFIRMED, iiap.getServiceClass());

        iiap.setServiceClass(InvokeIdAndPriority.ServiceClass.UNCONFIRMED);
        assertEquals(InvokeIdAndPriority.ServiceClass.UNCONFIRMED, iiap.getServiceClass());

        // Test setter starting from confirmed
        iiap = new InvokeIdAndPriority((byte) 0x40);
        assertEquals(InvokeIdAndPriority.ServiceClass.CONFIRMED, iiap.getServiceClass());

        iiap.setServiceClass(InvokeIdAndPriority.ServiceClass.UNCONFIRMED);
        assertEquals(InvokeIdAndPriority.ServiceClass.UNCONFIRMED, iiap.getServiceClass());

        iiap.setServiceClass(InvokeIdAndPriority.ServiceClass.CONFIRMED);
        assertEquals(InvokeIdAndPriority.ServiceClass.CONFIRMED, iiap.getServiceClass());
    }

    @Test
    public void testGetPriority() throws Exception {
        assertNotNull(new InvokeIdAndPriority((byte) 0x00).getPriority());
        assertNotNull(new InvokeIdAndPriority((byte) 0x80).getPriority());
        assertEquals(InvokeIdAndPriority.Priority.NORMAL, new InvokeIdAndPriority((byte) 0x00).getPriority());
        assertEquals(InvokeIdAndPriority.Priority.HIGH, new InvokeIdAndPriority((byte) 0x80).getPriority());
    }

    @Test(expected = DLMSConnectionException.class)
    public void testSetPriorityNull() throws Exception {
        new InvokeIdAndPriority((byte) 0x00).setPriority(null);
    }

    @Test(expected = DLMSConnectionException.class)
    public void testSetPriorityInvalid() throws Exception {
        new InvokeIdAndPriority((byte) 0x00).setPriority(7);
    }

    @Test
    public void testSetCorrectPriorityValue() throws Exception {
        InvokeIdAndPriority iiap;

        // Test setter starting from normal
        iiap = new InvokeIdAndPriority((byte) 0x00);
        assertEquals(InvokeIdAndPriority.Priority.NORMAL, iiap.getPriority());

        iiap.setPriority(1);
        assertEquals(InvokeIdAndPriority.Priority.HIGH, iiap.getPriority());

        iiap.setPriority(0);
        assertEquals(InvokeIdAndPriority.Priority.NORMAL, iiap.getPriority());

        // Test setter starting from high
        iiap = new InvokeIdAndPriority((byte) 0x80);
        assertEquals(InvokeIdAndPriority.Priority.HIGH, iiap.getPriority());

        iiap.setPriority(0);
        assertEquals(InvokeIdAndPriority.Priority.NORMAL, iiap.getPriority());

        iiap.setPriority(1);
        assertEquals(InvokeIdAndPriority.Priority.HIGH, iiap.getPriority());
    }

    @Test
    public void testSetCorrectPriorityEnum() throws Exception {
        InvokeIdAndPriority iiap;

        // Test setter starting from normal
        iiap = new InvokeIdAndPriority((byte) 0x00);
        assertEquals(InvokeIdAndPriority.Priority.NORMAL, iiap.getPriority());

        iiap.setPriority(InvokeIdAndPriority.Priority.HIGH);
        assertEquals(InvokeIdAndPriority.Priority.HIGH, iiap.getPriority());

        iiap.setPriority(InvokeIdAndPriority.Priority.NORMAL);
        assertEquals(InvokeIdAndPriority.Priority.NORMAL, iiap.getPriority());

        // Test setter starting from high
        iiap = new InvokeIdAndPriority((byte) 0x80);
        assertEquals(InvokeIdAndPriority.Priority.HIGH, iiap.getPriority());

        iiap.setPriority(InvokeIdAndPriority.Priority.NORMAL);
        assertEquals(InvokeIdAndPriority.Priority.NORMAL, iiap.getPriority());

        iiap.setPriority(InvokeIdAndPriority.Priority.HIGH);
        assertEquals(InvokeIdAndPriority.Priority.HIGH, iiap.getPriority());
    }

    @Test
    public void setTheInvokeIdTest() throws DLMSConnectionException {
        InvokeIdAndPriority iiap = new InvokeIdAndPriority((byte) 0xF0);    // All priority bits are set - these should not be modified when setting the invoke id

        // Business methods
        iiap.setTheInvokeId(0x00);
        assertEquals("InvokeIdAndPriority does not match.", (byte) 0xF0, iiap.getInvokeIdAndPriorityData());
        iiap.setTheInvokeId(0x01);
        assertEquals("InvokeIdAndPriority does not match.", (byte) 0xF1, iiap.getInvokeIdAndPriorityData());
        iiap.setTheInvokeId(0x05);
        assertEquals("InvokeIdAndPriority does not match.", (byte) 0xF5, iiap.getInvokeIdAndPriorityData());
        iiap.setTheInvokeId(0x0F);
        assertEquals("InvokeIdAndPriority does not match.", (byte) 0xFF, iiap.getInvokeIdAndPriorityData());
    }

    @Test(expected = DLMSConnectionException.class)
    public void setInvalidInvokeIdTest() throws DLMSConnectionException {
        InvokeIdAndPriority iiap = new InvokeIdAndPriority((byte) 0xF0);    // All priority bits are set - these should not be modified when setting the invoke id

        // Business methods
        iiap.setTheInvokeId(0x11);  // Should throw a DLMSConnectionException, cause the invokeId is > 15.
    }
}