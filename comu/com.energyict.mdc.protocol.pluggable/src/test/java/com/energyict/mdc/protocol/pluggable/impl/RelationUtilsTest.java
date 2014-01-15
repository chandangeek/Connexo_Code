package com.energyict.mdc.protocol.pluggable.impl;

import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Tests the {@link RelationUtils} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/10/12
 * Time: 11:02
 */
public class RelationUtilsTest {

    @Test
    public void createConformRelationTypeNameReturnOriginalTest() {
        final String original = "MyOriginalString";
        assertEquals("SysMyOrigin1953773102", RelationUtils.createConformRelationTypeName(original));
    }

    @Test
    public void createConformRelationTypeNameReturnConverted() {
        final String original = "AWayLongerOriginalStringThenAllowed";
        assertEquals("SysAWayLong1529668302", RelationUtils.createConformRelationTypeName(original));
    }

    @Test
    public void createOriginalAndConformRelationTypeNameShortLengthTest() {
        final int maxLength = 5;
        final String original = "AWayLongerOriginalStringThenAllowed";
        assertEquals("15296", RelationUtils.createOriginalAndConformRelationNameBasedOnJavaClassname(original, maxLength));
    }

    @Test
    public void createConformRelationAttributeNameReturnOriginalTest() {
        final String original = "MyOriginalString";
        assertEquals(original, RelationUtils.createConformRelationAttributeName(original));
    }

    @Test
    public void createConformRelationAttributeNameReturnConverted() {
        final String original = "AWayLongerOriginalStringThenAllowed";
        assertEquals("AWayLongerOriginalSt1529668302", RelationUtils.createConformRelationAttributeName(original));
    }

    @Test
    public void createOriginalAndConformRelationAttributeNameShortLengthTest() {
        final int maxLength = 5;
        final String original = "AWayLongerOriginalStringThenAllowed";
        assertEquals("15296", RelationUtils.createOriginalAndConformRelationNameBasedOnJavaClassname(original, maxLength));
    }

    @Test
    public void createOriginalAndConformRelationNameWithJavaClassNameShortLengthTest() {
        final int maxLength = 5;
        final String original = "com.energyict.AWayLongerOriginalStringThenAllowed";
        assertEquals("10489", RelationUtils.createOriginalAndConformRelationNameBasedOnJavaClassname(original, maxLength));
    }

    @Test
    public void createOriginalAndConformRelationNameWithHashNotExceedingMaxTest() {
        final int maxLength = 100;
        final String original = "A_NormalString";
        assertEquals("A_NormalString289509578", RelationUtils.createOriginalAndConformRelationNameBasedOnJavaClassname(original, maxLength));
    }

    @Test
    public void createOriginalAndConformRelationNameWithJavaClassNameWithHashNotExceedingMaxTest() {
        final int maxLength = 100;
        final String original = "com.energyict.blablabla.A_NormalString";
        assertEquals("A_NormalString1149065162", RelationUtils.createOriginalAndConformRelationNameBasedOnJavaClassname(original, maxLength));
    }

    @Test
    public void createOriginalAndConformRelationNameWithHashExceedingMaxTest() {
        final int maxLength = 20;
        final String original = "A_NormalString";
        assertEquals("A_NormalStr289509578", RelationUtils.createOriginalAndConformRelationNameBasedOnJavaClassname(original, maxLength));
    }

    @Test
    public void createOriginalAndConformRelationNameWithJavaClassNameWithHashExceedingMaxTest() {
        final int maxLength = 20;
        final String original = "com.energyict.test.A_NormalString";
        assertEquals("A_NormalSt1300892553", RelationUtils.createOriginalAndConformRelationNameBasedOnJavaClassname(original, maxLength));
    }

    @Test
    public void testWithDifferentSuffix(){
        final int maxLength = 24;
        final String firstMbusDevice = "com.energyict.smartmeterprotocolimpl.eict.webrtuz3.MbusDevice";
        final String secondMbusDevice = "com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice";
        assertThat(
                RelationUtils.createOriginalAndConformRelationNameBasedOnJavaClassname(firstMbusDevice, maxLength)
                        .equals(
                                RelationUtils.createOriginalAndConformRelationNameBasedOnJavaClassname(secondMbusDevice, maxLength)))
                .isFalse();
    }

}