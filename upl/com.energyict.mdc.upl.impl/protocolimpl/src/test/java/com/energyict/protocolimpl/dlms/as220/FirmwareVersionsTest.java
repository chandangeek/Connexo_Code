package com.energyict.protocolimpl.dlms.as220;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocolimpl.utils.DummyDLMSConnection;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Copyrights EnergyICT
 * Date: 24-nov-2010
 * Time: 12:50:03
 */
public class FirmwareVersionsTest {

    public static final String version23 = "0101020509060000000000011601110211030600000000";
    public static final String version12 = "0101020509060000000000011601110111020600000000";

    @Test
    public void isHigherOrEqualsThenTest() throws Exception {
        FirmwareVersions fv = new FirmwareVersions();
        FirmwareVersionAttribute fva = new FirmwareVersionAttribute(DLMSUtils.hexStringToByteArray(version23));
        fv.setFirmwareVersionAttribute(fva);
        assertTrue(fv.isHigherOrEqualsThen("2.3"));

        fva = new FirmwareVersionAttribute(DLMSUtils.hexStringToByteArray(version12));
        fv.setFirmwareVersionAttribute(fva);
        assertFalse(fv.isHigherOrEqualsThen("2.3"));

    }
}
