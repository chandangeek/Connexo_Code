package com.energyict.protocolimpl.elster.opus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;

public class Opus implements MeterProtocol{

	private final String oldPassword;
	private final String newPassword;
	private final int outstationID;	
	
	private InputStream inputStream;
	private OutputStream outputStream;
	private int timeOut=5000;

	Opus(){
		this.oldPassword="--------";
		this.newPassword="--------";
		this.outstationID=7; // for testing purposes only!!!!!!!!!!
	}
	
	Opus(String oldPassword, String newPassword, int outstationID){
		this.oldPassword=oldPassword;
		this.newPassword=newPassword;
		this.outstationID=outstationID;
	}

	protected void doConnect() throws IOException {
		// TODO Auto-generated method stub
		System.out.println("test");
		
	}

	protected void doDisConnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	protected List doGetOptionalKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	protected ProtocolConnection doInit(InputStream inputStream,
			OutputStream outputStream, int timeoutProperty,
			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
			int protocolCompatible, Encryptor encryptor,
			HalfDuplexController halfDuplexController) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	protected void doValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
	}

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProtocolVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getTime() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTime() throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void connect() throws IOException {
		// testrun
		OpusCommandFactory ocf=new OpusCommandFactory(outstationID,oldPassword,newPassword,inputStream,outputStream);
		ocf.setNumChan(15);
		ocf.setCap(0);  // hard on 0
		ocf.setPeriod(1);
		ocf.setDateOffset(0); // hard on 0
		ocf.command(3 ,5,timeOut);  // command and maximum attempts
		ocf.command(4 ,5,timeOut);  // command and maximum attempts
		ocf.command(5 ,5,timeOut);  // command and maximum attempts
		ocf.command(81,5,timeOut); // command and maximum attempts
	}

	public void disconnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	public Object fetchCache(int arg0) throws SQLException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getCache() {
		// TODO Auto-generated method stub
		return null;
	}

	public Quantity getMeterReading(int arg0) throws UnsupportedException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Quantity getMeterReading(String arg0) throws UnsupportedException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumberOfChannels() throws UnsupportedException, IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public ProfileData getProfileData(boolean arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public ProfileData getProfileData(Date arg0, boolean arg1)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public ProfileData getProfileData(Date arg0, Date arg1, boolean arg2)
			throws IOException, UnsupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getProfileInterval() throws UnsupportedException, IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getRegister(String arg0) throws IOException,
			UnsupportedException, NoSuchRegisterException {
		// TODO Auto-generated method stub
		return null;
	}

	public void init(InputStream inputStream, OutputStream outputStream, TimeZone arg2,
			Logger arg3) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("init");
        this.inputStream = inputStream;
        this.outputStream = outputStream;
		
	}

	public void initializeDevice() throws IOException, UnsupportedException {
		// TODO Auto-generated method stub
		
	}

	public void release() throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void setCache(Object arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setProperties(Properties arg0) throws InvalidPropertyException,
			MissingPropertyException {
		// TODO Auto-generated method stub
		
	}

	public void setRegister(String arg0, String arg1) throws IOException,
			NoSuchRegisterException, UnsupportedException {
		// TODO Auto-generated method stub
		
	}

	public void updateCache(int arg0, Object arg1) throws SQLException,
			BusinessException {
		// TODO Auto-generated method stub
		
	}

	public List getOptionalKeys() {
		// TODO Auto-generated method stub
		ArrayList list = new ArrayList();
		return list;
	}

	public List getRequiredKeys() {
		// TODO Auto-generated method stub
		ArrayList list = new ArrayList();
		return list;
	}

}
