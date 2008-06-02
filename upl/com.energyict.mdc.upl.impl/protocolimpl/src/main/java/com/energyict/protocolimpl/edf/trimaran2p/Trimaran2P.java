/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.APSEPDUFactory;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu.DLMSPDUFactory;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.Connection62056;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.ProtocolLink;

/**
 * @author gna
 *
 */
public class Trimaran2P extends AbstractProtocol implements ProtocolLink{
	
	private APSEPDUFactory aPSEFactory;
	private DLMSPDUFactory dLMSPDUFactory;
	private Connection62056 connection62056;
	private Trimaran2PProfile trimaran2PProfile;

	/**
	 * 
	 */
	public Trimaran2P() {
	}
	
	public static void main(String arg[]){
		
	}

	@Override
	protected void doConnect() throws IOException {
		getAPSEFactory().getAuthenticationReqAPSE();
		getDLMSPDUFactory().getInitiateRequest();
		getLogger().info(getDLMSPDUFactory().getStatusResponse().toString());
	}

	@Override
	protected void doDisConnect() throws IOException {
		
	}

	@Override
	protected List doGetOptionalKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ProtocolConnection doInit(InputStream inputStream,
			OutputStream outputStream, int timeoutProperty,
			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
			int protocolCompatible, Encryptor encryptor,
			HalfDuplexController halfDuplexController) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getFirmwareVersion() throws IOException, UnsupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProtocolVersion() {
		return "$Date$";
	}

	@Override
	public Date getTime() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTime() throws IOException {
		// TODO Auto-generated method stub
		
	}

	public APSEPDUFactory getAPSEFactory() {
		return aPSEFactory;
	}

	public Connection62056 getConnection62056() {
		// TODO Auto-generated method stub
		return null;
	}

	public DLMSPDUFactory getDLMSPDUFactory() {
		return dLMSPDUFactory;
	}

	/**
	 * @return the trimaran2PProfile
	 */
	protected Trimaran2PProfile getTrimaran2PProfile() {
		return trimaran2PProfile;
	}

	/**
	 * @param trimaran2PProfile the trimaran2PProfile to set
	 */
	protected void setTrimaran2PProfile(Trimaran2PProfile trimaran2PProfile) {
		this.trimaran2PProfile = trimaran2PProfile;
	}

}
