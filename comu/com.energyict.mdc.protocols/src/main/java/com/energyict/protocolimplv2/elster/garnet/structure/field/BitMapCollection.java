package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractBitMaskField;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class BitMapCollection<T extends AbstractBitMaskField> extends AbstractField<BitMapCollection> {

    private static final int DEFAULT_INNER_LENGTH_T = 1;

    private final int length;
    private final int nrOfMasks;
    private final int startIndex;

    private List<T> collection;
    private Class<? extends T> classOfBitMaskObject;

    public BitMapCollection(int length, int nr_of_masks, Class<? extends T> classOfBitMaskObject) {
        this(length, nr_of_masks, 0, classOfBitMaskObject);
    }

    public BitMapCollection(int length, int nr_of_masks, int startIndex, Class<? extends T> classOfBitMaskObject) {
        this.length = length;
        this.nrOfMasks = nr_of_masks;
        this.startIndex = startIndex;
        this.classOfBitMaskObject = classOfBitMaskObject;
        this.collection = new ArrayList<>(this.length);
    }

    @Override
    public byte[] getBytes() {
        BitSet bitset = new BitSet(length * 8);
        for (int i = 0; i < nrOfMasks; i++) {
            for (int y = 0; y < getInnerLengthOfT(); y++) {
                bitset.set(startIndex + (i * getInnerLengthOfT()) + y, collection.get(i).getBitMask().get(y));
            }
        }
        byte[] bytes = flipByteOrder(bitset.toByteArray());
        while (bytes.length < length) {
            bytes = ProtocolTools.concatByteArrays(new byte[1], bytes); // Left pad with additional zeros
        }
        return bytes;
    }

    @Override
    public BitMapCollection parse(byte[] rawData, int offset) throws ParsingException {
        byte[] bitsMap = ProtocolTools.getSubArray(rawData, offset, offset + length);
        bitsMap = flipByteOrder(bitsMap);    // BitSet.valueOf reads bytes in little endian encoding!
        BitSet bitSet = BitSet.valueOf(bitsMap);
        for (int i = startIndex; i < (startIndex + nrOfMasks); i++) {
            T bitMaskObject = (T) getNewInstanceOfT().parse(bitSet, i);
            collection.add(bitMaskObject);
        }
        return this;
    }

    private byte[] flipByteOrder(byte[] byteArray) {
        return ProtocolTools.reverseByteArray(byteArray);
    }

    private T getNewInstanceOfT() throws ParsingException {
        try {
            return classOfBitMaskObject.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ParsingException(e);
        }
    }

    private int getInnerLengthOfT() {
        try {
            return getNewInstanceOfT().getLength();
        } catch (ParsingException e) {
            return DEFAULT_INNER_LENGTH_T;    // We should normally not reach this point
        }
    }

    @Override
    public int getLength() {
        return length;
    }

    public List<T> getAllBitMasks() {
        return collection;
    }

    public void addElementToCollection(T element) {
        collection.add(element);
    }
}