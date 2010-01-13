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
import com.energyict.protocol.ProtocolUtils;

/**
 *
 * @author kvds
 */
public class Structure extends AbstractDataType {

    protected List dataTypes;
    private int offsetBegin,offsetEnd;
    int autoIndex=0;

    /** Creates a new instance of Structure */
    public Structure() {
        dataTypes = new ArrayList();
    }

    public Structure(byte[] berEncodedData, int offset, int level) throws IOException {
        offsetBegin = offset;
        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_STRUCTURE) {
			throw new IOException("Structure, invalid identifier "+berEncodedData[offset]);
		}
        offset++;
        dataTypes = new ArrayList();
        int length = (int)DLMSUtils.getAXDRLength(berEncodedData,offset);
        offset += DLMSUtils.getAXDRLengthOffset(berEncodedData,offset);
        //setLevel(getLevel()+1);
        for (int i=0;i<length;i++) {
           AbstractDataType adt = AXDRDecoder.decode(berEncodedData,offset,getLevel()+1);
           adt.setLevel(level);
           dataTypes.add(adt);
           offset+=adt.size();
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
        return (AbstractDataType)dataTypes.get(autoIndex++);
    }

    public AbstractDataType getDataType() {
        return (AbstractDataType)dataTypes.get(autoIndex==0?0:autoIndex-1);
    }

    public AbstractDataType getDataType(int index) {
    	autoIndex=index+1;
        return (AbstractDataType)dataTypes.get(index);
    }
    public int nrOfDataTypes() {
        return dataTypes.size();
    }

    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) {
			strBuffTab.append("  ");
		}
        StringBuffer strBuff = new StringBuffer();
        strBuff.append(strBuffTab.toString()+"Structure("+dataTypes.size()+"):\n");
        Iterator it = dataTypes.iterator();
        while(it.hasNext()) {
            AbstractDataType adt = (AbstractDataType)it.next();
            strBuff.append(strBuffTab.toString()+adt);
        }
        return strBuff.toString();
    }

    protected byte[] doGetBEREncodedByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(DLMSCOSEMGlobals.TYPEDESC_STRUCTURE);
        baos.write(DLMSUtils.getAXDRLengthEncoding(dataTypes.size()));
        Iterator it = dataTypes.iterator();
        while(it.hasNext()) {
            AbstractDataType dt = (AbstractDataType)it.next();
            if (dt==null) {
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
        return offsetEnd-offsetBegin;
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


    static public void main(String[] args) {
        try {
            Structure s = new Structure();
            s.addDataType(new VisibleString("LGZ123456",20));
            s.addDataType(new TypeEnum(0));
            s.addDataType(new Structure().addDataType(new Integer16(0x2a55)).addDataType(new Unsigned8(12)).addDataType(null));
            s.addDataType(new Structure().addDataType(new Unsigned32(0x7fff)).addDataType(new Unsigned8(128)));
            s.addDataType(new VisibleString("End",20));
            s.addDataType(new Unsigned16(20));
            s.addDataType(new Integer64(-123456789123456789L));
            s.addDataType(new Integer32(-1235564));
            s.addDataType(new Integer8(-12));
            s.addDataType(new Integer16(1309));
            byte[] data = s.getBEREncodedByteArray();
            System.out.println(ProtocolUtils.outputHexString(data));

            System.out.println(AXDRDecoder.decode(data));

            System.out.println(s.nrOfDataTypes());
            System.out.println(s.getDataType(0));
            System.out.println(s.getDataType(1));
            System.out.println(s.getDataType(9));
            System.out.println(s.getDataType(10));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }



}
