package com.energyict.protocolimpl.cm10;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dialer.coreimpl.ATDialer;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;

public class CM10 extends AbstractProtocol {
	
	private CM10Connection cm10Connection = null;
	private CM10Profile cm10Profile = null;
    private CommandFactory commandFactory=null;
    private ObisCodeMapper obisCodeMapper = new ObisCodeMapper(this);
    private RegisterFactory registerFactory;
    
    private StatusTable statusTable;

    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getCM10Profile().getProfileData(lastReading,includeEvents);
    }
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    } 
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getStatusTable().getNumberOfChannels();
    }
    
    protected void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((getInfoTypeSerialNumber() == null) || 
            ("".compareTo(getInfoTypeSerialNumber())==0)) return;
    }
    
	protected void doConnect() throws IOException {
		getLogger().info("doConnect");
		StatusTable statusTable = getStatusTable();
		getLogger().info(statusTable.toString());
		getLogger().info("endConnect");
	}

	public StatusTable getStatusTable() throws IOException {
		if (statusTable == null) {
			getLogger().info("start getStatus");
			CommandFactory commandFactory = getCommandFactory();
			Response response = 
				commandFactory.getReadStatusCommand().invoke();
			statusTable = new StatusTable(this);
			statusTable.parse(response.getData());
			getLogger().info("end getStatus");
		}
		return statusTable;
	}
	
	public TimeTable getTimeTable() throws IOException {
		getLogger().info("start getTime");
		CommandFactory commandFactory = getCommandFactory();
		Response response = 
			commandFactory.getReadTimeCommand().invoke();
		TimeTable timeTable = new TimeTable(this);
		timeTable.parse(response.getData());
		getLogger().info("end getTime");
		return timeTable;
	}
	
	protected void doDisConnect() throws IOException {
	}

	protected List doGetOptionalKeys() {
		return new ArrayList();
	}
	
	public RegisterFactory getRegisterFactory() throws IOException {
        if (registerFactory == null) {
            registerFactory = new RegisterFactory(this);
            registerFactory.init();
        }
        return registerFactory;
    }

	protected ProtocolConnection doInit(InputStream inputStream,
			OutputStream outputStream, int timeoutProperty,
			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
			int protocolCompatible, Encryptor encryptor,
			HalfDuplexController halfDuplexController) throws IOException {
		setCM10Connection(
    			new CM10Connection(
    					inputStream, 
    					outputStream, 
    					timeoutProperty, 
    					protocolRetriesProperty, 
    					forcedDelay, 
    					echoCancelling, 
    					halfDuplexController));
		getCM10Connection().setCM10(this);
        setCommandFactory(new CommandFactory(this));
    	setCM10Profile(new CM10Profile(this));
    	return this.getCM10Connection();
	}
	
	public CM10Profile getCM10Profile() {
        return cm10Profile;
    }

    public void setCM10Profile(CM10Profile cm10Profile) {
        this.cm10Profile = cm10Profile;
    }
    
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }  

	protected void doValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		// TODO Auto-generated method stub
		
	}

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProtocolVersion() {
		return "$Revision: 1.1 $";
	}

	public Date getTime() throws IOException {
		Date time = getTimeTable().getTime();
		getLogger().info("time: " + time);
		return time;
	}

	public void setTime() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		try {
	           int count=0;
	           System.out.println("start DialerTest");
	           Dialer dialer = new ATDialer();
	           dialer.init("COM1");
	           dialer.connect("000441908257417",60000);
	           dialer.getSerialCommunicationChannel().setParamsAndFlush(1200,
	                   SerialCommunicationChannel.DATABITS_8,
	                   SerialCommunicationChannel.PARITY_NONE,
	                   SerialCommunicationChannel.STOPBITS_1);
	           
	           try {
	               System.out.println("connected, start sending");
	               byte[] data = {0x65, 0x0B, 0x00, 0x00, 0x00, 0x21, 0x00, 0x00, 0x00, 0x00, 0x6F};
	               dialer.getOutputStream().write(data);
	               
	               
	               while(true) {
	                  if (dialer.getInputStream().available() != 0) {
	                      
	                      int kar = dialer.getInputStream().read();
	                      System.out.print((char)kar); 
	                      count=0;
	                  }
	                  else {
	                      Thread.sleep(100);
	                      if (count++ >= 100) {
	                          System.out.println();
	                          System.out.println("KV_DEBUG> connection timeout DialerTest");
	                          break;
	                      }
	                  }
	               }
	           }
	           catch(Exception e){
	               e.printStackTrace();   
	           }
	           dialer.disConnect();
	           System.out.println("end DialerTest");
	        }
	        catch(NestedIOException e) {
	            e.printStackTrace();  
	        }
	        catch(LinkException e) {
	            e.printStackTrace();  
	        }
	        catch(IOException e) {
	            e.printStackTrace();
	        }
	}
	
	public CM10Connection getCM10Connection() {
        return cm10Connection;
    }

    protected void setCM10Connection(CM10Connection cm10Connection) {
        this.cm10Connection = cm10Connection;
    }

}
