/*
 * ExtendedRegister.java
 *
 * Created on 19 augustus 2004, 8:53
 */

package com.energyict.dlms.cosem;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.VisibleString;

import java.io.IOException;
import java.util.Date;

import static com.energyict.dlms.DLMSCOSEMGlobals.CLOCK_OBJECT_LN;
import static com.energyict.dlms.DLMSCOSEMGlobals.EXTENDED_REGISTER_CAPTURE_TIME;
import static com.energyict.dlms.DLMSCOSEMGlobals.EXTENDED_REGISTER_STATUS;

/**
 *
 * @author  Koen
 * Changes:
 * GNA |03022009| Added method to get an attributes abstractDataType
 * GNA |03062009| Getter/Setter for Status attribute
 */
public class ExtendedRegister extends Register implements CosemObject {
    public final int DEBUG=0;
    public static final int CLASSID= DLMSClassId.EXTENDED_REGISTER.getClassId();

    Date captureTime;
    boolean captureTimeCached=false;

    /** Creates a new instance of ExtendedRegister */
    public ExtendedRegister(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }


    public String toString() {
        try {
           return super.toString()+", captureTime="+getCaptureTime();
        }
        catch(IOException e) {
           return "ExtendedRegister, toString, retrieving error!";
        }
    }

    /**
     * Getter for property captureTime.
     * @return Value of property captureTime.
     */
    public Date getCaptureTime() throws IOException {
        if (captureTimeCached) {
			return captureTime;
		} else {
			return (getCaptureTime(getResponseData(EXTENDED_REGISTER_CAPTURE_TIME)));
		}
    }

    public Date getCaptureTime(byte[] responseData) throws IOException {
        Clock clock = new Clock(protocolLink,getObjectReference(CLOCK_OBJECT_LN,protocolLink.getMeterConfig().getClockSN()));
        captureTime = clock.getDateTime(responseData);
        return captureTime;
    }


    public void setCaptureTime(Date captureTime) throws IOException {
        this.captureTime=captureTime;
        captureTimeCached=true;
    }

    public void setCaptureTime(OctetString octetString) throws IOException {
        Clock clock = new Clock(protocolLink,getObjectReference(CLOCK_OBJECT_LN,protocolLink.getMeterConfig().getClockSN()));
        clock.setDateTime(octetString);
        captureTime = clock.getDateTime();
        captureTimeCached=true;
    }

    public Date getBillingDate() {
        Date retValue;

        retValue = super.getBillingDate();
        return retValue;
    }

    public int getResetCounter() {
        int retValue;

        retValue = super.getResetCounter();
        return retValue;
    }
    protected int getClassId() {
        return CLASSID;
    }

    public String getText() throws IOException {
        try {
            DataContainer dc = new DataContainer();
            dc.parseObjectList(getResponseData(EXTENDED_REGISTER_STATUS),protocolLink.getLogger());
            return dc.getText(",");
        }
        catch(IOException e) {
            if (e.toString().indexOf("R/W denied")>=0) {
				return null; //"This extended register status field is R/W denied";
			}
            throw e;
        }
    }

    /**
     * @return the status of the register
     * @throws java.io.IOException if resulted dataType is not supported or when read failed.
     */
    public long getStatus() throws IOException {
    	return DLMSUtils.parseValue2long(getResponseData(EXTENDED_REGISTER_STATUS));
    }

     /**  To be used when the status element has data-type 'Visible-string' or 'octet-string'
     * @return the status of the register
     * @throws java.io.IOException if resulted dataType is not supported or when read failed.
     */
    public String getStatusText() throws IOException {
        VisibleString visibleString = new VisibleString(getResponseData(EXTENDED_REGISTER_STATUS), 0);
        return visibleString.getStr();
    }

    /**
     * @param status - The dataType is manufactures specific
     * @throws java.io.IOException when writing failed, possible dataType not supported
     */
    public void setStatus(AbstractDataType status) throws IOException {
    	write(EXTENDED_REGISTER_STATUS, status.getBEREncodedByteArray());
    }

}
