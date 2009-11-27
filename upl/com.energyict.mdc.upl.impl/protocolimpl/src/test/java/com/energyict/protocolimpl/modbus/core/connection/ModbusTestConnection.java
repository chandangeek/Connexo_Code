package com.energyict.protocolimpl.modbus.core.connection;

import java.io.InputStream;
import java.io.OutputStream;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;


/**
 * A test connection-class for a Modbus protocol
 * 
 * @author gna
 *
 */
public class ModbusTestConnection extends ModbusConnection {
	
	/** the expected response */
	private ResponseData responseData;

	public ModbusTestConnection() throws ConnectionException{
		super(null, null, Integer.valueOf(0), Integer.valueOf(0), Long.valueOf(0), Integer.valueOf(0), null,
				Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
	}
	
	public ModbusTestConnection(InputStream inputStream,
			OutputStream outputStream, int timeout, int maxRetries,
			long forcedDelay, int echoCancelling,
			HalfDuplexController halfDuplexController, int interframeTimeout,
			int responseTimeout, int physicalLayer) throws ConnectionException {
		super(inputStream, outputStream, timeout, maxRetries, forcedDelay,
				echoCancelling, halfDuplexController, interframeTimeout,
				responseTimeout, physicalLayer);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Overriding of the normal ModbusConnection sendRequest.
	 * This returns your previously SET ResponseData
	 */
	@Override
	public ResponseData sendRequest(RequestData requestData){
		return this.responseData;
	}
	
	/**
	 * Set the desired {@link ResponseData}
	 * @param responseData
	 */
	public void setResponseData(ResponseData responseData){
		this.responseData = responseData;
	}
	
}
