package com.energyict.protocolimpl.emon.ez7.core.command;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * @author sva
 * @since 12/08/13 - 11:57
 */
public class RGLInfoTest {

    @Test
    public void extractNumericalIdentificationNumberTest() {
        RGLInfo rglInfo = new RGLInfo(null);
        List values = Arrays.asList("", "", "0000", "0000", "0000", "2300", "1234", "5678");
        String identification = rglInfo.extractIdentificationNumber(values);

        assertEquals("The identification number doesn't match the expected one", "230012345678", identification);
    }

    @Test
    public void extractAlphaNumericalIdentificationNumberTest() {
        RGLInfo rglInfo = new RGLInfo(null);
        List values = Arrays.asList("", "", "4241", "3030", "3031", "3233", "3334", "3536");
        String identification = rglInfo.extractIdentificationNumber(values);

        assertEquals("The identification number doesn't match the expected one", "BA0001233456", identification);

    }
}