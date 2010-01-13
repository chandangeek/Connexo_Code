/*
 * Structure.java
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
public class Structure extends AbstractDataType {

	protected List<AbstractDataType> dataTypes;
	private int offsetBegin, offsetEnd;
	int autoIndex = 0;

	/** Creates a new instance of Structure */
	public Structure() {
		dataTypes = new ArrayList<AbstractDataType>();
	}

	public Structure(byte[] berEncodedData, int offset, int level) throws IOException {
		offsetBegin = offset;
		if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_STRUCTURE) {
			throw new IOException("Structure, invalid identifier " + berEncodedData[offset]);
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

	public boolean hasMoreElements() {
		if (autoIndex == nrOfDataTypes()) {
			return false;
		} else {
			return true;
		}
	}

	public AbstractDataType getNextDataType() {
		return dataTypes.get(autoIndex++);
	}

	public AbstractDataType getDataType() {
		return dataTypes.get(autoIndex == 0 ? 0 : autoIndex - 1);
	}

	public AbstractDataType getDataType(int index) {
		autoIndex = index + 1;
		return dataTypes.get(index);
	}

	public int nrOfDataTypes() {
		return dataTypes.size();
	}

	public String toString() {
		StringBuffer strBuffTab = new StringBuffer();
		for (int i = 0; i < getLevel(); i++) {
			strBuffTab.append("  ");
		}
		StringBuffer strBuff = new StringBuffer();
		strBuff.append(strBuffTab.toString() + "Structure(" + dataTypes.size() + "):\n");
		Iterator<AbstractDataType> it = dataTypes.iterator();
		while (it.hasNext()) {
			AbstractDataType adt = it.next();
			strBuff.append(strBuffTab.toString() + adt);
		}
		return strBuff.toString();
	}

	protected byte[] doGetBEREncodedByteArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(DLMSCOSEMGlobals.TYPEDESC_STRUCTURE);
		baos.write(DLMSUtils.getAXDRLengthEncoding(dataTypes.size()));
		Iterator<AbstractDataType> it = dataTypes.iterator();
		while (it.hasNext()) {
			AbstractDataType dt = it.next();
			if (dt == null) {
				baos.write(0);
			} else {
				baos.write(dt.getBEREncodedByteArray());
			}
		}
		return baos.toByteArray();
	}

	public Structure addDataType(AbstractDataType dataType) {
		dataTypes.add(dataType);
		return this;
	}

	protected int size() {
		return offsetEnd - offsetBegin;
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
