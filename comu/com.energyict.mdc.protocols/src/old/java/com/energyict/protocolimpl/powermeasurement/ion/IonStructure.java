/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class IonStructure extends IonObject {

    Map attributes = new HashMap();

    void add(String name, Object value) {
        attributes.put(name, value);
    }
    
    Object get( String name ) {
        return attributes.get( name );
    }

    public String toString() {
        StringBuffer rslt = new StringBuffer();
        rslt.append("IonObject " + type + " [ ");
        Iterator i = attributes.keySet().iterator();
        while (i.hasNext()) {
            Object key = i.next();
            IonObject ion = (IonObject) attributes.get(key);
            rslt.append( key + "=\"" + ion + "\" " );
        }
        rslt.append("]");
        return rslt.toString();
    }

}