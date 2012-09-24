/**
 * 
 */
package com.energyict.protocolimpl.modbus.schneider.compactnsx;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;

import java.io.IOException;
import java.util.*;

/**
 * @author gna
 *
 */
public class CompactNSX extends Modbus {

	/**
	 * 
	 */
	public CompactNSX() {
	}

	protected void doTheConnect() throws IOException {
	}

	protected void doTheDisConnect() throws IOException {
	}

	protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        return result;
	}

	protected void doTheValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		 setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout", "25").trim()));
		 setInfoTypePhysicalLayer(1);
	}

	protected void initRegisterFactory() {
		setRegisterFactory(new RegisterFactory(this));
	}

	public DiscoverResult discover(DiscoverTools discoverTools) {
		return null;
	}

    /** Protocol version **/
    public String getProtocolVersion() {
        return "$Date$";
    }
    
    public String getFirmwareVersion() throws IOException {
    	return getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification().toString();
    }
    
    public void setTime() throws IOException {
    	byte[] time = perpareCurrentTime();
    	byte[] regValues = prepareCommand(769, 18, 768, "0", time);
    	getRegisterFactory().findRegister("Buffer").getWriteMultipleRegisters(regValues);
    }

	public Date getTime() throws IOException{
    	byte[] regValues = prepareCommand(768, 10, 768, "0", null);
    	getRegisterFactory().findRegister("Buffer").getWriteMultipleRegisters(regValues);
    	long timeout = System.currentTimeMillis() + 3000;
    	while(true){
    		int status = (Integer)getRegisterFactory().findRegister("CommandStatus").objectValueWithParser("IntegerParser");
    		if(status != 3){
    			if(status == 0){
    				break;
    			} else {
    				throw new IOException("Could not read time, errorCode: " + status);
    			}
    		}
    	}
    	return getRegisterFactory().findRegister("Date").dateValue();
    }
	
    /*******************************************************************************************
    R e g i s t e r P r o t o c o l  i n t e r f a c e 
    *******************************************************************************************/
   public RegisterValue readRegister(ObisCode obisCode) throws IOException {
       try {
           return new RegisterValue(obisCode,getRegisterFactory().findRegister(obisCode).quantityValue());
       }
       catch(ModbusException e) {
           if ((e.getExceptionCode()==0x02) && (e.getFunctionErrorCode()==0x83))
               throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
           else
               throw e;
       }
   }
    
	/**
	 * 
	 * @param commandCode - Execution code (ex: 768 - readTime; 769 - setTime)
	 * @param numbOfPars - Number of parameters to be set
	 * @param destination - Destination
	 * @param passWord - Password, contains 4 chars, default use 0000
	 * @param data - data to be written if any
	 * @return a composed byteArray
	 */
    private byte[] prepareCommand(int commandCode, int numbOfPars, int destination, String passWord, byte[] data){
    	byte[] result = new byte[40];
    	result[0] = (byte) ((commandCode>>8)&0xFF);
    	result[1] = (byte) (commandCode&0xFF);
    	result[2] = (byte) ((numbOfPars>>8)&0xFF);
    	result[3] = (byte) (numbOfPars&0xFF);
    	result[4] = (byte) ((destination>>8)&0xFF);
    	result[5] = (byte) (destination&0xFF);
    	result[6] = 0; result[7] = 0;
    	if(passWord.equalsIgnoreCase("")){
    		passWord = "0";
    	}
    	int pass = Integer.parseInt(passWord);
    	result[8] = (byte)((pass>>24)&0xFF);
    	result[9] = (byte)((pass>>16)&0xFF);
    	result[10] = (byte)((pass>>8)&0xFF);
    	result[11] = (byte)(pass&0xFF);
    	if(data != null){
    		System.arraycopy(data, 0, result, 12, data.length);
    	}
    	result[34] = (byte) 0x1F;
    	result[35] = (byte) 0x53;
    	result[36] = (byte) 0x1F;
    	result[37] = (byte) 0x54;
    	result[38] = (byte) 0x1F;
    	result[39] = (byte) 0x55;
    	return result;
    }

    private byte[] perpareCurrentTime() {
    	byte[] time = new byte[8];
    	Calendar cal = Calendar.getInstance(gettimeZone());
    	time[0] = (byte) (cal.get(Calendar.MONTH) +1);
    	time[1] = (byte) (cal.get(Calendar.DAY_OF_MONTH));
    	time[2] = (byte) (cal.get(Calendar.YEAR)-2000);
    	time[3] = (byte) (cal.get(Calendar.HOUR_OF_DAY));
    	time[4] = (byte) (cal.get(Calendar.MINUTE));
    	time[5] = (byte) (cal.get(Calendar.SECOND));
    	time[6] = (byte) ((cal.get(Calendar.MILLISECOND)>>8)&0xFF);
    	time[7] = (byte) ((cal.get(Calendar.MILLISECOND))&0xFF);
    	return time;
	}
}
