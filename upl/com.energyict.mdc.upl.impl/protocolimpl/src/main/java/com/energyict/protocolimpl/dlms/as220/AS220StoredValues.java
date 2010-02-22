package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;
import java.util.Date;

import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;

/**
 * @author jme
 *
 */
public class AS220StoredValues implements StoredValues {

	/* (non-Javadoc)
	 * @see com.energyict.dlms.cosem.StoredValues#getBillingPointCounter()
	 */
	public int getBillingPointCounter() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.energyict.dlms.cosem.StoredValues#getBillingPointTimeDate(int)
	 */
	public Date getBillingPointTimeDate(int billingPoint) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.energyict.dlms.cosem.StoredValues#getHistoricalValue(com.energyict.obis.ObisCode)
	 */
	public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.energyict.dlms.cosem.StoredValues#getProfileGeneric()
	 */
	public ProfileGeneric getProfileGeneric() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.energyict.dlms.cosem.StoredValues#retrieve()
	 */
	public void retrieve() throws IOException {
		// TODO Auto-generated method stub

	}

}
