/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * PACTRegisterFactory.java
 *
 * Created on 26 maart 2004, 17:15
 */

package com.energyict.protocolimpl.pact.core.common;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import com.energyict.protocolimpl.pact.core.meterreading.MeterReadingsInterpreter;

import java.io.IOException;
import java.util.Date;
/**
 *
 * @author  Koen
 */
public class PACTRegisterFactory {

    private static final int DEBUG=0;

    private long timeOffset = 0;
    private Date oldMeterTime;

    ProtocolLink protocolLink;
    FileTransfer fileTransfer=null;

    MeterIdentitySerialNumber meterIdentitySerialNumber=null;
    MeterReadingsInterpreter meterReadingsInterpreter=null;

    /** Creates a new instance of PACTRegister */
    public PACTRegisterFactory(ProtocolLink protocolLink) {
        this.protocolLink=protocolLink;
    }


    /** Getter for property protocolLink.
     * @return Value of property protocolLink.
     *
     */
    public com.energyict.protocolimpl.pact.core.common.ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    /** Getter for property meterIdentitySerialNumber.
     * @return Value of property meterIdentitySerialNumber.
     *
     */
    public com.energyict.protocolimpl.pact.core.common.MeterIdentitySerialNumber getMeterIdentitySerialNumber() throws NestedIOException, ConnectionException {
        if (meterIdentitySerialNumber==null) {
            meterIdentitySerialNumber = new MeterIdentitySerialNumber(getProtocolLink().getPactConnection().sendStringRequest("s"));
            if (DEBUG >=1)
                System.out.println("KV_DEBUG>\n"+meterIdentitySerialNumber.toString());
        }
        return meterIdentitySerialNumber;
    }

    public MeterReadingsInterpreter getMeterReadingsInterpreter() throws NestedIOException, ConnectionException {
        if (meterReadingsInterpreter == null) {

        	setTimeOffset();

            // get raw meterreading data
            byte[] data=null;
            if (getProtocolLink().getPACTMode().isPACTStandard())
                data = getProtocolLink().getPactConnection().getMeterReadingData();
            else if (getProtocolLink().getPACTMode().isPAKNET())
                data = getProtocolLink().getPactConnection().getMeterReadingDataStream();

            // do authorization and validation
            if (getProtocolLink().getPACTToolkit() != null) {
                getFileTransfer().appendData(data);
                try {
                    int encrypted = getProtocolLink().getPACTToolkit().validateData(getFileTransfer().getFileName());
                    if (encrypted == 1) {
                       data = getFileTransfer().getDecryptedReadingsData();
                    }
                }
                catch(IOException e) {
                    throw new NestedIOException(e);
                }
            } // if (getProtocolLink().getPACTToolkit() != null)

            meterReadingsInterpreter = new MeterReadingsInterpreter(data,getProtocolLink());
            meterReadingsInterpreter.parse();
            setOldMeterDate(meterReadingsInterpreter.getCounters().getMeterDateTime());

            if (protocolLink.isExtendedLogging())
                protocolLink.getLogger().info(meterReadingsInterpreter.toString());
        }
        return meterReadingsInterpreter;
    }

    private void setTimeOffset(){
    	this.timeOffset = System.currentTimeMillis();
    }
    private long getTimeOffset(){
    	return this.timeOffset;
    }

    private void setOldMeterDate(Date meterDateTime) {
    	this.oldMeterTime = meterDateTime;
	}

    public Date getCurrentTime(){
    	if(oldMeterTime != null)
    		return new Date(oldMeterTime.getTime() + System.currentTimeMillis() - getTimeOffset());
    	else
    		return null;
    }


	/** Getter for property fileTransfer.
     * @return Value of property fileTransfer.
     *
     */
    public FileTransfer getFileTransfer() {
        if (fileTransfer == null)
            fileTransfer = new FileTransfer();
        return fileTransfer;
    }
}
