package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.protocol.meteridentification.MeterType;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Copyrights EnergyICT
 * Date: 16-sep-2010
 * Time: 9:42:57
 */
public class ABBA1700MeterTypeTest {

    private static final String IDENT_OLD = "/GEC5090100140400@000";
    private static final String IDENT_NEW = "/GEC5090100100400@000";
    private static final String IDENT_NEW_EXTENDED = "/GEC2090100260200@000";


    @Test
    public void testIsAssigned() throws Exception {
        assertFalse(new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_UNASSIGNED).isAssigned());
        assertTrue(new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_16_TOU).isAssigned());
        assertTrue(new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_32_TOU).isAssigned());
        assertTrue(new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_32_TOU_5_CDR).isAssigned());
        for (int type = 3; type < 255; type++) {
            assertFalse(new ABBA1700MeterType(type).isAssigned());
        }
        assertTrue(new ABBA1700MeterType(new MeterType(IDENT_OLD)).isAssigned());
        assertTrue(new ABBA1700MeterType(new MeterType(IDENT_NEW)).isAssigned());
        assertTrue(new ABBA1700MeterType(new MeterType(IDENT_NEW_EXTENDED)).isAssigned());
    }

    @Test
    public void testUpdateWith() throws Exception {
        ABBA1700MeterType type;

        type = new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_UNASSIGNED);
        assertFalse(type.isAssigned());
        type.updateWith(new MeterType(IDENT_OLD));
        assertTrue(type.isAssigned());

        type = new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_UNASSIGNED);
        assertFalse(type.isAssigned());
        type.updateWith(new MeterType(IDENT_NEW));
        assertTrue(type.isAssigned());

        type = new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_UNASSIGNED);
        assertFalse(type.isAssigned());
        type.updateWith(new MeterType(IDENT_NEW_EXTENDED));
        assertTrue(type.isAssigned());
    }

    @Test
    public void testGetNrOfTariffRegisters() throws Exception {
        assertEquals(-1, new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_UNASSIGNED).getNrOfTariffRegisters());
        assertEquals(16, new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_16_TOU).getNrOfTariffRegisters());
        assertEquals(32, new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_32_TOU).getNrOfTariffRegisters());
        assertEquals(32, new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_32_TOU_5_CDR).getNrOfTariffRegisters());
        assertEquals(16, new ABBA1700MeterType(new MeterType(IDENT_OLD)).getNrOfTariffRegisters());
        assertEquals(32, new ABBA1700MeterType(new MeterType(IDENT_NEW)).getNrOfTariffRegisters());
        assertEquals(32, new ABBA1700MeterType(new MeterType(IDENT_NEW_EXTENDED)).getNrOfTariffRegisters());
    }

    @Test
    public void testGetExtraOffsetHistoricDisplayScaling() throws Exception {
        assertEquals(-1, new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_UNASSIGNED).getExtraOffsetHistoricDisplayScaling());
        assertEquals(0, new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_16_TOU).getExtraOffsetHistoricDisplayScaling());
        assertEquals(124, new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_32_TOU).getExtraOffsetHistoricDisplayScaling());
        assertEquals(124, new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_32_TOU_5_CDR).getExtraOffsetHistoricDisplayScaling());
        assertEquals(0, new ABBA1700MeterType(new MeterType(IDENT_OLD)).getExtraOffsetHistoricDisplayScaling());
        assertEquals(124, new ABBA1700MeterType(new MeterType(IDENT_NEW)).getExtraOffsetHistoricDisplayScaling());
        assertEquals(124, new ABBA1700MeterType(new MeterType(IDENT_NEW_EXTENDED)).getExtraOffsetHistoricDisplayScaling());
    }

    @Test
    public void testHasExtendedCustomerRegisters() throws Exception {
        assertFalse(new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_UNASSIGNED).hasExtendedCustomerRegisters());
        assertFalse(new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_16_TOU).hasExtendedCustomerRegisters());
        assertFalse(new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_32_TOU).hasExtendedCustomerRegisters());
        assertTrue(new ABBA1700MeterType(ABBA1700MeterType.METERTYPE_32_TOU_5_CDR).hasExtendedCustomerRegisters());
        assertFalse(new ABBA1700MeterType(new MeterType(IDENT_OLD)).hasExtendedCustomerRegisters());
        assertFalse(new ABBA1700MeterType(new MeterType(IDENT_NEW)).hasExtendedCustomerRegisters());
        assertTrue(new ABBA1700MeterType(new MeterType(IDENT_NEW_EXTENDED)).hasExtendedCustomerRegisters());
    }
}
