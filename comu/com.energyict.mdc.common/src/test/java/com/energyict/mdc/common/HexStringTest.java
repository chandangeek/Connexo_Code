package com.energyict.mdc.common;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.MultiBundleTranslator;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * User: gde
 * Date: 17/04/13
 */
@RunWith(MockitoJUnitRunner.class)
public class HexStringTest {

    @Mock
    private Environment environment;

    @Before
    public void setEnvironment () {
        Environment.DEFAULT.set(this.environment);
        when(this.environment.getErrorMsg("invalidHexString")).thenReturn(MultiBundleTranslator.MISSING_RESOURCE_PREFIX + "invalidHexString");
    }

    @After
    public void cleanEnvironment () {
        Environment.DEFAULT.set(null);
    }

    @Test
    public void testConstructorValid() {
        HexString hexString = new HexString("0A7B");
        assertNotNull(hexString);
    }

    @Test
    public void testConstructorValid2() {
        HexString hexString = new HexString("+0A7B");
        assertNotNull(hexString);
    }

    @Test
    public void testConstructorValid3() {
        HexString hexString = new HexString("-0A7B");
        assertNotNull(hexString);
    }

    @Test
    public void testConstructorEmpty() {
        HexString hexString = new HexString();
        assertNotNull(hexString);
    }

    @Test
    public void testToString() {
        HexString hexString = new HexString("0A7B");
        assertEquals("0A7B", hexString.toString());
    }

    @Test
    public void testToStringLowerCaseInputConvertedToUpper() {
        HexString hexString = new HexString("0a7b");
        assertEquals("0A7B", hexString.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnlyHexCharactersAllowed() {
        new HexString("0A7BG8");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnlyHexCharactersAllowed2() {
        new HexString("0A7B+2");
    }

    @Test
    public void testValueEquality() {
        HexString hexString1 = new HexString("0A7B");
        HexString hexString2 = new HexString("0a7B");
        HexString hexString3 = new HexString("0A7B");
        HexString hexString4 = new HexString("0AC79D");
        assertFalse("equals(null) should always return false", hexString1.equals(null));
        assertTrue("equals(this) should always return true", hexString1.equals(hexString1));
        assertTrue("equals() with same value object should return true", hexString1.equals(hexString3));
        assertTrue("equals() with same value object should return true", hexString1.equals(hexString2));
        assertTrue("equals() with same value object should return true", hexString2.equals(hexString1));
        assertFalse("equals() with different value object should return false", hexString1.equals(hexString4));
        assertFalse("equals() with different value object should return false", hexString4.equals(hexString1));
        assertFalse("equals() with different value object should return false", hexString4.equals(new HexString()));
        assertFalse("equals() with different value object should return false", new HexString().equals(hexString4));
        assertTrue("equals() with both null should return true", new HexString().equals(new HexString()));
    }

    @Test
    public void testValueHashCode() {
        HexString hexString1 = new HexString("0A7B");
        HexString hexString2 = new HexString("0a7B");
        HexString hexString3 = new HexString("0A7B");
        HexString hexString4 = new HexString("0AC79D");
        assertTrue("hashCode on same Object should remain the same", hexString1.hashCode() == hexString1.hashCode());
        assertTrue("equal Objects must have equal hashCodes", hexString1.hashCode() == hexString3.hashCode());
        assertTrue("equal Objects must have equal hashCodes", hexString1.hashCode() == hexString2.hashCode());
        assertFalse("non equal Objects should have different hashCodes", hexString1.hashCode() == hexString4.hashCode());
        assertFalse("non equal Objects should have different hashCodes", hexString1.hashCode() == new HexString().hashCode());
        assertTrue("equal Objects must have equal hashCodes", new HexString().hashCode() == new HexString().hashCode());
    }

    @Test
    public void testNullable() {
        HexString hexString = new HexString("");
        assert(hexString.isNull());
        hexString = new HexString(null);
        assert(hexString.isNull());
        hexString = new HexString("0A7B");
        assert(!hexString.isNull());
    }

}
