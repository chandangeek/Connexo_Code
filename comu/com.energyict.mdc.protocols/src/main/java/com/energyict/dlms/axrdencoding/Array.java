/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Array.java
 *
 * Created on 17 oktober 2007, 14:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dlms.axrdencoding;

import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.DLMSUtils;
import com.google.common.base.Strings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * @author kvds
 */
public class Array extends AbstractDataType implements Iterable<AbstractDataType> {

	private List<AbstractDataType> dataTypes;
	private int offsetBegin, offsetEnd;

	/**
	 * Creates a new instance of Array
	 */
	public Array() {
        this(new AbstractDataType[0]);
	}

    public Array(AbstractDataType... dataTypes) {
        this.dataTypes = new ArrayList<>();
        for (AbstractDataType dataType : dataTypes) {
            addDataType(dataType);
        }
    }

    /**
     * Constructor with an initial number of dataTypes. The same amount of dataTypes are added as null objects to the List
     *
     * @param nrOfDataTypes the number of datatypes
     */
    public Array(int nrOfDataTypes) {
        dataTypes = new ArrayList<>(nrOfDataTypes);
        for(int i = 0; i < nrOfDataTypes; i++){
            dataTypes.add(null);
        }
    }

	public Array(byte[] berEncodedData, int offset, int level) throws IOException {
		offsetBegin = offset;
		if (berEncodedData[offset] != AxdrType.ARRAY.getTag()) {
			throw new ProtocolException("Array, invalid identifier " + berEncodedData[offset]);
		}
		offset++;
		dataTypes = new ArrayList<>();
		int length = DLMSUtils.getAXDRLength(berEncodedData, offset);
		offset += DLMSUtils.getAXDRLengthOffset(berEncodedData, offset);
		// setLevel(getLevel()+1);
		for (int i = 0; i < length; i++) {
			AbstractDataType adt = AXDRDecoder.decode(berEncodedData, offset, getLevel() + 1);
            adt.setLevel(level);
			dataTypes.add(adt);
			offset += adt.size();
		}
		offsetEnd = offset;
	}

	public String toString() {
		StringBuilder strBuff = new StringBuilder();
		String indent = Strings.repeat("  ", getLevel());
		strBuff.append(indent).append("Array[").append(dataTypes.size()).append("]:\n");
		for (AbstractDataType adt : dataTypes) {
			strBuff.append(indent).append(adt);
		}
		return strBuff.toString();
	}

	protected int size() {
		return offsetEnd - offsetBegin;
	}

	protected byte[] doGetBEREncodedByteArray() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(AxdrType.ARRAY.getTag());
			baos.write(DLMSUtils.getAXDRLengthEncoding(dataTypes.size()));
			for (AbstractDataType dt : dataTypes) {
				baos.write(dt.getBEREncodedByteArray());
			}
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * Add the given dataType at the end of the current dataTypes-List
     *
     * @param dataType the dataType to add
     * @return this array
     */
	public Array addDataType(AbstractDataType dataType) {
		dataTypes.add(dataType);
		return this;
	}

    /**
     * Sets the given {@link com.energyict.dlms.axrdencoding.AbstractDataType} to the given index in the dataType list
     *
     * @param index    the index of the list to update the datatype
     * @param dataType the dataType to add
     */
    public void setDataType(int index, AbstractDataType dataType) {
        dataTypes.set(index, dataType);
    }

	public AbstractDataType getDataType(int index) {
		return dataTypes.get(index);
	}

    /**
     * Getter for a list of all the {@link com.energyict.dlms.axrdencoding.AbstractDataType}s
     *
     * @return all the dataTypes
     */
    public List<AbstractDataType> getAllDataTypes(){
        return this.dataTypes;
    }

	public int nrOfDataTypes() {
		return dataTypes.size();
	}

	public BigDecimal toBigDecimal() {
		return null;
	}

	public int intValue() {
		return -1;
	}

	public long longValue() {
		return -1;
	}

    public Iterator<AbstractDataType> iterator() {
        return new ArrayIterator(this);
    }

    private class ArrayIterator implements Iterator<AbstractDataType> {

        private final Array arrayObject;
        private int index;

        private ArrayIterator(Array arrayObject) {
            this.arrayObject = arrayObject;
            this.index = 0;
        }

        public boolean hasNext() {
            return this.index < arrayObject.nrOfDataTypes();
        }

        public AbstractDataType next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No next element available in array.");
            }
            AbstractDataType item = arrayObject.getDataType(index);
            this.index++;
            return item;
        }

        public void remove() {
            throw new UnsupportedOperationException("remove() method is not supported");
        }

    }

    public <T extends AbstractDataType> T getDataType(int index, Class<T> expectedClass) throws IOException {
        final int dataTypes = nrOfDataTypes();
        if (dataTypes <= index) {
            throw new ProtocolException("Invalid index [" + index + "] while reading [" + expectedClass.getSimpleName() + "]. Array contains only [" + dataTypes + "] items.");
        }
        final AbstractDataType dataType = getDataType(index);
        if (!dataType.getClass().getName().equalsIgnoreCase(expectedClass.getName())) {
            throw new ProtocolException("Invalid dataType at index [" + index + "]. Expected [" + expectedClass.getSimpleName() + "] but received [" + dataType.getClass().getSimpleName() + "]");
        }
        return (T) getDataType(index);
    }

}
