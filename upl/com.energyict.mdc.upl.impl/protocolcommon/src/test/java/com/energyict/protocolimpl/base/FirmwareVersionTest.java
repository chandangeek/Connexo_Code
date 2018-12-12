package com.energyict.protocolimpl.base;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Copyrights EnergyICT
 * Date: 18/05/11
 * Time: 14:21
 */
public class FirmwareVersionTest {

    @Test
    public void testBefore() throws Exception {
        FirmwareVersion oldVersion = new FirmwareVersion("1.2.3");
        FirmwareVersion middleVersion = new FirmwareVersion("1.2.3.1");
        FirmwareVersion newVersion = new FirmwareVersion("1.2.4");

        assertFalse(oldVersion.before(oldVersion));
        assertFalse(middleVersion.before(middleVersion));
        assertFalse(newVersion.before(newVersion));

        assertTrue(oldVersion.before(middleVersion));
        assertTrue(oldVersion.before(newVersion));
        assertTrue(middleVersion.before(newVersion));

        assertFalse(newVersion.before(oldVersion));
        assertFalse(middleVersion.before(oldVersion));
        assertFalse(newVersion.before(middleVersion));

    }

    @Test
    public void testBeforeOrEqual() throws Exception {
        FirmwareVersion oldVersion = new FirmwareVersion("1.2.3");
        FirmwareVersion middleVersion = new FirmwareVersion("1.2.3.1");
        FirmwareVersion newVersion = new FirmwareVersion("1.2.4");

        assertTrue(oldVersion.beforeOrEqual(oldVersion));
        assertTrue(middleVersion.beforeOrEqual(middleVersion));
        assertTrue(newVersion.beforeOrEqual(newVersion));

        assertTrue(oldVersion.beforeOrEqual(middleVersion));
        assertTrue(oldVersion.beforeOrEqual(newVersion));
        assertTrue(middleVersion.beforeOrEqual(newVersion));

        assertFalse(newVersion.beforeOrEqual(oldVersion));
        assertFalse(middleVersion.beforeOrEqual(oldVersion));
        assertFalse(newVersion.beforeOrEqual(middleVersion));

    }

    @Test
    public void testAfter() throws Exception {
        FirmwareVersion oldVersion = new FirmwareVersion("1.2.3");
        FirmwareVersion middleVersion = new FirmwareVersion("1.2.3.1");
        FirmwareVersion newVersion = new FirmwareVersion("1.2.4");

        assertFalse(oldVersion.after(oldVersion));
        assertFalse(middleVersion.after(middleVersion));
        assertFalse(newVersion.after(newVersion));

        assertFalse(oldVersion.after(middleVersion));
        assertFalse(oldVersion.after(newVersion));
        assertFalse(middleVersion.after(newVersion));

        assertTrue(newVersion.after(oldVersion));
        assertTrue(middleVersion.after(oldVersion));
        assertTrue(newVersion.after(middleVersion));
    }

    @Test
    public void testAfterOrEqual() throws Exception {
        FirmwareVersion oldVersion = new FirmwareVersion("1.2.3");
        FirmwareVersion middleVersion = new FirmwareVersion("1.2.3.1");
        FirmwareVersion newVersion = new FirmwareVersion("1.2.4");

        assertTrue(oldVersion.afterOrEqual(oldVersion));
        assertTrue(middleVersion.afterOrEqual(middleVersion));
        assertTrue(newVersion.afterOrEqual(newVersion));

        assertFalse(oldVersion.afterOrEqual(middleVersion));
        assertFalse(oldVersion.afterOrEqual(newVersion));
        assertFalse(middleVersion.afterOrEqual(newVersion));

        assertTrue(newVersion.afterOrEqual(oldVersion));
        assertTrue(middleVersion.afterOrEqual(oldVersion));
        assertTrue(newVersion.afterOrEqual(middleVersion));
    }

    @Test
    public void testEqual() throws Exception {
        FirmwareVersion oldVersion = new FirmwareVersion("1.2.3");
        FirmwareVersion middleVersion = new FirmwareVersion("1.2.3.1");
        FirmwareVersion newVersion = new FirmwareVersion("1.2.4");

        assertTrue(oldVersion.equal(oldVersion));
        assertTrue(middleVersion.equal(middleVersion));
        assertTrue(newVersion.equal(newVersion));

        assertFalse(oldVersion.equal(middleVersion));
        assertFalse(oldVersion.equal(newVersion));
        assertFalse(middleVersion.equal(newVersion));
        assertFalse(newVersion.equal(oldVersion));
        assertFalse(middleVersion.equal(oldVersion));
        assertFalse(newVersion.equal(middleVersion));

    }

}
