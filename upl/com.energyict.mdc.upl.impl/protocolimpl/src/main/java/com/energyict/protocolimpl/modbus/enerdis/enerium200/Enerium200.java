package com.energyict.protocolimpl.modbus.enerdis.enerium200;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.MeterInfo;

public class Enerium200 extends Modbus {

	private static final int DEBUG 	= 0;
	
	private MeterInfo meterInfo 	= null;
	
	protected void doTheConnect() throws IOException {

	}

	protected void doTheDisConnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	protected List doTheGetOptionalKeys() {
		List returnList = new ArrayList(0);
		// TODO Auto-generated method stub
		return returnList;
	}

	protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		// TODO Auto-generated method stub
	}

	protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
        getRegisterFactory().setZeroBased(false);
	}

	public DiscoverResult discover(DiscoverTools discoverTools) {
		DiscoverResult discover = new DiscoverResult();
		
		
		return discover;
	}

	/*
	 * Constructors
	 */


	
	/*
	 * Private getters, setters and methods
	 */

	public RegisterFactory getRegisterFactory() {
		return (RegisterFactory) super.getRegisterFactory();
	}
	
    int[] readRawValue(int address, int length)  throws IOException {
        
        HoldingRegister r = new HoldingRegister(address, length);
        r.setRegisterFactory(getRegisterFactory());
        return r.getReadHoldingRegistersRequest().getRegisters();
    
    }

	/*
	 * Public methods
	 */

    public Date getTime() throws IOException {
    	return getMeterInfo().getTime();
    }

    public void setTime() throws IOException {
    	byte[] rawClock = new byte[6];
    	Calendar cal = ProtocolUtils.getCleanGMTCalendar();
    	cal.setTime(new Date());
    	cal.add(Calendar.HOUR, 1);
    	long date = cal.getTimeInMillis() / 1000;
    	
    	rawClock[0] = (byte) (0x01);
    	rawClock[1] = (byte) (0x04);
    	rawClock[2] = (byte)((date & 0x000000FF000000L) >> 24);
    	rawClock[3] = (byte)((date & 0x00000000FF0000L) >> 16);
    	rawClock[4] = (byte)((date & 0x0000000000FF00L) >> 8);
    	rawClock[5] = (byte)((date & 0x000000000000FFL) >> 0);
    	
    	getRegisterFactory().writeTime.getWriteMultipleRegisters(rawClock);
    }
    
    private String getSerialNumber() throws IOException {
    	return getMeterInfo().getSerialNumber();
    }
    
    protected void validateSerialNumber() throws IOException {
       if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) return;
       String sn = getSerialNumber();
       if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
       throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
    }
    
	/*
	 * Public getters and setters
	 */

    private MeterInfo getMeterInfo() throws IOException {
		if (this.meterInfo == null) {
	    	meterInfo = (MeterInfo) getRegisterFactory().meterInfo.value(); meterInfo.printInfo();
		}
		return meterInfo;
	}

}
