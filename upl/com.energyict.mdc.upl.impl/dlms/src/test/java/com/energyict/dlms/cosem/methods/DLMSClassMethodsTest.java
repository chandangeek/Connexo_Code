package com.energyict.dlms.cosem.methods;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Copyrights EnergyICT
 * Date: 31/01/12
 * Time: 13:17
 */
public class DLMSClassMethodsTest {

    private static final Enum[][] DLMS_CLASS_METHODS = new Enum[][]{
            ActivityCalendarMethods.values(),
            ActivityCalendarMethods.values(),
            AssociationSNMethods.values(),
            ChangeOfSupplierManagementMethods.values(),
            ChangeOfTenantManagementMethods.values(),
            ChangeOfTenantManagementMethods.values(),
            MbusClientMethods.values(),
            PrivacyEnhancingDataAggregationMethods.values(),
            SpecialDaysTableMethods.values(),
            SupplierIdMethods.values(),
            SupplierNameMethods.values(),
            ZigbeeSETCControlMethods.values(),
            ZigbeeHanManagementMethods.values()
    };

    @Test
    public void testDlmsClassMethod() throws Exception {
        for (Enum[] dlmsClassMethods : DLMS_CLASS_METHODS) {
            validateUniqueMethodId(dlmsClassMethods);
        }
    }

    private void validateUniqueMethodId(Enum[] dlmsClassMethods) {
        for (Enum dlmsClassMethod : dlmsClassMethods) {

            assertTrue(
                    "[" + dlmsClassMethod.getDeclaringClass().getName() + "] should implement [" + DLMSClassMethods.class.getName() + "]",
                    dlmsClassMethod instanceof DLMSClassMethods
            );

            DLMSClassMethods method = (DLMSClassMethods) dlmsClassMethod;
            for (Enum classMethod : dlmsClassMethods) {
                DLMSClassMethods mt = (DLMSClassMethods) classMethod;
                String message = "[" + mt.getClass().getName() + "." + mt + "] and [" + mt.getClass().getName() + "." + method + "] have $error$. Probably a bug?";
                if (mt != method) {
                    assertTrue(message.replace("$error$", "same methodNumber"), mt.getMethodNumber() != method.getMethodNumber());
                    assertTrue(message.replace("$error$", "same SN address"), mt.getShortName() != method.getShortName());
                    assertEquals(message.replace("$error$", "different DlmsClassId"), mt.getDlmsClassId(), method.getDlmsClassId());
                } else {
                    assertEquals(message.replace("$error$", "different methodNumber"), mt.getMethodNumber(), method.getMethodNumber());
                    assertEquals(message.replace("$error$", "different SN address"), mt.getShortName(), method.getShortName());
                    assertEquals(message.replace("$error$", "different DlmsClassId"), mt.getDlmsClassId(), method.getDlmsClassId());
                }
            }

        }
    }


}
