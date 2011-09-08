package com.energyict.smartmeterprotocolimpl.common;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;

import java.io.*;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Protocols that have to be accessible to the MeterTool should implement this
 * interface. Implementors may not assume a database to be present.
 *
 */
public interface SmartMeterToolProtocol {

	/**
	 * Allows setting the protocol properties.
	 *
	 * @param properties the properties to add.
	 */
	void addProperties(Properties properties);

	/**
	 * Allows setting the protocol's cache.
	 *
	 * @param cacheObject the cacheObject
	 */
	void setCache(Object cacheObject);

	/**
	 * @return the protocol's cache
	 */
	Object getCache();

	/**
	 * Initializing global objects
	 *
	 * @throws java.io.IOException - can be cause by the TCPIPConnection
	 */
	void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException;

	/**
	 * Makes a connection to the server, if the socket is not available then an
	 * error is thrown. After a successful connection, we initiate an
	 * authentication request.
	 *
	 * @throws java.io.IOException
	 */
	void connect() throws IOException;

	/**
	 * After every communication, we close the connection to the meter.
	 *
	 * @throws java.io.IOException
	 * @throws com.energyict.dlms.DLMSConnectionException
	 */
	void disConnect() throws IOException;

	/**
	 * @return the DLMSMeterConfig for the connected meter
	 */
	DLMSMeterConfig getMeterConfig();

	/**
	 * Validates the Properties
	 *
	 * @throws com.energyict.protocol.MissingPropertyException
	 */
	void validateProperties() throws MissingPropertyException, InvalidPropertyException;

	/**
	 * @return a CosemObjectFactory that can be used to read data from the meter.
	 */
	CosemObjectFactory getCosemObjectFactory();

}
