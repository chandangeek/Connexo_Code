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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;

/**
 *
 * @author kvds
 */
public class Array extends AbstractDataType {

	private List<AbstractDataType> dataTypes;
	private int offsetBegin, offsetEnd;

	/**
	 * Creates a new instance of Array
	 */
	public Array() {
		dataTypes = new ArrayList<AbstractDataType>();
	}

    /**
     * Constructor with an initial number of dataTypes. The same amount of dataTypes are added as null objects to the List
     *
     * @param nrOfDataTypes the number of datatypes
     */
    public Array(int nrOfDataTypes) {
        dataTypes = new ArrayList<AbstractDataType>(nrOfDataTypes);
        for(int i = 0; i < nrOfDataTypes; i++){
            dataTypes.add(null);
        }
    }

	public Array(byte[] berEncodedData, int offset, int level) throws IOException {
		offsetBegin = offset;
		if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_ARRAY) {
			throw new IOException("Array, invalid identifier " + berEncodedData[offset]);
		}
		offset++;
		dataTypes = new ArrayList<AbstractDataType>();
		int length = (int) DLMSUtils.getAXDRLength(berEncodedData, offset);
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
		StringBuffer strBuffTab = new StringBuffer();
		for (int i = 0; i < getLevel(); i++) {
			strBuffTab.append("  ");
		}
		StringBuffer strBuff = new StringBuffer();
		strBuff.append(strBuffTab.toString() + "Array[" + dataTypes.size() + "]:\n");
		Iterator<AbstractDataType> it = dataTypes.iterator();
		while (it.hasNext()) {
			AbstractDataType adt = it.next();
			strBuff.append(strBuffTab.toString() + adt);
		}
		return strBuff.toString();
	}

	protected int size() {
		return offsetEnd - offsetBegin;
	}

	protected byte[] doGetBEREncodedByteArray() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(DLMSCOSEMGlobals.TYPEDESC_ARRAY);
			baos.write(DLMSUtils.getAXDRLengthEncoding(dataTypes.size()));
			Iterator<AbstractDataType> it = dataTypes.iterator();
			while (it.hasNext()) {
				AbstractDataType dt = it.next();
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
}
