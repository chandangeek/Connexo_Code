/**
 * MK10GENERIC.java
 * 
 * Created on 8-jan-2009, 12:47:25 by jme
 * 
 */
package com.energyict.genericprotocolimpl.edmi.mk10;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.CommunicationScheduler;

/**
 * @author jme
 *
 */
public class MK10Push implements GenericProtocol {

	private static final int DEBUG = 0;

	/*
	 * Constructors
	 */

	// TODO Auto-generated Constructors stub


	/*
	 * Private getters, setters and methods
	 */

	// TODO Auto-generated Private getters, setters and methods stub


	/*
	 * Public methods
	 */
	
	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		// TODO Auto-generated method stub
	}

	/*
	 * Public getters and setters
	 */

	public String getVersion() {
		return "$Date: 2009-01-08 13:02:10 +0200 (di, 26 aug 2008) $";
	}

	public List getOptionalKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	public List getRequiredKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addProperties(Properties properties) {
		// TODO Auto-generated method stub
	}

}
