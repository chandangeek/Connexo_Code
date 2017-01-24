package com.energyict.protocolimpl.ansi.c12;

import com.energyict.protocolimpl.ansi.c12.C1222Layer.ResponseControlEnum;
import com.energyict.protocolimpl.ansi.c12.C1222Layer.SecurityExtensionEnum;
import com.energyict.protocolimpl.ansi.c12.C1222Layer.SecurityModeEnum;

import java.io.ByteArrayOutputStream;

public class C1222Buffer 
{	
	private SecurityModeEnum securityMode = SecurityModeEnum.SecurityClearTextWithAuthentication;
	private ResponseControlEnum responseControl = ResponseControlEnum.ResponseControlAlways;
	private SecurityExtensionEnum securityExtension = SecurityExtensionEnum.ExtensionNo;
	private int uiExtra = 0;
	private int epsemSize = 0;
	private long initializationVector = 0x916A784B; // MToBigEndianUINT32((Muint32)MTime::GetCurrentUtcTime().GetTimeT());
	private boolean sessionless = false;
	private boolean securityKeyIdAndInitializationVectorWereSent = false;
	private byte epsemControl;
	private ByteArrayOutputStream userInformation = new ByteArrayOutputStream();
	private ByteArrayOutputStream command = new ByteArrayOutputStream();
	private ByteArrayOutputStream acse = new ByteArrayOutputStream();
	private ByteArrayOutputStream result = new ByteArrayOutputStream();
	private ByteArrayOutputStream canonifiedCleartext = new ByteArrayOutputStream();
	private C1222RequestParms requestParms = new C1222RequestParms();
	private C1222ResponseParms responseParms = new C1222ResponseParms();
	private long apInvocationId = 0x12345678;
	private byte[] mac = null;

	private String calledApTitle;
	private String securityKey;
	private String password;
	private int securityKeyId = 0;
	
	public int getSecurityKeyId() {
		return securityKeyId;
	}
	public String getSecurityKey() {
		return securityKey;
	}
	public void setSecurityKey(String securityKey) {
		this.securityKey = securityKey;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getCalledApTitle() {
		return calledApTitle;
	}
	public void setCalledApTitle(String calledApTitle) {
		this.calledApTitle = calledApTitle;
	}

	public C1222Buffer() {
	}

	public void reset()
	{
		userInformation.reset();
		command.reset();
		acse.reset();
		result.reset();
		canonifiedCleartext.reset();
		requestParms = new C1222RequestParms();
		responseParms.reset();  // Do a reset - some settings need to be kept during the whole session.
	}
	
	public long getApInvocationId() {
		return apInvocationId;
	}
	public void setApInvocationId(long apInvocationId) {
		this.apInvocationId = apInvocationId;
	}
	public C1222ResponseParms getResponseParms() {
		return responseParms;
	}
	public void setResponseParms(C1222ResponseParms responseParms) {
		this.responseParms = responseParms;
	}
	public C1222RequestParms getRequestParms() {
		return requestParms;
	}
	public ByteArrayOutputStream getCanonifiedCleartext() {
		return canonifiedCleartext;
	}
	public void setCanonifiedCleartext(ByteArrayOutputStream canonifiedCleartext) {
		this.canonifiedCleartext = canonifiedCleartext;
	}
	public ByteArrayOutputStream getResult() {
		return result;
	}
	public void setResult(ByteArrayOutputStream result) {
		this.result = result;
	}
	public byte[] getOutgoingBytes() {
		return null;
	}
	public byte[] getMac() {
		return mac;
	}
	public void setMac(byte[] mac) {
		this.mac = mac;
	}
	public ByteArrayOutputStream getAcse() {
		return acse;
	}
	public void setAcse(ByteArrayOutputStream acse) {
		this.acse = acse;
	}
	public ByteArrayOutputStream getCommand() {
		return command;
	}
	public void setCommand(ByteArrayOutputStream command) {
		this.command = command;
	}
	public SecurityModeEnum getSecurityMode() {
		return securityMode;
	}
	public void setSecurityMode(SecurityModeEnum securityMode) {
		this.securityMode = securityMode;
	}
	public ResponseControlEnum getResponseControl() {
		return responseControl;
	}
	public void setResponseControl(ResponseControlEnum responseControl) {
		this.responseControl = responseControl;
	}
	public SecurityExtensionEnum getSecurityExtension() {
		return securityExtension;
	}
	public void setSecurityExtension(SecurityExtensionEnum securityExtension) {
		this.securityExtension = securityExtension;
	}
	public int getUiExtra() {
		return uiExtra;
	}
	public void setUiExtra(int uiExtra) {
		this.uiExtra = uiExtra;
	}
	public int getEpsemSize() {
		return epsemSize;
	}
	public void setEpsemSize(int epsemSize) {
		this.epsemSize = epsemSize;
	}
	public long getInitializationVector() {
		return initializationVector;
	}
	public void setInitializationVector(long initializationVector) {
		this.initializationVector = initializationVector;
	}
	public boolean isSessionless() {
		return sessionless;
	}
	public void setSessionless(boolean sessionless) {
		this.sessionless = sessionless;
	}
	public boolean isSecurityKeyIdAndInitializationVectorWereSent() {
		return securityKeyIdAndInitializationVectorWereSent;
	}
	public void setSecurityKeyIdAndInitializationVectorWereSent(
			boolean securityKeyIdAndInitializationVectorWereSent) {
		this.securityKeyIdAndInitializationVectorWereSent = securityKeyIdAndInitializationVectorWereSent;
	}
	public ByteArrayOutputStream getUserInformation() {
		return userInformation;
	}
	public void setUserInformation(ByteArrayOutputStream userInformation) {
		this.userInformation = userInformation;
	}
	public byte getEpsemControl() {
		return epsemControl;
	}
	public void setEpsemControl(byte epsemControl) {
		this.epsemControl = epsemControl;
	}
}