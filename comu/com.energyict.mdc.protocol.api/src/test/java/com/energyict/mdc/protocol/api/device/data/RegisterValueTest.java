/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.Register;

import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RegisterValueTest {

    private static final ObisCode OBIS = ObisCode.fromString("1.0.1.8.0.255");
    private static final String TEXT = "TestText for register value";
    private static final Date EVENT_TIME = new Date(10);
    private static final Date READ_TIME = new Date(50);
    private static final Date FROM_TIME = new Date(0);
    private static final Date TO_TIME = new Date(20);
    private static final Quantity QUANTITY = new Quantity(5, Unit.get("kWh"));
    private static final Quantity EVENT_QUANTITY = new Quantity(EVENT_TIME.getTime() / 1000, Unit.get(255));
    private static final int REGISTER_ID = 25;
    private static final String SERIAL_NUMBER = "3000-1F3B5A-0105";
    private static final com.energyict.protocol.Register REGISTER = new Register(REGISTER_ID, OBIS, SERIAL_NUMBER);

    @Test
    public void testGetText() throws Exception {
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS).getText());
        assertEquals(TEXT, new com.energyict.protocol.RegisterValue(OBIS, TEXT).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, EVENT_TIME).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, TO_TIME).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getText());
        assertEquals(TEXT, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getText());

        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER).getText());
        assertEquals(TEXT, new com.energyict.protocol.RegisterValue(REGISTER, TEXT).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, EVENT_TIME).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, TO_TIME).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getText());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getText());
        assertEquals(TEXT, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getText());
    }

    @Test
    public void testObisCode() throws Exception {
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(OBIS).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(OBIS, TEXT).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(OBIS, EVENT_TIME).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, TO_TIME).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getObisCode());

        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(REGISTER).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(REGISTER, TEXT).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(REGISTER, EVENT_TIME).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, TO_TIME).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getObisCode());
        assertEquals(OBIS, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getObisCode());
    }

    @Test
    public void testQuantity() throws Exception {
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS).getQuantity());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, TEXT).getQuantity());
        assertEquals(EVENT_QUANTITY, new com.energyict.protocol.RegisterValue(OBIS, EVENT_TIME).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, TO_TIME).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getQuantity());

        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER).getQuantity());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, TEXT).getQuantity());
        assertEquals(EVENT_QUANTITY, new com.energyict.protocol.RegisterValue(REGISTER, EVENT_TIME).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, TO_TIME).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getQuantity());
        assertEquals(QUANTITY, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getQuantity());
    }

    @Test
    public void testEventTime() throws Exception {
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS).getEventTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, TEXT).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(OBIS, EVENT_TIME).getEventTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, TO_TIME).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getEventTime());

        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER).getEventTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, TEXT).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(REGISTER, EVENT_TIME).getEventTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, TO_TIME).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getEventTime());
        assertEquals(EVENT_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getEventTime());
    }

    @Test
    public void testFromTime() throws Exception {
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS).getFromTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, TEXT).getFromTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, EVENT_TIME).getFromTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY).getFromTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME).getFromTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, TO_TIME).getFromTime());
        assertEquals(FROM_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getFromTime());
        assertEquals(FROM_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getFromTime());
        assertEquals(FROM_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getFromTime());
        assertEquals(FROM_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getFromTime());

        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER).getFromTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, TEXT).getFromTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, EVENT_TIME).getFromTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY).getFromTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME).getFromTime());
        assertEquals(null, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, TO_TIME).getFromTime());
        assertEquals(FROM_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getFromTime());
        assertEquals(FROM_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getFromTime());
        assertEquals(FROM_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getFromTime());
        assertEquals(FROM_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getFromTime());
    }

    @Test
    public void testToTime() throws Exception {
        assertNotNull(new com.energyict.protocol.RegisterValue(OBIS).getToTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(OBIS, TEXT).getToTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(OBIS, EVENT_TIME).getToTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(OBIS, QUANTITY).getToTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME).getToTime());
        assertEquals(TO_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, TO_TIME).getToTime());
        assertEquals(TO_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getToTime());
        assertEquals(TO_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getToTime());
        assertEquals(TO_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getToTime());
        assertEquals(TO_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getToTime());

        assertNotNull(new com.energyict.protocol.RegisterValue(REGISTER).getToTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(REGISTER, TEXT).getToTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(REGISTER, EVENT_TIME).getToTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY).getToTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME).getToTime());
        assertEquals(TO_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, TO_TIME).getToTime());
        assertEquals(TO_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getToTime());
        assertEquals(TO_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getToTime());
        assertEquals(TO_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getToTime());
        assertEquals(TO_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getToTime());
    }

    @Test
    public void testReadTime() throws Exception {
        assertNotNull(new com.energyict.protocol.RegisterValue(OBIS).getReadTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(OBIS, TEXT).getReadTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(OBIS, EVENT_TIME).getReadTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(OBIS, QUANTITY).getReadTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME).getReadTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, TO_TIME).getReadTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getReadTime());
        assertEquals(READ_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getReadTime());
        assertEquals(READ_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getReadTime());
        assertEquals(READ_TIME, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getReadTime());

        assertNotNull(new com.energyict.protocol.RegisterValue(REGISTER).getReadTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(REGISTER, TEXT).getReadTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(REGISTER, EVENT_TIME).getReadTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY).getReadTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME).getReadTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, TO_TIME).getReadTime());
        assertNotNull(new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getReadTime());
        assertEquals(READ_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getReadTime());
        assertEquals(READ_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getReadTime());
        assertEquals(READ_TIME, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getReadTime());
    }

    @Test
    public void testRegisterID() throws Exception {
        assertEquals(0, new com.energyict.protocol.RegisterValue(OBIS).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(OBIS, TEXT).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(OBIS, EVENT_TIME).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, TO_TIME).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getRtuRegisterId());
        assertEquals(REGISTER_ID, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getRtuRegisterId());
        assertEquals(REGISTER_ID, new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getRtuRegisterId());

        assertEquals(0, new com.energyict.protocol.RegisterValue(REGISTER).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(REGISTER, TEXT).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(REGISTER, EVENT_TIME).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, TO_TIME).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getRtuRegisterId());
        assertEquals(0, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getRtuRegisterId());
        assertEquals(REGISTER_ID, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getRtuRegisterId());
        assertEquals(REGISTER_ID, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getRtuRegisterId());
    }

    @Test
    public void testSerialNumber() throws Exception {
        assertNull(new com.energyict.protocol.RegisterValue(OBIS).getSerialNumber());
        assertNull(new com.energyict.protocol.RegisterValue(OBIS, TEXT).getSerialNumber());
        assertNull(new com.energyict.protocol.RegisterValue(OBIS, EVENT_TIME).getSerialNumber());
        assertNull(new com.energyict.protocol.RegisterValue(OBIS, QUANTITY).getSerialNumber());
        assertNull(new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME).getSerialNumber());
        assertNull(new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, TO_TIME).getSerialNumber());
        assertNull(new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getSerialNumber());
        assertNull(new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getSerialNumber());
        assertNull(new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getSerialNumber());
        assertNull(new com.energyict.protocol.RegisterValue(OBIS, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getSerialNumber());

        assertEquals(SERIAL_NUMBER, new com.energyict.protocol.RegisterValue(REGISTER).getSerialNumber());
        assertEquals(SERIAL_NUMBER, new com.energyict.protocol.RegisterValue(REGISTER, TEXT).getSerialNumber());
        assertEquals(SERIAL_NUMBER, new com.energyict.protocol.RegisterValue(REGISTER, EVENT_TIME).getSerialNumber());
        assertEquals(SERIAL_NUMBER, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY).getSerialNumber());
        assertEquals(SERIAL_NUMBER, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME).getSerialNumber());
        assertEquals(SERIAL_NUMBER, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, TO_TIME).getSerialNumber());
        assertEquals(SERIAL_NUMBER, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME).getSerialNumber());
        assertEquals(SERIAL_NUMBER, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME).getSerialNumber());
        assertEquals(SERIAL_NUMBER, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID).getSerialNumber());
        assertEquals(SERIAL_NUMBER, new com.energyict.protocol.RegisterValue(REGISTER, QUANTITY, EVENT_TIME, FROM_TIME, TO_TIME, READ_TIME, REGISTER_ID, TEXT).getSerialNumber());
    }

}
