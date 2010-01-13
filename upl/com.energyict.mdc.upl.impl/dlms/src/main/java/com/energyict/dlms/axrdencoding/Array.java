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
import com.energyict.protocol.ProtocolUtils;

/**
 *
 * @author kvds
 */
public class Array extends AbstractDataType {

    List dataTypes;
    private int offsetBegin,offsetEnd;

    /** Creates a new instance of Array */
    public Array() {
        dataTypes = new ArrayList();
    }

    public Array(byte[] berEncodedData, int offset, int level) throws IOException {
        offsetBegin = offset;
        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_ARRAY) {
			throw new IOException("Array, invalid identifier "+berEncodedData[offset]);
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

    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) {
			strBuffTab.append("  ");
		}
        StringBuffer strBuff = new StringBuffer();
        strBuff.append(strBuffTab.toString()+"Array["+dataTypes.size()+"]:\n");
        Iterator it = dataTypes.iterator();
        while(it.hasNext()) {
            AbstractDataType adt = (AbstractDataType)it.next();

            strBuff.append(strBuffTab.toString()+adt);
        }
        return strBuff.toString();
    }

    protected int size() {
        return offsetEnd-offsetBegin;
    }

    protected byte[] doGetBEREncodedByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(DLMSCOSEMGlobals.TYPEDESC_ARRAY);
        baos.write(DLMSUtils.getAXDRLengthEncoding(dataTypes.size()));
        Iterator it = dataTypes.iterator();
        while(it.hasNext()) {
            AbstractDataType dt = (AbstractDataType)it.next();
            baos.write(dt.getBEREncodedByteArray());
        }
        return baos.toByteArray();
    }

    public Array addDataType(AbstractDataType dataType) {
        dataTypes.add(dataType);
        return this;
    }

    public AbstractDataType getDataType(int index) {
        return (AbstractDataType)dataTypes.get(index);
    }
    public int nrOfDataTypes() {
        return dataTypes.size();
    }

    static public void main(String[] args) {
        try {
            Structure s = new Structure();
            s.addDataType(new VisibleString("LGZ123456",20));
            s.addDataType(new TypeEnum(0));
            s.addDataType(new Array().addDataType(new Integer16(0xaa55)).addDataType(new Integer16(0xaa55)).addDataType(new Integer16(0xaa55)).addDataType(new Structure().addDataType(new VisibleString("TEST")).addDataType(new VisibleString("TEST")).addDataType(new VisibleString("TEST")).addDataType(new OctetString(new byte[]{1,2,3,4,5,6}))).addDataType(new OctetString(new byte[]{1,2,3})));
            System.out.println(ProtocolUtils.outputHexString(s.getBEREncodedByteArray()));

//            DataContainer dc = new DataContainer();
//            dc.parseObjectList(s.getBEREncodedByteArray(),null);
//            System.out.println(dc.print2strDataContainer());
//            System.out.println(dc.getRoot().getOctetString(0));

            System.out.println(AXDRDecoder.decode(s.getBEREncodedByteArray()));
            //System.out.println(AXDRDecoder.decode(s.getBEREncodedByteArray()).getStructure().getDataType(0).getVisibleString().getStr());

        }
        catch(IOException e) {
            e.printStackTrace();
        }
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
