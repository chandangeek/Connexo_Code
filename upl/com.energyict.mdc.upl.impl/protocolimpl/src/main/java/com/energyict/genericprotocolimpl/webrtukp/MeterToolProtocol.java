package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocol.MissingPropertyException;

/**
 * Protocols that have to be accessible to the MeterTool should implement this interface. Implementors may not assume a database to be present.
 * 
 * @author Tom De Greyt
 */
public interface MeterToolProtocol {

	/**
	 * Allows setting whether the protocol should enforce the correct serial number. This method will typically be used to disable this check.
	 * 
	 * @param enforce true if the meter's serial number should be checked, false otherwise
	 */
	public void setEnforceSerialNumber(boolean enforce);

	/**
	 * Allows setting the protocol properties.
	 * 
	 * @param properties the properties to add.
	 */
	public void addProperties(Properties properties);

	/**
	 * Allows setting the protocol's cache.
	 * 
	 * @param cacheObject the cacheObject
	 */
	public void setCache(Object cacheObject);

	/**
	 * @return the protocol's cache
	 */
	public Object getCache();

	/**
	 * Sets the logger the protocol should use.
	 * 
	 * @param logger the logger
	 */
	public void setLogger(Logger logger);

	/**
	 * Initializes the Link to use.
	 * 
	 * @param link the link
	 */
	public void setLink(Link link);

	/**
	 * Initializing global objects
	 * 
	 * @throws IOException - can be cause by the TCPIPConnection
	 * @throws DLMSConnectionException - could not create a dlmsConnection
	 * @throws BusinessException
	 * @throws SQLException when a database exception occurred
	 */
	public void init() throws IOException, DLMSConnectionException, SQLException, BusinessException;

	/**
	 * Makes a connection to the server, if the socket is not available then an error is thrown. After a successful connection, we initiate an authentication request.
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public void connect() throws IOException, SQLException, BusinessException;

	/**
	 * After every communication, we close the connection to the meter.
	 * 
	 * @throws IOException
	 * @throws DLMSConnectionException
	 */
	public void disConnect() throws IOException;

	/**
	 * @return the DLMSMeterConfig for the connected meter
	 */
	public DLMSMeterConfig getMeterConfig();

	/**
	 * Validates the Properties
	 * 
	 * @throws MissingPropertyException
	 */
	public void validateProperties() throws MissingPropertyException;

	/**
	 * @return a CosemObjectFactory that can be used to read data from the meter.
	 */
	public CosemObjectFactory getCosemObjectFactory();

}
