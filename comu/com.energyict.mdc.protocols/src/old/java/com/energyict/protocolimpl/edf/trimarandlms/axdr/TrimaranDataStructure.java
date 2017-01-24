/*
 * DataStructure.java
 *
 * Created on 3 april 2003, 17:18
 *
 * Changes:
 *     KV 16012003 add print method
 *                 add array2String method
 */

package com.energyict.protocolimpl.edf.trimarandlms.axdr;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.io.Serializable;
/**
 *
 * @author  Koen
 */
public class TrimaranDataStructure implements Serializable {

	/**	Generated serialVersionUID */
	private static final long serialVersionUID = -1718485361794272561L;
	/*
     *   An element object can be of type TrimaranDataStructure, TrimaranOctetString, Integer or String.
     *
     */
    protected Object element[];
    protected TrimaranDataStructure parent;

    // KV 16012004
    public void print() {
        for (int i = 0 ; i< element.length ; i++) {
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
            if (intarray[i] < 16){
            	strbuff.append("0");
            }
            strbuff.append(Integer.toHexString(intarray[i]));
        }
        return strbuff.toString();
    }

    public boolean isOctetString(int index) {
        if (element[index] instanceof TrimaranOctetString) {
			return true;
		} else {
			return false;
		}
    }

    public boolean isStructure(int index) {
        if (element[index] instanceof TrimaranDataStructure) {
			return true;
		} else {
			return false;
		}
    }
    public boolean isInteger(int index) {
        if (element[index].getClass().getName().compareTo("java.lang.Integer") == 0) {
			return true;
		} else {
			return false;
		}
    }
    public boolean isLong(int index) {
        if (element[index].getClass().getName().compareTo("java.lang.Long") == 0) {
			return true;
		} else {
			return false;
		}
    }
    public boolean isString(int index) {
        if (element[index].getClass().getName().compareTo("java.lang.String") == 0) {
			return true;
		} else {
			return false;
		}
    }

    public TrimaranDataStructure(int iNROfEntries) {
        element = new Object[iNROfEntries];
    } // public TrimaranDataStructure()

    public long getValue(int index) {
        if (isLong(index)) {
			return getLong(index);
		} else if (isInteger(index)) {
			return ((int)getInteger(index)& 0xffffffff);
		} else {
			return 0;
		}
    }

    public void addInteger(int index,int iValue) {
        element[index] = new Integer(iValue);
    }
    public int getInteger(int index) {
        return ((Integer)element[index]).intValue();
    }
    public void addLong(int index,long value) {
        element[index] = new Long(value);
    }
    public long getLong(int index) {
        return ((Long)element[index]).longValue();
    }

    public Long convert2Long(int index) throws IOException {
        if (isLong(index)) {
			return (Long)getElement(index);
		} else if (isInteger(index)) {
			return (new Long((long)getInteger(index)));
		} else if (isString(index)) {
			return (Long.decode(getString(index)));
		}

        throw new IOException("TrimaranDataStructure, Error converting element of type "+element[index].getClass().getName()+" to Long object");
    }

    public void addString(int index,String str) {
        element[index] = str;
    }
    public String getString(int index) {
        return (String)element[index];
    }

    public TrimaranOctetString getOctetString(int index) {
        return ((TrimaranOctetString)element[index]);
    }

    public void addStructure(int index,int iNROfElements) {
        element[index] = new TrimaranDataStructure(iNROfElements);
    }

    public TrimaranDataStructure getStructure(int index) {
        return (TrimaranDataStructure)element[index];
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
    public TrimaranDataStructure getParent() {
        return parent;
    }

} // class TrimaranDataStructure
