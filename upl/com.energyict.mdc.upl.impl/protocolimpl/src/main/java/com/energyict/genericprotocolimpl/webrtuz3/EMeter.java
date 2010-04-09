package com.energyict.genericprotocolimpl.webrtuz3;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;

/**
 * Copyrights EnergyICT
 *
 * @since 9-apr-2010 13:51:14
 * @author jme
 */
public class EMeter implements GenericProtocol {

	private CommunicationProfile	commProfile;

	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		this.commProfile = scheduler.getCommunicationProfile();

	}

	public long getTimeDifference() {
		return 0;
	}

	public void addProperties(Properties properties) {

	}

	public String getVersion() {
		return "$Date$";
	}

	public List getOptionalKeys() {
		return new ArrayList(0);
	}

	public List getRequiredKeys() {
		return new ArrayList(0);
	}

}
