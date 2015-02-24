package com.energyict.protocolimpl.modbus.generic;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.generic.common.DataTypeSelector;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author sva
 * @since 21/11/13 - 9:53
 */
public class ParserFactoryTest {

    private static int LITTLE_ENDIAN_OFFSET = 0x80;

    private ParserFactory factory;

    @Before
    public void initializeParserFactory() {
        this.factory = new ParserFactory();
    }

    @Test
    public void testByteParser() throws Exception {
        int[] values = new int[]{0x000A};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.BYTE.getDataTypeCode()).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("10");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testRegisterParser() throws Exception {
        int[] values = new int[]{0x8020};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.REGISTER.getDataTypeCode()).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("32800");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testSignedRegisterParser() throws Exception {
        int[] values = new int[]{0x8020};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.SIGNED_REGISTER.getDataTypeCode()).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("-32736");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testIntegerParser() throws Exception {
        int[] values = new int[]{0x8020, 0x9040};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.INTEGER.getDataTypeCode()).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("2149617728");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testSignedIntegerParser() throws Exception {
        int[] values = new int[]{0x8020, 0x9040};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.SIGNED_INTEGER.getDataTypeCode()).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("-2145349568");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testLongParser() throws Exception {
        int[] values = new int[]{0x8020, 0x9040, 0x8121, 0x9141};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.LONG.getDataTypeCode()).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("9232537842828284225");

        Assert.assertEquals(expected, val);
    }

    @Test
    public void testSignedLongParser() throws Exception {
        int[] values = new int[]{0x8020, 0x9040, 0x8121, 0x9141};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.SIGNED_LONG.getDataTypeCode()).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("-9214206230881267391");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void test32BItsFloatParser() throws Exception {
        int[] values = new int[]{0x3f8f, 0xcd68};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.FLOAT_32BIT.getDataTypeCode()).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("1.123456");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void test64BitsFloatParser() throws Exception {
        int[] values = new int[]{0x3ff1, 0xf9ac, 0xffa7, 0xeb6c};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.FLOAT_64BIT.getDataTypeCode()).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("1.123456");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void test32BitsBCDParser() throws Exception {
        int[] values = new int[]{0x0177, 0x0858};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.BCD_32BIT.getDataTypeCode()).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("1770858");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void test64BitsBCDParser() throws Exception {
        int[] values = new int[]{0x123, 0x4567, 0x0177, 0x0858};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.BCD_32BIT.getDataTypeCode()).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("123456701770858");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testASCIIParser() throws Exception {
        int[] values = new int[]{0x4153, 0x4349, 0x4920};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.ASCII.getDataTypeCode() + values.length).val(values, getDummyRegister());

        // Asserts
        String expected = "ASCII";
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testLittleEndianByteParser() throws Exception {
        int[] values = new int[]{0x000A};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.BYTE.getDataTypeCode() + LITTLE_ENDIAN_OFFSET).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("10");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testLittleEndianRegisterParser() throws Exception {
        int[] values = new int[]{0x8020};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.REGISTER.getDataTypeCode() + LITTLE_ENDIAN_OFFSET).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("32800");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testLittleEndianSignedRegisterParser() throws Exception {
        int[] values = new int[]{0x8020};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.SIGNED_REGISTER.getDataTypeCode() + LITTLE_ENDIAN_OFFSET).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("-32736");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testLittleEndianIntegerParser() throws Exception {
        int[] values = new int[]{0x9040, 0x8020};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.INTEGER.getDataTypeCode() + LITTLE_ENDIAN_OFFSET).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("2149617728");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testLittleEndianSignedIntegerParser() throws Exception {
        int[] values = new int[]{0x9040, 0x8020};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.SIGNED_INTEGER.getDataTypeCode() + LITTLE_ENDIAN_OFFSET).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("-2145349568");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testLittleEndianLongParser() throws Exception {
        int[] values = new int[]{0x9141, 0x8121, 0x9040, 0x8020};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.LONG.getDataTypeCode() + LITTLE_ENDIAN_OFFSET).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("9232537842828284225");

        Assert.assertEquals(expected, val);
    }

    @Test
    public void testLittleEndianSignedLongParser() throws Exception {
        int[] values = new int[]{0x9141, 0x8121, 0x9040, 0x8020};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.SIGNED_LONG.getDataTypeCode() + LITTLE_ENDIAN_OFFSET).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("-9214206230881267391");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testLittleEndian32BItsFloatParser() throws Exception {
        int[] values = new int[]{0xcd68, 0x3f8f};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.FLOAT_32BIT.getDataTypeCode() + LITTLE_ENDIAN_OFFSET).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("1.123456");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testLittleEndian64BitsFloatParser() throws Exception {
        int[] values = new int[]{0xeb6c, 0xffa7, 0xf9ac, 0x3ff1};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.FLOAT_64BIT.getDataTypeCode() + LITTLE_ENDIAN_OFFSET).val(values, getDummyRegister());

        // Asserts
        BigDecimal expected = new BigDecimal("1.123456");
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testLittleEndianASCIIParser() throws Exception {
        int[] values = new int[]{0x4920, 0x4349, 0x4153};

        // Business method
        Object val = getParser(DataTypeSelector.DataType.ASCII.getDataTypeCode() + LITTLE_ENDIAN_OFFSET + values.length).val(values, getDummyRegister());

        // Asserts
        String expected = "ASCII";
        Assert.assertEquals(expected, val);
    }

    private Parser getParser(int dataTypeCode) throws IOException {
        return getFactory().get(Integer.toString(dataTypeCode));
    }

    private AbstractRegister getDummyRegister() {
        return new HoldingRegister(-1, -1);
    }

    public ParserFactory getFactory() {
        return factory;
    }
}
