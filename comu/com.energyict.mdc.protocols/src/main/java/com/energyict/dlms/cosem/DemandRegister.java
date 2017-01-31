/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.Quantity;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import static com.energyict.dlms.DLMSCOSEMGlobals.CLOCK_OBJECT_LN;
import static com.energyict.dlms.DLMSCOSEMGlobals.DEMAND_REGISTER_CAPTURE_TIME;
import static com.energyict.dlms.DLMSCOSEMGlobals.DEMAND_REGISTER_CURRENT_AVERAGE_VALUE;
import static com.energyict.dlms.DLMSCOSEMGlobals.DEMAND_REGISTER_LAST_AVERAGE_VALUE;
import static com.energyict.dlms.DLMSCOSEMGlobals.DEMAND_REGISTER_NUMBER_OF_PERIODS;
import static com.energyict.dlms.DLMSCOSEMGlobals.DEMAND_REGISTER_PERIOD;
import static com.energyict.dlms.DLMSCOSEMGlobals.DEMAND_REGISTER_SCALER_UNIT;
import static com.energyict.dlms.DLMSCOSEMGlobals.DEMAND_REGISTER_START_TIME_CURRENT;
import static com.energyict.dlms.DLMSCOSEMGlobals.DEMAND_REGISTER_STATUS;


/**
 *
 * @author  Koen
 * Changes:
 * GNA |03022009| Added method to get an attributes abstractDataType
 */
public class DemandRegister extends AbstractCosemObject implements CosemObject {
    public final int DEBUG=0;
    long currentAverage;
    long lastAverage;
    ScalerUnit scalerUnit=null;
    Date captureTime;
    boolean captureTimeCached=false;
    Date startTimeCurrent;
    long period=-1;
    long numberOfPeriods=-1;

    /** Creates a new instance of DemandRegister */
    public DemandRegister(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }

    public String toString() {
        try {
        return "currentAverage="+getCurrentAverage()+
               ", lastAverage="+getLastAverage()+
               ", scalerUnit="+getScalerUnit()+
               ", captureTime="+getCaptureTime()+
               ", startTimeCurrent="+getStartTimeCurrent()+
               ", period="+getPeriod()+
               ", numberOfPeriods="+getNumberOfPeriods();
        }
        catch(IOException e) {
           return "DemandRegister, toString, retrieving error!";
        }
    }

    /**
     * Getter for property currentAverage.
     * @return Value of property currentAverage.
     */
    public long getCurrentAverage() throws IOException {
        currentAverage = getLongData(DEMAND_REGISTER_CURRENT_AVERAGE_VALUE);
        return currentAverage;
    }


    /**
     * Getter for property lastAverage.
     * @return Value of property lastAverage.
     */
    public long getLastAverage() throws IOException {
        lastAverage = getLongData(DEMAND_REGISTER_LAST_AVERAGE_VALUE);
        return lastAverage;
    }



    /**
     * Getter for property scalerUnit.
     * @return Value of property scalerUnit.
     */
    public ScalerUnit getScalerUnit() throws IOException {
        if (scalerUnit == null) {
            byte[] responseData = getResponseData(DEMAND_REGISTER_SCALER_UNIT);
            scalerUnit = new ScalerUnit(responseData);
        }
        return scalerUnit;
    }

    /**
     * Getter for property captureTime.
     * @return Value of property captureTime.
     */
    public java.util.Date getCaptureTime() throws IOException {
        if (captureTimeCached) {
			return captureTime;
		} else {
			return (getCaptureTime(getResponseData(DEMAND_REGISTER_CAPTURE_TIME)));
		}
    }

    public java.util.Date getCaptureTime(byte[] responseData) throws IOException {
        Clock clock = new Clock(protocolLink,getObjectReference(CLOCK_OBJECT_LN,protocolLink.getMeterConfig().getClockSN()));
        captureTime = clock.getDateTime(responseData);
        return captureTime;
    }

    public void setCaptureTime(OctetString octetString) throws IOException {
        Clock clock = new Clock(protocolLink,getObjectReference(CLOCK_OBJECT_LN,protocolLink.getMeterConfig().getClockSN()));
        clock.setDateTime(octetString);
        captureTime = clock.getDateTime();
        captureTimeCached=true;
    }

    /**
     * Getter for property startTimeCurrent.
     * @return Value of property startTimeCurrent.
     */
    public java.util.Date getStartTimeCurrent() throws IOException {
        Clock clock = new Clock(protocolLink,getObjectReference(CLOCK_OBJECT_LN,protocolLink.getMeterConfig().getClockSN()));
        startTimeCurrent = clock.getDateTime(getResponseData(DEMAND_REGISTER_START_TIME_CURRENT));
        return startTimeCurrent;
    }



    /**
     * Getter for property period.
     * @return Value of property period.
     */
    public long getPeriod() throws IOException {
        if (period==-1) {
			period = getLongData(DEMAND_REGISTER_PERIOD);
		}
        return period;
    }



    /**
     * Getter for property numberOfPeriods.
     * @return Value of property numberOfPeriods.
     */
    public long getNumberOfPeriods() throws IOException {
        if (numberOfPeriods==-1) {
			numberOfPeriods = getLongData(DEMAND_REGISTER_NUMBER_OF_PERIODS);
		}
        return numberOfPeriods;
    }

    public Quantity getQuantityValue() throws IOException {
        return new Quantity(new BigDecimal(getValue()),getScalerUnit().getEisUnit());
    }

    public long getValue() throws IOException {
        return getCurrentAverage();
    }

    public String getText() throws IOException {
        try {
            DataContainer dc = new DataContainer();
            dc.parseObjectList(getResponseData(DEMAND_REGISTER_STATUS),protocolLink.getLogger());
            return dc.getText(",");
        }
        catch(IOException e) {
            if (e.toString().indexOf("R/W denied")>=0) {
				return null; //This demand register status field is R/W denied";
			}
            throw e;
        }

    }

    public Date getBillingDate() {
        return null;
    }

    public int getResetCounter() {
        return -1;
    }

    protected int getClassId() {
        return DLMSClassId.DEMAND_REGISTER.getClassId();
    }

}
