package com.energyict.protocolimpl.dlms.elster.ek2xx;

import java.io.IOException;
import java.util.Date;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;

public class StoredValuesImpl implements StoredValues {

	private static final int DEBUG 	= 0;

	private CosemObjectFactory cof 		= null;
	private ProtocolLink protocolLink 	= null;
	
    public StoredValuesImpl(CosemObjectFactory cof) {
        this.cof=cof;
        protocolLink = cof.getProtocolLink();
    }
	
	public int getBillingPointCounter() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Date getBillingPointTimeDate(int billingPoint) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public HistoricalValue getHistoricalValue(ObisCode obisCode)
			throws IOException {
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
