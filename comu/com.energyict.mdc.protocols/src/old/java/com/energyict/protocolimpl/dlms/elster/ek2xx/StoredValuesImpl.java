package com.energyict.protocolimpl.dlms.elster.ek2xx;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.Date;

/**
 * @author jme
 * @since 17-aug-2009
 */
public class StoredValuesImpl implements StoredValues {

	public StoredValuesImpl(CosemObjectFactory cof) {}

	public int getBillingPointCounter() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Date getBillingPointTimeDate(int billingPoint) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public ProfileGeneric getProfileGeneric() {
		// TODO Auto-generated method stub
		return null;
	}

	public void retrieve() throws IOException {
		// TODO Auto-generated method stub
	}
}
