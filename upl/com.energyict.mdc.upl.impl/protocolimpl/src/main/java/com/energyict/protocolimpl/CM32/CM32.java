package com.energyict.protocolimpl.CM32;

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

public class CM32 extends AbstractProtocol {
	
	private CM32Connection cm32Connection = null;
	private CM32Profile cm32Profile = null;
    private CommandFactory commandFactory=null;
    private ObisCodeMapper obisCodeMapper = new ObisCodeMapper(this);
    private RegisterFactory registerFactory;

    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getCM32Profile().getProfileData(lastReading,includeEvents);
    }
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    } 
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 16;
    }
    
    protected void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((getInfoTypeSerialNumber() == null) || 
            ("".compareTo(getInfoTypeSerialNumber())==0)) return;
    }
    
	protected void doConnect() throws IOException {
		getLogger().info("doConnect");
		
		CommandFactory commandFactory = getCommandFactory();
		Response response = 
			commandFactory.getReadTimeCommand().invoke();
		TimeTable timeTable = new TimeTable(this);
		timeTable.parse(response.getData());
		Date time = timeTable.getTime();
		getLogger().info("time in doConnect: " + time);
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
		setCM32Connection(
    			new CM32Connection(
    					inputStream, 
    					outputStream, 
    					timeoutProperty, 
    					protocolRetriesProperty, 
    					forcedDelay, 
    					echoCancelling, 
    					halfDuplexController));
		getCM32Connection().setCM32(this);
        setCommandFactory(new CommandFactory(this));
    	setCM32Profile(new CM32Profile(this));
    	return this.getCM32Connection();
	}
	
	public CM32Profile getCM32Profile() {
        return cm32Profile;
    }

    public void setCM32Profile(CM32Profile cm32Profile) {
        this.cm32Profile = cm32Profile;
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
		return "$Date$";
	}

	public Date getTime() throws IOException {
		getLogger().info("getTime");
		CommandFactory commandFactory = getCommandFactory();
		Response response = 
			commandFactory.getReadTimeCommand().invoke();
		TimeTable timeTable = new TimeTable(this);
		timeTable.parse(response.getData());
		Date time = timeTable.getTime();
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
	
	public CM32Connection getCM32Connection() {
        return cm32Connection;
    }

    protected void setCM32Connection(CM32Connection cm32Connection) {
        this.cm32Connection = cm32Connection;
    }

}
