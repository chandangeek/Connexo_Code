package com.energyict.protocolimpl.instromet.v555.tables;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author sva
 * @since 4/01/2016 - 17:17
 */
public class LoggedDataTableTest {

    private LoggedDataTable loggedDataTable;

    @Before
    public void setUp() throws Exception {
        loggedDataTable = new LoggedDataTable(null);
    }

    @Test
    public void testConvertYear() throws Exception {
        assertEquals(2015, loggedDataTable.convertYear((byte) 0xF0));
        assertEquals(2014, loggedDataTable.convertYear((byte) 0xE0));

        assertEquals(2016, loggedDataTable.convertYear((byte) 0x00));
        assertEquals(2017, loggedDataTable.convertYear((byte) 0x10));
        assertEquals(2029, loggedDataTable.convertYear((byte) 0xD0));
    }

}