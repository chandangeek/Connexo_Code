package com.energyict.dlms.cosem.attributes;

import com.energyict.obis.ObisCode;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Copyrights EnergyICT
 * Date: 31/01/12
 * Time: 14:15
 */
public class DLMSClassAttributesTest {

    private static final Enum[][] DLMS_CLASS_ATTRIBUTES = new Enum[][]{
            ActivityCalendarAttributes.values(),
            ActivityCalendarAttributes.values(),
            AssociationLNAttributes.values(),
            ChangeOfSupplierManagementAttributes.values(),
            ChangeOfTenantManagementAttributes.values(),
            DataAttributes.values(),
            DemandRegisterAttributes.values(),
            DisconnectControlAttribute.values(),
            ExtendedRegisterAttributes.values(),
            Ipv4SetupAttributes.values(),
            LimiterAttributes.values(),
            MbusClientAttributes.values(),
            PrivacyEnhancingDataAggregationAttributes.values(),
            RegisterAttributes.values(),
            SFSKActiveInitiatorAttribute.values(),
            SFSKIec61334LLCSetupAttribute.values(),
            SFSKMacCountersAttribute.values(),
            SFSKPhyMacSetupAttribute.values(),
            SFSKSyncTimeoutsAttribute.values(),
            SpecialDaysTableAttributes.values(),
            ActivePassiveAttributes.values(),
            ZigBeeSASJoinAttribute.values(),
            ZigBeeSASStartupAttribute.values(),
            ZigBeeSasApsFragmentationAttributes.values(),
            ZigbeeHanManagementAttributes.values(),
            ZigbeeSETCControlAttributes.values(),
    };

    private static final ObisCode OBIS_CODE = ObisCode.fromString("1.0.0.8.0.255");

    @Test
    public void testDlmsClassMethod() throws Exception {
        for (Enum[] dlmsClassAttributes : DLMS_CLASS_ATTRIBUTES) {
            validateUniqueAttributes(dlmsClassAttributes);
        }
    }

    private void validateUniqueAttributes(Enum[] dlmsClassAttributes) {
        for (Enum dlmsClassAttribute : dlmsClassAttributes) {

            assertTrue(
                    "[" + dlmsClassAttribute.getDeclaringClass().getName() + "] should implement [" + DLMSClassAttributes.class.getName() + "]",
                    dlmsClassAttribute instanceof DLMSClassAttributes
            );

            DLMSClassAttributes method = (DLMSClassAttributes) dlmsClassAttribute;
            for (Enum classMethod : dlmsClassAttributes) {
                DLMSClassAttributes mt = (DLMSClassAttributes) classMethod;
                String message = "[" + mt.getClass().getName() + "." + mt + "] and [" + mt.getClass().getName() + "." + method + "] have $error$. Probably a bug?";
                if (mt != method) {
                    assertTrue(message.replace("$error$", "same attributeNumber"), mt.getAttributeNumber() != method.getAttributeNumber());
                    assertTrue(message.replace("$error$", "same SN address"), mt.getShortName() != method.getShortName());
                    assertTrue(message.replace("$error$", "different DLMSAttribute"), mt.getDLMSAttribute(OBIS_CODE) != method.getDLMSAttribute(OBIS_CODE));
                    assertEquals(message.replace("$error$", "different DlmsClassId"), mt.getDlmsClassId(), method.getDlmsClassId());
                } else {
                    assertEquals(message.replace("$error$", "different methodNumber"), mt.getAttributeNumber(), method.getAttributeNumber());
                    assertEquals(message.replace("$error$", "different SN address"), mt.getShortName(), method.getShortName());
                    assertEquals(message.replace("$error$", "different DlmsClassId"), mt.getDlmsClassId(), method.getDlmsClassId());
                    assertEquals(message.replace("$error$", "different DLMSAttribute"), mt.getDLMSAttribute(OBIS_CODE), method.getDLMSAttribute(OBIS_CODE));
                }
            }

        }
    }


}
