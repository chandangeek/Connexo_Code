package com.energyict.protocolimpl.enermet.e120;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

class SeriesResponse implements Response {

    private NackCode nackCode;
    private int registerIndex;
    private TreeMap valueMap; 

    SeriesResponse(NackCode nackCode) {
        this.nackCode = nackCode;
        valueMap = new TreeMap();
    }
    
    void setRegisterIndex(int registerIndex) {
        this.registerIndex = registerIndex;
    }

    SeriesResponse addValue( E120RegisterValue value ){
        valueMap.put(value.getTime(),value);
        return this;
    }
    
    public NackCode getNackCode(){
        return nackCode;
    }

    public Object getValue() {
        return valueMap;
    }

    public boolean isOk() {
        return NackCode.OK.equals(nackCode);
    }
    
    public Object get(Object key) {
        return valueMap.get(key);
    }

    public Set keySet() {
        return valueMap.keySet();
    }
    
    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        
        rslt.append("SeriesResponse [");
        rslt.append("registerIndex: ").append(registerIndex).append(", ");
        
        Iterator i = valueMap.values().iterator();
        while (i.hasNext()) {
            E120RegisterValue value = (E120RegisterValue) i.next();
            rslt.append("\n\t").append(value);
        }
        
        rslt.append("]");
        return rslt.toString();
    }

}
