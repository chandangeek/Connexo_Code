/*
 * BitString.java
 *
 * Created on 16 oktober 2007, 11:35
 *
 */

package com.energyict.dlms.axrdencoding;

import com.energyict.mdc.upl.ProtocolException;

import com.energyict.dlms.DLMSUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.Iterator;

/**
 * @author kvds
 */
public class BitString extends AbstractDataType implements Iterable<Boolean> {

    private final BitSet bitSet;
    private final int nrOfBits;

    /**
     * Creates a new instance of BitString
     */
    public BitString(byte[] berEncodedData, int offset) throws IOException {

        if (berEncodedData[offset] != AxdrType.BIT_STRING.getTag()) {
            throw new ProtocolException("BitString, invalid identifier " + berEncodedData[offset]);
        }

        offset++;
        nrOfBits = DLMSUtils.getAXDRLength(berEncodedData, offset);
        offset += DLMSUtils.getAXDRLengthOffset(berEncodedData, offset);

        int nrOfBytes = nrOfBits / 8 + (nrOfBits % 8 != 0 ? 1 : 0);

        this.bitSet = new BitSet(this.nrOfBits);

        for (int i = 0; i < this.nrOfBits; i++) {
            final int byteNr = i / 8;
            final int bitNr = 7 - (i % 8);
            final int mask = (0x01 << bitNr);
            final int maskedByte = berEncodedData[(byteNr + offset)] & mask;
            this.bitSet.set(i, maskedByte != 0x00);
        }

    }

    public BitString(final boolean[] values) {
        this.nrOfBits = values.length;
        this.bitSet = new BitSet(this.nrOfBits);
        for (int i = 0; i < this.nrOfBits; i++) {
            this.bitSet.set(i, values[i]);
        }
    }

    public BitString(long value) {
        this(value, 64);
    }

    public BitString(int value) {
        this(value, 32);
    }

    // TODO this constructor has a bug, please fix it
    public BitString(final long value, final int size) {
        this.nrOfBits = size > Long.SIZE ? Long.SIZE : size;
        this.bitSet = new BitSet(this.nrOfBits);
        for (int i = 0; i < size; i++) {
            final long bitValue = (value >> i) & 0x01;
            this.bitSet.set(this.nrOfBits - (i + 1), bitValue != 0);
        }
    }

    /**
     * Improved BitString constructor
     * @param value
     * @param size
     * @param nrOfBits this parameter is ignored
     */
    public BitString(final long value, final int size, final int nrOfBits) {

        if (size > Long.SIZE || nrOfBits > Long.SIZE) {
            throw new IllegalArgumentException("Wrong size of bits for LONG!");
        }

        this.nrOfBits = size;
        this.bitSet = new BitSet(this.nrOfBits);
        for (int i = 0; i < size; i++) {
            final long bitValue = (value >> i) & 0x01;
            this.bitSet.set(this.nrOfBits - (i + 1), bitValue != 0);
        }
    }

    public BitString(final BigDecimal value, final int nrOfBits) {
        final BigInteger bigIntegerValue = value.toBigInteger();
        this.nrOfBits = nrOfBits;
        this.bitSet = new BitSet(this.nrOfBits);
        for (int i = 0; i < nrOfBits; i++) {
            final long bitValue = bigIntegerValue.shiftRight(i).testBit(0) ? 1 : 0;
            this.bitSet.set(this.nrOfBits - (i + 1), bitValue != 0);
        }
    }

    /**
     * Returns the number of bytes required.
     *
     * @return    The number of bytes required.
     */
    private int getNumberOfBytesRequired() {
        return (this.nrOfBits / 8) + (this.nrOfBits % 8 > 0 ? 1 : 0);
    }

    /**
     * Returns the data in the form of an integer array.
     *
     * @return    The data in the form of an integer array.
     */
    public byte[] getData() {
        final byte[] data = new byte[this.getNumberOfBytesRequired()];

        for (int i = 0; i < data.length; i++) {
            byte current = 0;

            for (int j = 0; j < 8; j++) {
                current |= (this.bitSet.get((i * 8) + j) ? (0x80 >>> j) : 0x00);
            }

            data[i] = current;
        }

        return data;
    }

    protected byte[] doGetBEREncodedByteArray() {
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();;

            stream.write(AxdrType.BIT_STRING.getTag());
            stream.write( DLMSUtils.getAXDRLengthEncoding( this.nrOfBits ) );
            stream.write( this.getData() );

            return stream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("IO error when encoding bitstring : [" + e.getMessage() + "]", e);
        }
    }

    /**
     * @return The number of bits used in the bit mask.
     */
    public int getNrOfBits() {
        return this.nrOfBits;
    }

    /**
     * Returns the value of the bit with the specified index. The value
     * is <code>true</code> if the bit with the index <code>bitIndex</code>
     * is currently set in this <code>BitSet</code>; otherwise, the result
     * is <code>false</code>.
     *
     * @param bitIndex the bit index.
     * @return the value of the bit with the specified index.
     * @throws IndexOutOfBoundsException if the specified index is out of range.
     */
    public final boolean get(final int bitIndex) {
        if (bitIndex >= this.nrOfBits) {
            throw new IndexOutOfBoundsException("Unable to get bit index [" + bitIndex + "] from bit string. This bit string only has [" + this.nrOfBits + "] bits.");
        }
        return this.bitSet.get(bitIndex);
    }

    /**
     * Sets the bit at the specified index to <code>true</code>.
     *
     * @param bitIndex a bit index.
     * @throws IndexOutOfBoundsException if the specified index is out of range.
     */
    public final void set(final int bitIndex) {
        set(bitIndex, true);
    }

    /**
     * Sets the bit at the specified index to the specified value.
     *
     * @param bitIndex a bit index.
     * @param value    a boolean value to set.
     * @throws IndexOutOfBoundsException if the specified index is out of range.
     */
    public final void set(final int bitIndex, final boolean value) {
        if (bitIndex >= this.nrOfBits) {
            throw new IndexOutOfBoundsException("Unable to set bit index [" + bitIndex + "] from bit string to [" + value + "]. This bit string only has [" + this.nrOfBits + "] bits.");
        }
        this.bitSet.set(bitIndex, value);
    }

    /**
     * The complete size of the bitstring when BER-encoded (including tags and length field)
     *
     * @return The length of the bitstring
     */
    protected int size() {
        return doGetBEREncodedByteArray().length;
    }

    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(longValue());
    }

    public int intValue() {
        return (int) (longValue() & 0x0FFFFFFFF);
    }

    public long longValue() {
        long value = 0;
        final int maxBits = Math.min(this.nrOfBits, Long.SIZE);
        for (int i = 0; i < maxBits; i++) {
            if (this.bitSet.get(maxBits - (i + 1))) {
                value |= 0x01 << i;
            }
        }
        return value;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getLevel(); i++) {
            sb.append("  ");
        }
        final String hexString = ProtocolTools.getHexStringFromBytes( this.getData() ).replaceAll("\\$", "");
        return sb.toString() + "BitString=0x" + hexString + "\n";
    }

    /**
     * Count the number of bits set or cleared, depending on the given value
     *
     * @param value True to count the bits that are set, false to count the other bits
     * @return The number of values that are the same as the given value
     */
    public final int getCount(final boolean value) {
        final int cardinality = this.bitSet.cardinality();
        return value ? cardinality : this.nrOfBits - cardinality;
    }

    /**
     * Checks if all bits are set or all bits are cleared
     *
     * @param value True to test if all bits are set, false to test for cleared
     * @return true if all bits == the given value
     */
    public final boolean areAllBits(final boolean value) {
        return getCount(value) == this.nrOfBits;
    }

    /**
     * @return the value of this {@link BitString} as a {@link }
     */
    public final BitSet asBitSet() {
        return this.bitSet;
    }

    public Iterator<Boolean> iterator() {
        return new Iterator<Boolean>() {

            private int index = 0;

            public boolean hasNext() {
                return this.index < BitString.this.getNrOfBits();
            }

            public Boolean next() {
                return BitString.this.get(this.index++);
            }

            public void remove() {
                throw new UnsupportedOperationException("Removing a bit from a BitString is not supported.");
            }
        };
    }
}
