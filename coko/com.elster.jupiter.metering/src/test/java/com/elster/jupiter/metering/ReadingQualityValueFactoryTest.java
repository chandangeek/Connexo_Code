package com.elster.jupiter.metering;

import com.elster.jupiter.properties.ReadingQualityPropertyValue;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 15/06/2016 - 11:50
 */
public class ReadingQualityValueFactoryTest {

    @Test
    public void testValidation() {
        ReadingQualityValueFactory readingQualityValueFactory = new ReadingQualityValueFactory();
        assertTrue(!readingQualityValueFactory.isValid(new ReadingQualityPropertyValue("1.1")));
        assertTrue(!readingQualityValueFactory.isValid(new ReadingQualityPropertyValue("1")));
        assertTrue(!readingQualityValueFactory.isValid(new ReadingQualityPropertyValue("a.b.c")));
        assertTrue(!readingQualityValueFactory.isValid(new ReadingQualityPropertyValue("1.1.f")));
        assertTrue(!readingQualityValueFactory.isValid(new ReadingQualityPropertyValue("abcdefgh")));
        assertTrue(!readingQualityValueFactory.isValid(new ReadingQualityPropertyValue("8.1.1")));
        assertTrue(!readingQualityValueFactory.isValid(new ReadingQualityPropertyValue("1.200.1")));
        assertTrue(readingQualityValueFactory.isValid(new ReadingQualityPropertyValue("1.1.12345")));
        assertTrue(readingQualityValueFactory.isValid(new ReadingQualityPropertyValue("1.1.1")));
    }
}