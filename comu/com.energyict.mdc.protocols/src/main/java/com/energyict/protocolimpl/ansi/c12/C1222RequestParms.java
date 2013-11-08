package com.energyict.protocolimpl.ansi.c12;

public class C1222RequestParms 
{
	private String applicationContext = "";
	private String callingApTitle = "1.3.6.1.4.1.33507.1919.77.1";
	private String standardApplicationContextOid = "2.16.124.113620.1.22";
	private String standardNetworkContextOid = "2.16.124.113620.1.22.0";
	private String elsterOid = "1.3.6.1.4.1.33507.1";
	private String edClass = "";    // Note: this should be a Hex-string (e.g. "A10F")
	private int aeQualifier = -1;
	private int sessionIdleTimeout = 60;

	public String getApplicationContext() {
		return applicationContext;
	}
	public void setApplicationContext(String applicationContext) {
		this.applicationContext = applicationContext;
	}
	public String getCallingApTitle() {
		return callingApTitle;
	}
	public void setCallingApTitle(String callingApTitle) {
		this.callingApTitle = callingApTitle;
	}
	public String getStandardApplicationContextOid() {
		return standardApplicationContextOid;
	}
	public void setStandardApplicationContextOid(
			String standardApplicationContextOid) {
		this.standardApplicationContextOid = standardApplicationContextOid;
	}
	public String getStandardNetworkContextOid() {
		return standardNetworkContextOid;
	}
	public void setStandardNetworkContextOid(String standardNetworkContextOid) {
		this.standardNetworkContextOid = standardNetworkContextOid;
	}
	public String getElsterOid() {
		return elsterOid;
	}
	public void setElsterOid(String elsterOid) {
		this.elsterOid = elsterOid;
	}
	public String getEdClass() {
		return edClass;
	}
	public void setEdClass(String edClass) {
		this.edClass = edClass;
	}
	public int getAeQualifier() {
		return aeQualifier;
	}
	public void setAeQualifier(int aeQualifier) {
		this.aeQualifier = aeQualifier;
	}
	public int getSessionIdleTimeout() {
		return sessionIdleTimeout;
	}
	public void setSessionIdleTimeout(int sessionIdleTimeout) {
		this.sessionIdleTimeout = sessionIdleTimeout;
	}
}