/*
 * AbstractDataType.java
 *
 * Created on 17 oktober 2007, 14:19
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dlms.axrdencoding;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author kvds
 */
abstract public class AbstractDataType {
    
    abstract protected byte[] doGetBEREncodedByteArray() throws IOException;
    abstract protected int size();
    abstract public BigDecimal toBigDecimal( );
    abstract public int intValue();
    abstract public long longValue();
       
    
    private int level;
    
    /** Creates a new instance of AbstractDataType */
    public AbstractDataType() {
        
    } 
    
    public byte[] getBEREncodedByteArray() throws IOException {
        return doGetBEREncodedByteArray();
    }
    
    public Structure getStructure() {
        return (Structure)this;
    }
    public Array getArray() {
        return (Array)this;
    }
    public TypeEnum getTypeEnum() {
        return (TypeEnum)this;
    }
    
    public boolean isUnsigned8() {
        return this instanceof Unsigned8;
    }  
    public boolean isVisibleString() {
        return this instanceof VisibleString;
    }  
    public boolean isOctetString() {
        return this instanceof OctetString;
    }  
    
    public VisibleString getVisibleString() {
        return (VisibleString)this;
    }
    public OctetString getOctetString() {
        return (OctetString)this;
    }
    public Integer8 getInteger8() {
        return (Integer8)this;
    }
    public Integer16 getInteger16() {
        return (Integer16)this;
    }
    public Integer64 getInteger64() {
        return (Integer64)this;
    }
    public Unsigned8 getUnsigned8() {
        return (Unsigned8)this;
    }
    public Unsigned16 getUnsigned16() {
        return (Unsigned16)this;
    }
    public Unsigned32 getUnsigned32() {
        return (Unsigned32)this;
    }
    public BitString getBitString() {
        return (BitString)this;
    }
    public NullData getNullData() {
        return (NullData)this;
    }
    
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
            
    
}
