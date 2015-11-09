/*
 * DataStructure.java
 *
 * Created on 3 april 2003, 17:18
 *
 * Changes:
 *     KV 16012003 add print method
 *                 add array2String method
 */

package com.energyict.dlms;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author  Koen
 */
public class DataStructure implements Serializable {

	/*
	 *   An element object can be of type DataStructure, OctetString, Integer or String.
	 *
	 */
	public Object element[];
	public DataStructure parent;

	// KV 16012004
	public void print() {
		for (int i = 0 ; i< this.element.length ; i++) {
			if (isOctetString(i)) {
				System.out.print(array2String(getOctetString(i).getArray())+" ");
			}
			else if (isLong(i)) {
				System.out.print(String.valueOf(getLong(i))+" ");
			}
			else if (isInteger(i)) {
				System.out.print(String.valueOf(getInteger(i))+" ");
			}
			else if (isString(i)) {
				System.out.print(getString(i)+" ");
			}
			else if (isStructure(i)) {
				getStructure(i).print();
			}
		}
	}

	// KV 16012004
	public String array2String(byte[] array) {
		StringBuffer strbuff = new StringBuffer();
		int[] intarray = ProtocolUtils.toIntArray(array);
		for (int i=0;i<intarray.length;i++) {
			if (intarray[i] < 16) {
				strbuff.append("0");
			}
			strbuff.append(Integer.toHexString(intarray[i]));
		}
		return strbuff.toString();
	}

	public boolean isOctetString(int index) {
		if(this.element[index] == null){
			return false;
		}
		if (this.element[index] instanceof OctetString) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isStructure(int index) {
		if(this.element[index] == null){
			return false;
		}
		if (this.element[index] instanceof DataStructure) {
			return true;
		} else {
			return false;
		}
	}
	public boolean isInteger(int index) {
		if(this.element[index] == null){
			return false;
		}
		if (this.element[index].getClass().getName().compareTo("java.lang.Integer") == 0) {
			return true;
		} else {
			return false;
		}
	}
	public boolean isLong(int index) {
		if(this.element[index] == null){
			return false;
		}
		if (this.element[index].getClass().getName().compareTo("java.lang.Long") == 0) {
			return true;
		} else {
			return false;
		}
	}

	public BigDecimal getBigDecimalValue(int index) throws ProtocolException {
		if (isLong(index)) {
			return BigDecimal.valueOf(getLong(index));
		} else if (isInteger(index)) {
			return BigDecimal.valueOf(getInteger(index));
		} else if (isFloat(index)) {
			return BigDecimal.valueOf(getFloat(index));
		} else {
			throw new ProtocolException("Cannot parse element " + index + " (" + getElement(index).toString() + ") of the structure as a number");
		}
	}

	public boolean isFloat(int index) {
		Object element = this.element[index];
		return (element != null) && (element instanceof Float);
	}

	public boolean isString(int index) {
		if(this.element[index] == null){
			return false;
		}
		if (this.element[index].getClass().getName().compareTo("java.lang.String") == 0) {
			return true;
		} else {
			return false;
		}
	}

	public DataStructure(int iNROfEntries) {
		this.element = new Object[iNROfEntries];
	} // public DataStructure()

	public long getValue(int index) {
		if (isLong(index)) {
			return getLong(index);
		} else if (isInteger(index)) {
			return (getInteger(index)& 0xffffffff);
		} else {
			return 0;
		}
	}

	public void addInteger(int index,int iValue) {
		this.element[index] = new Integer(iValue);
	}
	public int getInteger(int index) {
		return ((Integer)this.element[index]).intValue();
	}
	public void addLong(int index,long value) {
		this.element[index] = new Long(value);
	}
	public long getLong(int index) {
		return ((Long)this.element[index]).longValue();
	}
    public void addFloat(int index,float value) {
		this.element[index] = new Float(value);
	}
	public float getFloat(int index) {
		return ((Float)this.element[index]).floatValue();
	}

	public Long convert2Long(int index) throws IOException {
		if (isLong(index)) {
			return (Long)getElement(index);
		} else if (isInteger(index)) {
			return (new Long(getInteger(index)));
		} else if (isString(index)) {
			return (Long.decode(getString(index)));
		} else if (isOctetString(index)) {
			return (Long.decode(getOctetString(index).toString()));
		}

		throw new ProtocolException("DataStructure, Error converting element of type "+this.element[index].getClass().getName()+" to Long object");
	}

	public String convert2String(int index) throws IOException {
		if (isLong(index)) {
			return ""+getElement(index);
		} else if (isInteger(index)) {
			return ""+(new Long(getInteger(index)));
		} else if (isString(index)) {
			return getString(index);
		}

		throw new ProtocolException("DataStructure, Error converting element of type "+this.element[index].getClass().getName()+" to String object");
	}

	public void addString(int index,String str) {
		this.element[index] = str;
	}
	public String getString(int index) {
		return (String)this.element[index];
	}

	public OctetString getOctetString(int index) {
		return ((OctetString)this.element[index]);
	}

	public void addStructure(int index,int iNROfElements) {
		this.element[index] = new DataStructure(iNROfElements);
	}

	public DataStructure getStructure(int index) {
		return (DataStructure)this.element[index];
	}

	/**
	 * Getter for property element.
	 * @return Value of property element.
	 */
	public java.lang.Object[] getElements() {
		return this.element;
	}

	public int getNrOfElements() {
		return getElements().length;
	}

	public Object getElement(int index) {
		return getElements()[index];
	}

	/**
	 * Getter for property parent.
	 * @return Value of property parent.
	 */
	public com.energyict.dlms.DataStructure getParent() {
		return this.parent;
	}

} // class DataStructure
