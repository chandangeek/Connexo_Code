package com.energyict.protocolimpl.iec1107.cewe.prometer;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.NestedIOException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.cbo.Utils;

/** 
 * Responsibilities:
 * - can read it's register counterpart in the meter ( getRawData() )
 * - if a ProRegister is cacheable, getRawData will only fetch it once  
 * - is able to parse all fields in a register ( asDate(), asInt(), as...() )  
 * 
 * If the register is cacheable, it can be parsed using all the "as...()" 
 * conversion methods.  However, if it is not cacheable, only the method
 * getRawData() is supported.  Every time the getRawData() is called, it 
 * will refetch the data from the meter.
 * 
 * To avoid this refetching it is possible to freeze a register.   
 * 
 * @author fbo */

class ProRegister {
    
    final static String NOT_SUPPORTED_EXCEPTION = 
        "Register is not cacheable, so method not supportted";
    
    /* reference to protocol object */
    private Prometer meter;
    /* protocol id of register */
    private String id;
    /* flag indicating of the register can be cached or not */
    private boolean cacheable;
    /* nr of records to be returned by command (default=1) */
    private int fetchSize;
    
    private String rawData;
    private List fields;
    
    /**
     * @param meter
     * @param id
     */
    ProRegister(Prometer meter, String id){
        this(meter, id, true);
    }
    
    /**
     * @param meter
     * @param id
     * @param cacheable
     */
    ProRegister(Prometer meter, String id, boolean cacheable){
        this(meter, id, cacheable, 1);
    }
    
    /**
     * @param meter
     * @param id
     * @param cacheable
     * @param fetchSize
     */
    ProRegister(Prometer meter, String id, boolean cacheable, int fetchSize){
        this.meter = meter;
        this.id = id;
        this.cacheable = cacheable;
        this.fetchSize = fetchSize;
    }
    
    /**
     * @param rawData
     */
    ProRegister(String rawData) {
        this.rawData = rawData;
        this.cacheable = true;
        String tmp = rawData.substring(1, rawData.length()-1); // remove braces ()
        fields = Arrays.asList(tmp.split(","));
    }
    
    /** id as byte[] */
    public byte[] getId(){
        return id.getBytes();
    }

    /** nr of fields */
    int size(){
        return fields.size();
    }
    
    /** iterator over all the fields */
    Iterator iterator(){
        return fields.iterator();
    }
    
    String getRawData() throws IOException {
        if( rawData == null ) {
            
            String tmp = meter.read(id + "(" + fetchSize + ")");
            if( ! cacheable ) return tmp;
            
            rawData = tmp;

        }
        return rawData;
    }
    
    ProRegister readAndFreeze( ) throws IOException {
        ProRegister register = new ProRegister( getRawData() );
        register.meter = meter;
        register.cacheable = true;
        return register;
    }
    
    void setCeweProMeter(Prometer meter){
        this.meter = meter;
    }
    
    /** parse field 0 as String */
    String asString() throws IOException {
        
        if( !cacheable ) throw createNotSupportedException();
        
        return asString(0);
        
    }
    
    /** parse field: fieldIdx as String */
    String asString(int fieldIdx) throws IOException {
        
        if( !cacheable ) throw createNotSupportedException();
        
        if( fields == null ) {
            String tmp = getRawData();
            tmp = tmp.substring(1, tmp.length()-1); // remove braces ()
            List f = Arrays.asList(tmp.split(",", -1));
            
            if( cacheable )
                fields = f;
            else 
                return (String)f.get(fieldIdx);
            
        }
        
        return (String)fields.get(fieldIdx);
        
    }
    
    /** parse field: fieldIdx as Double */
    Double asDouble(int fieldIdx) throws IOException {
        
        if( !cacheable ) throw createNotSupportedException();
        
        return new Double(asString(fieldIdx));
        
    }
    
    /** parse field 0 as Double */
    Double asDouble() throws IOException {
        
        if( !cacheable ) throw createNotSupportedException();
        
        return asDouble(0);
        
    }
    
    /** parse field: fieldIdx as BigDecimal */
    BigDecimal asBigDecimal(int fieldIdx) throws IOException {
        
        if( !cacheable ) throw createNotSupportedException();
        
        return new BigDecimal(asString(fieldIdx));
        
    }
    
    /** parse field 0 as BigDecimal */
    BigDecimal asBigDecimal() throws IOException {
        
        if( !cacheable ) throw createNotSupportedException();
        
        return asBigDecimal(0);
        
    }
    
    /** parse field: fieldIdx as int */
    int asInt(int fieldIdx) throws IOException {
        
        if( !cacheable ) throw createNotSupportedException();
        
        return Integer.parseInt(asString(fieldIdx));
        
    }
    
    /** parse field 0 as int */
    int asInt() throws IOException {
        
        if( !cacheable ) throw createNotSupportedException();
        
        return asInt(0);
        
    }

    /** parse field 0 as Date with sdf as DateFormat */
    Date asDate(SimpleDateFormat sdf) throws IOException {
        
        if( !cacheable ) throw createNotSupportedException();
        
        try {
            return sdf.parse(asString(0)+asString(1));
        } catch (ParseException e) {
            throw new NestedIOException(e);
        }
        
    }
    
    /** parse fieldas Date with short dateFormat */
    Date asShortDate(int fieldIdx) throws IOException {
        
        if( !cacheable ) throw createNotSupportedException();
        
        try {
            return meter.getShortDateFormat().parse(asString(fieldIdx));
        } catch (ParseException e) {
            throw new NestedIOException(e);
        }
        
    }
    
    /** parse field 0 as Quantity */
    Quantity asQuantity( ) throws IOException {
        return asQuantity(0);
    }
    
    /** parse field: fieldIdx as Quantity */
    Quantity asQuantity(int fieldIdx) throws IOException {

        String data = asString(fieldIdx);
        
        if( Utils.isNull(data) ) return null;
        
        int idx = data.indexOf('*');
        
        String number = data.substring(0, idx);
        String unit = data.substring(idx+1, data.length());
        
        return new Quantity( number, UnitParser.parse(unit) );
        
    }
    
    /** parse field: fieldIdx as Unit */
    Unit asUnit(int fieldIdx) throws IOException {
        return UnitParser.parse(asString(fieldIdx));
    }
    
    /** parse field 0 as Unit */
    Unit asUnit() throws IOException {
        return asUnit(0);
    }
    
    /** 19700101,000000 symantically means no date or NULL */
    boolean isNullDate() throws IOException{
        String tmp = getRawData();
        tmp = tmp.substring(1, tmp.length()-1); // remove braces ()
        return "19700101,000000".equals(tmp);
    }
    
    boolean isEmpty() throws IOException {
        return "(ID)".equals(getRawData());
    }
    
    boolean isEmpty(int fieldIdx) throws IOException {
        return asString(fieldIdx).length() == 0;
    }

    private ApplicationException createNotSupportedException( ) {
        return new ApplicationException( NOT_SUPPORTED_EXCEPTION );
    }
 
    public String toString( ){
        return "ProRegister[ " + id + "]";
    }
}
