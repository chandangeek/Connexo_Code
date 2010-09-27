package com.energyict.protocolimpl.utils;

import antlr.Utils;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.RegisterValue;
import org.junit.*;

import java.io.File;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author jme
 */
public class ProtocolToolsTest {

    private static final int SECONDS_PER_MINUTE = 60;
    private static final String NON_EXISTING_FILE_NAME = "nonexistingfilename";
    private static final String FILENAME_TO_READ = "/com/energyict/protocolimpl/utils/ProtocolToolsReadFileTest.txt";
    private static final String FILENAME_TO_WRITE = System.getProperty("java.io.tmpdir") + "/ProtocolToolsReadFileTest.tmp";
    private static final String VALUE_TO_READ_FROM_FILE = "9876543210123456789";

    private static final byte[] LONG_NAME = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06};

    private static final byte[] BYTE_ARRAY = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
    private static final String BYTE_ARRAY_AS_STRING = "000102030405";
    private static final String BYTE_ARRAY_AS_STRING_$ = "$00$01$02$03$04$05";
    private static final String BYTE_ARRAY_AS_STRING_0x = " 0x00 0x01 0x02 0x03 0x04 0x05";
    private static final String CUSTOM_PREFIX = " 0x";
    private static final String DEFAULT_PREFIX = "$";
    private static final String EMPTY_PREFIX = "";

    private static final int PADDING_TEST_LENGTH = 20;
    private static final char PADDING_TEST_CHARACTER = '-';
    private static final String PADDING_TEST_STRING = "123";

    private static final byte[] MERGE_ARRAY1 = "ABC012DEF345".getBytes();
    private static final byte[] MERGE_ARRAY2 = "GHI678JKL012".getBytes();
    private static final byte[] MERGED_ARRAY = "ABC012DEF345GHI678JKL012".getBytes();

    @BeforeClass
    @AfterClass
    public static void cleanUpData() {
        File fileToWrite = new File(FILENAME_TO_WRITE);
        if (fileToWrite.exists()) {
            fileToWrite.delete();
        }
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#addPadding(java.lang.String, char, int, boolean)}.
     */
    @Test
    public final void testAddPadding() {
        assertNotNull(ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, 0, false));
        assertNotNull(ProtocolTools.addPadding("", PADDING_TEST_CHARACTER, 0, false));
        assertEquals(null, ProtocolTools.addPadding(null, PADDING_TEST_CHARACTER, 0, false));
        assertEquals(null, ProtocolTools.addPadding(null, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, false));
        assertEquals(PADDING_TEST_STRING.length(), ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, 0, false).length());
        assertEquals(PADDING_TEST_STRING.length(), ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_STRING.length(), false).length());
        assertEquals(PADDING_TEST_LENGTH, ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, false).length());
        assertEquals(PADDING_TEST_STRING.length(), ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, 0, true).length());
        assertEquals(PADDING_TEST_STRING.length(), ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_STRING.length(), true).length());
        assertEquals(PADDING_TEST_LENGTH, ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, true).length());
        assertEquals(0, ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, true).indexOf(PADDING_TEST_STRING));
        assertEquals(PADDING_TEST_LENGTH - PADDING_TEST_STRING.length(), ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, false).indexOf(PADDING_TEST_STRING));
        assertEquals(PADDING_TEST_STRING.length(), ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, true).indexOf(PADDING_TEST_CHARACTER));
        assertEquals(0, ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, false).indexOf(PADDING_TEST_CHARACTER));
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getBytesFromHexString(java.lang.String)}.
     */
    @Test
    public final void testGetBytesFromHexStringString() {
        assertNotNull(ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_$));
        assertArrayEquals(BYTE_ARRAY, ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_$));
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getBytesFromHexString(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void testGetBytesFromHexStringStringString() {
        assertNotNull(ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_$, DEFAULT_PREFIX));
        assertNotNull(ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_0x, CUSTOM_PREFIX));
        assertNotNull(ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING, EMPTY_PREFIX));
        assertNotNull(ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING, null));
        assertArrayEquals(BYTE_ARRAY, ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_$, DEFAULT_PREFIX));
        assertArrayEquals(BYTE_ARRAY, ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_0x, CUSTOM_PREFIX));
        assertArrayEquals(BYTE_ARRAY, ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING, EMPTY_PREFIX));
        assertArrayEquals(BYTE_ARRAY, ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING, null));
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getDataBetweenBrackets(byte[])}.
     */
    @Test
    public final void testGetDataBetweenBracketsByteArray() {
        assertArrayEquals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("123(123456)321".getBytes()));
        assertArrayEquals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("(123456)321".getBytes()));
        assertArrayEquals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("(123456)".getBytes()));
        assertArrayEquals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets(")(123456)".getBytes()));
        assertArrayEquals("(123456".getBytes(), ProtocolTools.getDataBetweenBrackets("((123456)".getBytes()));
        assertArrayEquals("".getBytes(), ProtocolTools.getDataBetweenBrackets("()123456)".getBytes()));
        assertArrayEquals("".getBytes(), ProtocolTools.getDataBetweenBrackets(")123456)".getBytes()));
        assertArrayEquals("".getBytes(), ProtocolTools.getDataBetweenBrackets(")(123456".getBytes()));
        assertArrayEquals("".getBytes(), ProtocolTools.getDataBetweenBrackets(")(123456".getBytes()));
        assertArrayEquals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("(123456)(123)".getBytes()));
        assertArrayEquals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("123)(123456)".getBytes()));
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getDataBetweenBrackets(java.lang.String)}.
     */
    @Test
    public final void testGetDataBetweenBracketsString() {
        assertEquals("123456", ProtocolTools.getDataBetweenBrackets("\r\n#123(123456)321"));
        assertEquals("123456", ProtocolTools.getDataBetweenBrackets("(123456)321"));
        assertEquals("123456", ProtocolTools.getDataBetweenBrackets("(123456)"));
        assertEquals("123456", ProtocolTools.getDataBetweenBrackets(")(123456)"));
        assertEquals("(123456", ProtocolTools.getDataBetweenBrackets("((123456)"));
        assertEquals("", ProtocolTools.getDataBetweenBrackets("()123456)"));
        assertEquals("", ProtocolTools.getDataBetweenBrackets(")123456)"));
        assertEquals("", ProtocolTools.getDataBetweenBrackets(")(123456"));
        assertEquals("", ProtocolTools.getDataBetweenBrackets(")(123456"));
        assertEquals("123456", ProtocolTools.getDataBetweenBrackets("(123456)(123)"));
        assertEquals("123456", ProtocolTools.getDataBetweenBrackets("123)(123456)"));
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getHexStringFromBytes(byte[])}.
     */
    @Test
    public final void testGetHexStringFromBytes() {
        assertNotNull(ProtocolTools.getHexStringFromBytes(BYTE_ARRAY));
        assertEquals(BYTE_ARRAY_AS_STRING_$, ProtocolTools.getHexStringFromBytes(BYTE_ARRAY));
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#concatByteArrays(byte[], byte[])}.
     */
    @Test
    public final void testConcatByteArray() {
        assertArrayEquals(MERGE_ARRAY1, ProtocolTools.concatByteArrays(MERGE_ARRAY1, new byte[0]));
        assertArrayEquals(MERGE_ARRAY2, ProtocolTools.concatByteArrays(new byte[0], MERGE_ARRAY2));
        assertArrayEquals(MERGE_ARRAY1, ProtocolTools.concatByteArrays(MERGE_ARRAY1, null));
        assertArrayEquals(MERGE_ARRAY2, ProtocolTools.concatByteArrays(null, MERGE_ARRAY2));
        assertArrayEquals(new byte[0], ProtocolTools.concatByteArrays(new byte[0], null));
        assertArrayEquals(new byte[0], ProtocolTools.concatByteArrays(null, new byte[0]));
        assertArrayEquals(new byte[0], ProtocolTools.concatByteArrays(null, null));
        assertArrayEquals(MERGED_ARRAY, ProtocolTools.concatByteArrays(MERGE_ARRAY1, MERGE_ARRAY2));
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getEpochTimeFromString(java.lang.String)}.
     *
     * @throws Exception
     */
    @Test
    public void testGetEpochTimeFromStringString() throws Exception {
        String epochTime1[] = {
                "1285592545",
                "27/09/2010 13:02:25",
                "27\\09\\2010 13:02:25",
                "27-09-2010 13:02:25",
                "2010/09/27 13:02:25",
                "2010\\09\\27 13:02:25",
                "2010-09-27 13:02:25"
        };

        String epochTime2[] = {
                "1285592520",
                "27/09/2010 13:02",
                "27\\09\\2010 13:02",
                "27-09-2010 13:02",
                "2010/09/27 13:02",
                "2010\\09\\27 13:02",
                "2010-09-27 13:02"
        };

        for (String epochTime : epochTime1) {
            assertEquals(epochTime1[0], ProtocolTools.getEpochTimeFromString(epochTime));
        }
        for (String epochTime : epochTime2) {
            assertEquals(epochTime2[0], ProtocolTools.getEpochTimeFromString(epochTime));
        }

        String internalParseError = "THIS_GENERATES_AN_INTERNAL_:PARSE_ERROR";
        assertNull(ProtocolTools.getEpochTimeFromString(null));
        assertEquals(internalParseError, ProtocolTools.getEpochTimeFromString(internalParseError));

    }

    @Test
    public void testGetAsciiFromBytesByteArray() throws Exception {
        String asciiString = "................................ !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        byte[] bytes = new byte[127];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        assertEquals("", ProtocolTools.getAsciiFromBytes(null));
        assertEquals("", ProtocolTools.getAsciiFromBytes(new byte[0]));
        assertEquals(asciiString, ProtocolTools.getAsciiFromBytes(bytes));
    }

    @Test(timeout = 750)
    public void testDelayLongTiming() throws Exception {
        int delay = 500;
        long start = System.currentTimeMillis();
        ProtocolTools.delay(delay);
        long diff = (System.currentTimeMillis() - start) + 500;
        assertTrue(diff >= delay);
    }

    @Test(timeout = 750)
    public void testDelayLongInterrupt() throws Exception {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    ProtocolTools.delay(5000);
                    fail("Expected ProtocolInterruptedException but nothing happened.");
                } catch (Exception e) {
                    assertTrue(e instanceof ProtocolInterruptedException);
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
        ProtocolTools.delay(100);
        thread.interrupt();
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getSubArray(byte[], int, int)}.
     */
    @Test
    public final void testGetSubArray() {
        assertArrayEquals(MERGE_ARRAY1, ProtocolTools.getSubArray(MERGED_ARRAY, 0, MERGE_ARRAY1.length));
        assertArrayEquals(MERGE_ARRAY2, ProtocolTools.getSubArray(MERGED_ARRAY, MERGE_ARRAY1.length, MERGED_ARRAY.length));
        assertArrayEquals(new byte[0], ProtocolTools.getSubArray(MERGED_ARRAY, 0, MERGED_ARRAY.length + 1));
        assertArrayEquals(new byte[0], ProtocolTools.getSubArray(MERGED_ARRAY, -1, MERGE_ARRAY1.length));
        assertArrayEquals(new byte[0], ProtocolTools.getSubArray(MERGED_ARRAY, MERGE_ARRAY1.length + 1, MERGE_ARRAY1.length));
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#indexOff(byte[], byte)}.
     */
    @Test
    public final void testIndexOffByteArrayByte() {
        assertEquals(-1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) ' '));
        assertEquals(-1, ProtocolTools.indexOff(null, (byte) ' '));
        assertEquals(0, ProtocolTools.indexOff(MERGED_ARRAY, (byte) 'A'));
        assertEquals(3, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '0'));
        assertEquals(14, ProtocolTools.indexOff(MERGED_ARRAY, (byte) 'I'));
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#indexOff(byte[], byte, int)}.
     */
    @Test
    public final void testIndexOffByteArrayByteInt() {
        assertEquals(-1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) ' ', 0));
        assertEquals(-1, ProtocolTools.indexOff(null, (byte) ' '), 0);
        assertEquals(-1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '2', MERGED_ARRAY.length));
        assertEquals(-1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '2', MERGED_ARRAY.length + 1));
        assertEquals(MERGED_ARRAY.length - 1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '2', MERGED_ARRAY.length - 1));
        assertEquals(0, ProtocolTools.indexOff(MERGED_ARRAY, (byte) 'A', 0));
        assertEquals(-1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) 'A', 1));
        assertEquals(3, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '0', 0));
        assertEquals(3, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '0', 3));
        assertEquals(21, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '0', 5));
        assertEquals(21, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '0', 21));
        assertEquals(-1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '0', 22));
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#isArrayIndexInRange(byte[], int)}.
     */
    @Test
    public final void testIsArrayIndexInRange() {
        assertFalse(ProtocolTools.isArrayIndexInRange(BYTE_ARRAY, -1));
        assertTrue(ProtocolTools.isArrayIndexInRange(BYTE_ARRAY, 0));
        assertTrue(ProtocolTools.isArrayIndexInRange(BYTE_ARRAY, 1));
        assertTrue(ProtocolTools.isArrayIndexInRange(BYTE_ARRAY, BYTE_ARRAY.length - 1));
        assertFalse(ProtocolTools.isArrayIndexInRange(BYTE_ARRAY, BYTE_ARRAY.length));
        assertFalse(ProtocolTools.isArrayIndexInRange(BYTE_ARRAY, BYTE_ARRAY.length + 1));

        assertFalse(ProtocolTools.isArrayIndexInRange(null, -1));
        assertFalse(ProtocolTools.isArrayIndexInRange(null, 0));
        assertFalse(ProtocolTools.isArrayIndexInRange(null, 1));
        assertFalse(ProtocolTools.isArrayIndexInRange(null, BYTE_ARRAY.length - 1));
        assertFalse(ProtocolTools.isArrayIndexInRange(null, BYTE_ARRAY.length));
        assertFalse(ProtocolTools.isArrayIndexInRange(null, BYTE_ARRAY.length + 1));
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#readBytesFromFile(java.lang.String)}.
     */
    @Test
    public final void testReadBytesFromFile() {
        URL fileUrl = Utils.class.getResource(FILENAME_TO_READ);
        assertNotNull("This test needs the following file: " + FILENAME_TO_READ + " but the fileUrl was null!", fileUrl);
        String fileName = fileUrl.getFile();
        byte[] fileContent = ProtocolTools.readBytesFromFile(fileName);
        assertNotNull(fileContent);
        assertTrue(fileContent.length > 0);
        assertEquals(new File(fileName).length(), fileContent.length);
        String fileContentAsString = new String(fileContent);
        assertEquals(VALUE_TO_READ_FROM_FILE, ProtocolTools.getDataBetweenBrackets(fileContentAsString));
        assertArrayEquals(new byte[0], ProtocolTools.readBytesFromFile(NON_EXISTING_FILE_NAME));
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#writeBytesToFile(java.lang.String, byte[], boolean)}.
     */
    @Test
    public final void testWriteBytesToFile() {
        assertFalse("This tes will write data to " + FILENAME_TO_WRITE + ", but file already exist!", new File(FILENAME_TO_WRITE).exists());
        ProtocolTools.writeBytesToFile(FILENAME_TO_WRITE, BYTE_ARRAY, true);
        assertTrue("Wrote data to " + FILENAME_TO_WRITE + ", but file doesn't exist!", new File(FILENAME_TO_WRITE).exists());
        assertArrayEquals(BYTE_ARRAY, ProtocolTools.readBytesFromFile(FILENAME_TO_WRITE));
        ProtocolTools.writeBytesToFile(FILENAME_TO_WRITE, BYTE_ARRAY, false);
        assertArrayEquals(BYTE_ARRAY, ProtocolTools.readBytesFromFile(FILENAME_TO_WRITE));
        ProtocolTools.writeBytesToFile(FILENAME_TO_WRITE, BYTE_ARRAY, true);
        assertArrayEquals(ProtocolTools.concatByteArrays(BYTE_ARRAY, BYTE_ARRAY), ProtocolTools.readBytesFromFile(FILENAME_TO_WRITE));
    }


    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#roundUpToNearestInterval(Date, int)}.
     */
    @Test
    public void testRoundUpToNearestInterval() {
        final int[] profileInterval = {1, 2, 5, 10, 15, 20, 30, 60};
        Calendar original;
        Calendar corrected = Calendar.getInstance();
        corrected.set(Calendar.MILLISECOND, 0);

        for (int i = 0; i < profileInterval.length; i++) {
            corrected.set(2010, 1, 1, 1, profileInterval[i], 0);
            original = (Calendar) corrected.clone();
            for (int j = 0; j < (profileInterval[i] * SECONDS_PER_MINUTE); j++) {
                assertEquals(corrected.getTime(), ProtocolTools.roundUpToNearestInterval(original.getTime(), profileInterval[i]));
                original.add(Calendar.SECOND, -1);
            }
        }

    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#roundDownToNearestInterval(Date, int)}.
     */
    @Test
    public void testRoundDownToNearestInterval() {
        final int[] profileInterval = {1, 2, 5, 10, 15, 20, 30, 60};
        Calendar original;
        Calendar corrected = Calendar.getInstance();
        corrected.set(Calendar.MILLISECOND, 0);

        for (int i = 0; i < profileInterval.length; i++) {
            corrected.set(2010, 1, 1, 1, 0, 0);
            original = (Calendar) corrected.clone();
            for (int j = 0; j < (profileInterval[i] * SECONDS_PER_MINUTE); j++) {
                assertEquals("Error whil rounding date: " + original.getTime(), corrected.getTime(), ProtocolTools.roundDownToNearestInterval(original.getTime(), profileInterval[i]));
                original.add(Calendar.SECOND, 1);
            }
        }

    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#mergeDuplicateIntervals(java.util.List)}.
     */
    @Test
    public final void testMergeDuplicateIntervals() {

        List<IntervalData> in = new ArrayList<IntervalData>();
        for (int i = 0; i < 15; i++) {
            IntervalData id = new IntervalData(new Date(123456789));
            for (int value = 0; value < 5; value++) {
                id.addValue(value);
            }
            in.add(id);
        }

        for (int i = 0; i < 10; i++) {
            IntervalData id = new IntervalData(new Date(124456788));
            for (int value = 0; value < 5; value++) {
                id.addValue(value * 2);
            }
            in.add(id);
        }

        List<IntervalData> out = ProtocolTools.mergeDuplicateIntervals(in);
        assertNotNull(out);
        assertEquals(2, out.size());

    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#setObisCodeField(com.energyict.obis.ObisCode, int, byte)}.
     */
    @Test
    public final void testSetObisCodeField() {
        for (int fieldNr = 0; fieldNr < LONG_NAME.length; fieldNr++) {
            for (byte value = Byte.MIN_VALUE; value < Byte.MAX_VALUE; value++) {
                byte[] ln = LONG_NAME;
                ln[fieldNr] = value;
                assertEquals(ObisCode.fromByteArray(ln), ProtocolTools.setObisCodeField(ObisCode.fromByteArray(LONG_NAME), fieldNr, value));
            }
        }

    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#setObisCodeField(com.energyict.obis.ObisCode, int, byte)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testSetObisCodeFieldToLowException() {
        ProtocolTools.setObisCodeField(ObisCode.fromByteArray(LONG_NAME), -1, (byte) 0x01);
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#setObisCodeField(com.energyict.obis.ObisCode, int, byte)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testSetObisCodeFieldToHighException() {
        ProtocolTools.setObisCodeField(ObisCode.fromByteArray(LONG_NAME), 6, (byte) 0x01);
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#setObisCodeField(com.energyict.obis.ObisCode, int, byte)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testSetObisCodeFieldObisNullException() {
        ProtocolTools.setObisCodeField(null, 0, (byte) 0x01);
    }

    @Test
    public void testSetRegisterValueObisCode() throws Exception {
        ObisCode obis1 = ObisCode.fromString("1.0.1.8.0.255");
        ObisCode obis2 = ObisCode.fromString("1.1.1.8.0.255");
        Quantity quantity = new Quantity("5", Unit.get("kWh"));
        String text = "OBIS = " + obis1.toString();
        RegisterValue reg1 = new RegisterValue(obis1, quantity, new Date(123), new Date(456), new Date(789), new Date(), 5, text);
        RegisterValue reg2 = ProtocolTools.setRegisterValueObisCode(reg1, obis2);

        assertEquals(obis2, reg2.getObisCode());
        assertEquals(reg1.getQuantity(), reg2.getQuantity());
        assertEquals(reg1.getEventTime(), reg2.getEventTime());
        assertEquals(reg1.getFromTime(), reg2.getFromTime());
        assertEquals(reg1.getToTime(), reg2.getToTime());
        assertEquals(reg1.getReadTime(), reg2.getReadTime());
        assertEquals(reg1.getRtuRegisterId(), reg2.getRtuRegisterId());
        assertEquals(reg1.getText(), reg2.getText());

    }

    @Test
    public void testCreateCalendar() throws Exception {
        Calendar calendar = ProtocolTools.createCalendar(2010, 9, 27, 14, 44, 5, 12);
        assertEquals(2010, calendar.get(Calendar.YEAR));
        assertEquals(9 - 1, calendar.get(Calendar.MONTH));
        assertEquals(27, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(14, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(44, calendar.get(Calendar.MINUTE));
        assertEquals(5, calendar.get(Calendar.SECOND));
        assertEquals(12, calendar.get(Calendar.MILLISECOND));
    }

    /**
     * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#concatByteArrays(byte[], byte[])}.
     */
    @Test
    public void testConcatByteArraysByteArray() throws Exception {
        byte[] array1 = ProtocolTools.getBytesFromHexString("$01$02$03");
        byte[] array2 = ProtocolTools.getBytesFromHexString("");
        byte[] array3 = ProtocolTools.getBytesFromHexString("$04$05$06");
        byte[] array4 = ProtocolTools.getBytesFromHexString("$11$22$33");
        byte[] array5 = ProtocolTools.getBytesFromHexString("");
        byte[] array6 = ProtocolTools.getBytesFromHexString("$55$88$FF");

        byte[] total123456 = ProtocolTools.getBytesFromHexString("$01$02$03$04$05$06$11$22$33$55$88$FF");
        byte[] total25 = ProtocolTools.getBytesFromHexString("");
        byte[] total234 = ProtocolTools.getBytesFromHexString("$04$05$06$11$22$33");

        assertArrayEquals(total123456, ProtocolTools.concatByteArrays(array1, array2, array3, array4, array5, array6));
        assertArrayEquals(total25, ProtocolTools.concatByteArrays(array2, array5));
        assertArrayEquals(total234, ProtocolTools.concatByteArrays(array2, array3, array4));
        assertArrayEquals(total234, ProtocolTools.concatByteArrays(array2, null, null, array3, array4));
        assertArrayEquals(array6, ProtocolTools.concatByteArrays(array6));
        assertArrayEquals(array6, ProtocolTools.concatByteArrays(null, null, array6, null, null));

    }
}
