package com.energyict.genericprotocolimpl.common;

import java.io.IOException;
import java.util.List;

/**
 * @author gna
 *
 */
public interface ConcentratorProtocol{
	
	/**
	 * Fetch a list from the concentrator which contains all identificationNumbers or
	 * serialNumbers from his slave devices.
	 * The list should be use to search for each device in the databases
	 * 
	 * @return a list of ID's
	 * 
	 * @throws IOException 
	 */
	List<String> getSlaveDevices() throws IOException;
	
	/**
	 * Process each slave device.
	 * Search for the respective device in the dataBase and execute his corresponding protocol.
	 * Setup loggers/journals etc. before starting the communication
	 * @param id - an ID for the meter
	 */
	void handleSlaveDevice(String id);

}
