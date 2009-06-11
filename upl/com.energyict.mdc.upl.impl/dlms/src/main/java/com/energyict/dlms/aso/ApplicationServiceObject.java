package com.energyict.dlms.aso;

import java.io.IOException;

import com.energyict.dlms.DLMSConnection;

/**
 * 
 * @author gna
 * 
 *<pre>
 * The applicationServiceObject has three main objectives.
 * - Establish/maintain/release application associations
 * - Data transfer
 * - Layer management...
 * </pre>
 */
public class ApplicationServiceObject {

	private int securityLevel;
	
	private XdlmsAse xDlmsAse;
	private AssociationControlServiceElement acse;
	private DLMSConnection dlmsConnection;
	
	public ApplicationServiceObject(XdlmsAse xDlmsAse, int securityLevel, String callingAuthenticationValue, DLMSConnection connection, int contextId){
		this.securityLevel = securityLevel;
		this.xDlmsAse = xDlmsAse;
		this.acse = new AssociationControlServiceElement(securityLevel, callingAuthenticationValue, this.xDlmsAse, contextId);
		this.dlmsConnection = connection;
	}
	
	/*******************************************************************************************************
	 * Application association management
	 *******************************************************************************************************/
	
	public void createAssociation() throws IOException{
		byte[] request = this.acse.createAssociationRequest();
		byte[] response = this.dlmsConnection.sendRequest(request);
		this.acse.analyzeAARE(response);
		if(this.securityLevel >= 2){
			throw new IOException("High level security is not yet implemented.");
		}
	}
	
	public void releaseAssociation() throws IOException{
		byte[] request = this.acse.releaseAssociationRequest();
		byte[] response = this.dlmsConnection.sendRequest(request);
		this.acse.analyzeRLRE(response);
	}
	
	/*******************************************************************************************************/
}