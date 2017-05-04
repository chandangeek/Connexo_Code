/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ApplicationData.java
 *
 * Created on 18 juni 2003, 14:02
 */

package com.energyict.protocolimpl.mbus.core;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.mbus.core.connection.iec870.IEC870CIField;
import com.energyict.protocolimpl.mbus.core.connection.iec870.IEC870ConnectionException;

import java.io.IOException;
import java.util.TimeZone;



/**
 *
 * @author  Koen
 */
public class ApplicationData {

    int cIField;
    byte[] data=null;
    int length=-1;


    /**
     * Creates a new instance of ApplicationData
     */
    public ApplicationData(int cIField) throws IEC870ConnectionException {
        this.cIField=cIField; //1
    }


    public ApplicationData(int cIField,byte[] data) throws IEC870ConnectionException {
        this.cIField = cIField;
        this.data = data;
    }

    public ApplicationData(byte[] data) throws IEC870ConnectionException {
        this.cIField = data[0];
        this.data = ProtocolUtils.getSubArray2(data, 1,data.length-1);
    }


    public String toString() {
        return ProtocolUtils.outputHexString(data);
    }

    public AbstractCIField buildAbstractCIFieldObject(TimeZone timeZone) throws IOException {
        AbstractCIField obj=null;;
        switch(getCIField()) {

            case 0x72: {
                  obj = new CIField72h(timeZone);
                  obj.parse(getData());
            } break;

            case 0x51: {
                  obj = new CIField51h();
                  obj.parse(getData());
            } break;

            case 0x52: {
                  obj = new CIField52h();
                  obj.parse(getData());
            } break;

        } // switch(getCIField())

        return obj;

    } // public void parse()


    public byte[] getData() throws IEC870ConnectionException {
        return data;
    }
    public int getCIField() {
        return cIField;
    }
    public String getCIFieldDescription() {
        return IEC870CIField.getCIField(getCIField()).getDescription();
    }


} // public class ApplicationData
