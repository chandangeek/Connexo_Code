/*
 * Modbus.java
 *
 * Created on 20 september 2005, 9:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.Discover;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;
import com.energyict.protocolimpl.modbus.core.connection.ModbusTCPConnection;
import com.energyict.protocolimplv2.MdcManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 *
 * @author Koen
 * 
 * Changes:
 * 02/01/2009|JME - Added call to doTheGetOptionalKeys() in method doGetOptionalKeys() to read the optional keys from the abstract method, implemented by the protocol who's is extending Modbus
 * 19/03/2009|JME - Added setter for InfoTypeResponseTimeout property.
 * 
 */
abstract public class Modbus extends AbstractProtocol implements Discover,MessageProtocol {
    
    abstract protected void doTheConnect() throws IOException;
    abstract protected void doTheDisConnect() throws IOException;
    abstract protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException;
    abstract protected List doTheGetOptionalKeys();
    abstract protected void initRegisterFactory();
    
    protected ModbusConnection modbusConnection;
    private AbstractRegisterFactory registerFactory=null;
    private int  interframeTimeout;
            
    private String networkId;
    private boolean virtualLoadProfile;
    int responseTimeout;
    int physicalLayer;
    int firstTimeDelay;
    String meterFirmwareVersion;
    int connection;
    int nodeAddress;

    private int registerOrderFixedPoint;
    private int registerOrderFloatingPoint;
    
    /** Creates a new instance of Modbus */
    public Modbus() {
    }
    
    protected void doConnect() throws IOException {
    	
    	try {
    		Thread.sleep(firstTimeDelay);
    	}
    	catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    	
        doTheConnect();
    }
    
    protected void doDisConnect() throws IOException {
        doTheDisConnect();
    }
    
    
    public void setTime() throws IOException {
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }
    
    public BigDecimal getRegisterMultiplier(int address) throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    
    protected void setInfoTypePhysicalLayer(int physicalLayer) {
    	this.physicalLayer=physicalLayer;
    }
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","10").trim()));
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","15").trim()));
        setNetworkId(properties.getProperty("NetworkId",""));
        setVirtualLoadProfile(Integer.parseInt(properties.getProperty("VirtualLoadProfile","0").trim())==1);
        
        physicalLayer = Integer.parseInt(properties.getProperty("PhysicalLayer","0").trim());
        responseTimeout = Integer.parseInt(properties.getProperty("ResponseTimeout","200").trim());
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","2000").trim()));
        setInfoTypeProtocolRetriesProperty(Integer.parseInt(properties.getProperty("Retries","2").trim()));
        
        setRegisterOrderFixedPoint(Integer.parseInt(properties.getProperty("RegisterOrderFixedPoint","1").trim()));
        setRegisterOrderFloatingPoint(Integer.parseInt(properties.getProperty("RegisterOrderFloatingPoint","1").trim()));
        firstTimeDelay = Integer.parseInt(properties.getProperty("FirstTimeDelay", "0").trim());
        meterFirmwareVersion = properties.getProperty("MeterFirmwareVersion", "");
        connection = Integer.parseInt(properties.getProperty("Connection", "0").trim());
        nodeAddress = Integer.parseInt(properties.getProperty("NodeAddress", "255").trim());    // Only used in Modbus TCP/IP mode
        doTheValidateProperties(properties);
    }
    
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("VirtualLoadProfile");
        result.add("InterframeTimeout");
        result.add("ResponseTimeout");
        result.add("PhysicalLayer");
        result.add("RegisterOrderFixedPoint");
        result.add("RegisterOrderFloatingPoint");

        List optionalKeys = doTheGetOptionalKeys();
        if (optionalKeys != null) {
			result.addAll(optionalKeys);
		}
        
        return result;
    }
    
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        if (connection == 1) {  // 1: use Modbus TCP/IP Frame Format
            modbusConnection = new ModbusTCPConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInterframeTimeout(), responseTimeout, physicalLayer, nodeAddress);
        } else {                // use normal Modbus Frame Format
        modbusConnection = new ModbusConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInterframeTimeout(), responseTimeout, physicalLayer);
        }
        return modbusConnection;
    }
    
    public Date getTime() throws IOException {
        return new Date(); // KV_TO_DO
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 0; // KV_TO_DO
    }
    
    /*
     * Override this method if the subclass wants to set a specific register 
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        
    }
    
    /*
     * Override this method if the subclass wants to get a specific register 
     */
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        
        StringTokenizer strTok = new StringTokenizer(name,",");
        int functioncode=getTokVal(strTok.nextToken());
        int[] vals = new int[strTok.countTokens()];
        int i=0;
        while(strTok.countTokens()>0) {
			vals[i++]=getTokVal(strTok.nextToken());
		}
        return getRegisterFactory().getFunctionCodeFactory().getRequest(functioncode, vals).toString();
    }
    
    private int getTokVal(String tok) {
        if (tok.indexOf("0x")>=0) {
			return Integer.parseInt(tok.substring(2),16);
		} else {
			return Integer.parseInt(tok);
		} 
    }
    
    
    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e 
     *******************************************************************************************/
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            return new RegisterValue(obisCode,getRegisterFactory().findRegister(obisCode).quantityValue());
        } catch (ModbusException e) {
            getLogger().warning("Failed to read register " + obisCode.toString() + " - " + e.getMessage());
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
        // Note: ConnectionExceptions (due to timeout) are not catched, but will be thrown, so the session fails & retries
    }
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        
        //return new RegisterInfo(obisCode.getDescription());
        
        return new RegisterInfo(getRegisterFactory().findRegister(obisCode).getName());
    }    
    
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        if (extendedLogging==0) {
            StringBuffer strBuff = new StringBuffer();
            Iterator it = getRegisterFactory().getRegisters().iterator();
            while (it.hasNext()) {
                AbstractRegister ar = (AbstractRegister)it.next();
//System.out.println("KV_DEBUG> "+ar.getObisCode());                
                if (ar.getObisCode()==null) {
					strBuff.append(ar.getReg()+"("+ar.getRange()+"), "+ar.getName()+"\n");
				} else {
					strBuff.append(ar.getObisCode()+", "+ar.getReg()+"("+ar.getRange()+"), "+ar.getName()+"\n");
				}
            }
            return strBuff.toString();
        }
        else if (extendedLogging==1) {
            StringBuffer strBuff = new StringBuffer();
            Iterator it = getRegisterFactory().getRegisters().iterator();
            while (it.hasNext()) {
                AbstractRegister ar = (AbstractRegister)it.next();
                //System.out.println(ar.getObisCode());
                if (ar.getObisCode()!=null) {
					strBuff.append(readRegister(ar.getObisCode())+"\n");
				}
            }
            return strBuff.toString();
        } else {
			return "";
		}
    }
    
    
    /****************************************************************************************************************
     * Implementing Modbus interface
     ****************************************************************************************************************/    
    public ModbusConnection getModbusConnection() {
        return modbusConnection;
    }
    public TimeZone gettimeZone() {
        return super.getTimeZone();
    }

    public AbstractRegisterFactory getRegisterFactory() {
        if (registerFactory==null) {
			initRegisterFactory();
		}
        return registerFactory;
    }

    public void setRegisterFactory(AbstractRegisterFactory registerFactory) {
        this.registerFactory = registerFactory;
    }

    public int getInterframeTimeout() {
        return interframeTimeout;
    }

    public void setInfoTypeInterframeTimeout(int interframeTimeout) {
        this.interframeTimeout = interframeTimeout;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public boolean isVirtualLoadProfile() {
        return virtualLoadProfile;
    }

    public void setVirtualLoadProfile(boolean virtualLoadProfile) {
        this.virtualLoadProfile = virtualLoadProfile;
    }
    public int getRegisterOrderFixedPoint() {
        return registerOrderFixedPoint;
    }

    private void setRegisterOrderFixedPoint(int registerOrderFixedPoint) {
        this.registerOrderFixedPoint = registerOrderFixedPoint;
    }

    public int getRegisterOrderFloatingPoint() {
        return registerOrderFloatingPoint;
    }

    private void setRegisterOrderFloatingPoint(int registerOrderFloatingPoint) {
        this.registerOrderFloatingPoint = registerOrderFloatingPoint;
    }
    protected int getInfoTypeFirstTimeDelay() { 
		return firstTimeDelay;
	}
	protected void setInfoTypeFirstTimeDelay(int firstTimeDelay) {
		this.firstTimeDelay = firstTimeDelay;
	}
	protected String getInfoTypeMeterFirmwareVersion() {
		return meterFirmwareVersion;
	}
	protected void setInfoTypeMeterFirmwareVersion(String meterFirmwareVersion) {
		this.meterFirmwareVersion = meterFirmwareVersion;
	}
    protected void setInfoTypeResponseTimeout(int responseTimeout) {
    	this.responseTimeout = responseTimeout;
    }
	
    /*******************************************************************************************
    M e s s a g e P r o t o c o l  i n t e r f a c e 
    *******************************************************************************************/
   // message protocol
   public void applyMessages(List messageEntries) throws IOException {
   }
   
   public String stripOffTag(String content) {
	   return content.substring(content.indexOf(">")+1,content.lastIndexOf("<"));
   }
   public byte[] convertToBytesArray(int[] values) {
	   byte[] byteArray = new byte[values.length*2];
	   for (int i=0;i<values.length;i++) {
		   byteArray[i*2] = (byte)(values[i]>>8); 
		   byteArray[i*2+1] = (byte)values[i]; 	   
	   }
	   return byteArray;
   }
   
   public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		try {

            boolean writeMultipleRegisters = messageEntry.getContent().indexOf("<WriteMultipleRegisters")>=0;
            boolean writeMultipleCoils = messageEntry.getContent().indexOf("<WriteMultipleCoils")>=0;
            boolean writeSingleRegister = messageEntry.getContent().indexOf("<WriteSingleRegisters")>=0;
            boolean writeSingleCoil = messageEntry.getContent().indexOf("<WriteSingleCoil")>=0;

			if (writeMultipleRegisters || writeMultipleCoils) {
                String operation = writeMultipleRegisters ? "WriteMultipleRegisters" : "WriteMultipleCoils";
                getLogger().info("************************* "+operation+" *************************");
				// e.g. HEX,0e00,2f4,D6D8
				// e.g. DEC,3584,756,55000
				// e.g. 0e00,2f4,D6D8
				String content = stripOffTag(messageEntry.getContent());
				String[] contentEntries = content.split(",");
				if (contentEntries.length<2) {
					getLogger().severe("Error parsing "+operation+" message, content "+content+". Usage: [HEX or DEC,]address,val1,val2,...,valN");
					return MessageResult.createFailed(messageEntry);
				}
				else {
					int address;
					int[] values;
					if (contentEntries[0].compareTo("HEX")==0) {
						if (contentEntries.length<3) {
							getLogger().severe("Error parsing "+operation+" message, content "+content+". Usage: [HEX or DEC,]address,val1,val2,...,valN");
							return MessageResult.createFailed(messageEntry);
						}
						address = Integer.parseInt(contentEntries[1], 16);
						values = new int[contentEntries.length-2];
						for (int i=2;i<contentEntries.length;i++) {
							values[i-2]=Integer.parseInt(contentEntries[i],16);
						}
					}
					else if (contentEntries[0].compareTo("DEC")==0) {
						if (contentEntries.length<3) {
							getLogger().severe("Error parsing "+operation+" message, content "+content+". Usage: [HEX or DEC,]address,val1,val2,...,valN");
							return MessageResult.createFailed(messageEntry);
						}
						address = Integer.parseInt(contentEntries[1]);
						values = new int[contentEntries.length-2];
						for (int i=2;i<contentEntries.length;i++) {
							values[i-2]=Integer.parseInt(contentEntries[i]);
						}
					}
					else {
						address = Integer.parseInt(contentEntries[0], 16);
						values = new int[contentEntries.length-1];
						for (int i=1;i<contentEntries.length;i++) {
							values[i-1]=Integer.parseInt(contentEntries[i],16);
						}
					}

                    if(writeMultipleRegisters){
                        getRegisterFactory().getFunctionCodeFactory().getWriteMultipleRegisters(address, values.length, convertToBytesArray(values));
                    } else if(writeMultipleCoils){
                        int[] converted_values = new int[values.length];
                        for (int i = 0; i < values.length; i++) {
                            converted_values[i] = values[i] == 1 ? 65280 : 0;
                        }

                        getRegisterFactory().getFunctionCodeFactory().getWriteMultipleCoils(address, converted_values.length, convertToBytesArray(converted_values));
                    }
	                return MessageResult.createSuccess(messageEntry);
				}
				
			}
			else if (writeSingleRegister || writeSingleCoil) {
                String operation = writeSingleRegister ? "WriteSingleRegister" : "WriteSingleCoil";
				getLogger().info("************************* "+operation+" *************************");
				// e.g. HEX,0e00,2f4
				// e.g. DEC,3584,756
				// e.g. 0e00,2f4
				String content = stripOffTag(messageEntry.getContent());
				String[] contentEntries = content.split(",");
				if (contentEntries.length<2) {
					getLogger().severe("Error parsing "+operation+" message, content "+content+". Usage: [HEX or DEC,]address,value");
					return MessageResult.createFailed(messageEntry);
				}
				else {
					int address;
					int value;
					if (contentEntries[0].compareTo("HEX")==0) {
						if (contentEntries.length<3) {
							getLogger().severe("Error parsing "+operation+" message, content "+content+". Usage: [HEX or DEC,]address,value");
							return MessageResult.createFailed(messageEntry);
						}
						address = Integer.parseInt(contentEntries[1], 16);
						value = Integer.parseInt(contentEntries[2], 16);
					}
					else if (contentEntries[0].compareTo("DEC")==0) {
						if (contentEntries.length<3) {
							getLogger().severe("Error parsing "+operation+" message, content "+content+". Usage: [HEX or DEC,]address,value");
							return MessageResult.createFailed(messageEntry);
						}
						address = Integer.parseInt(contentEntries[1]);
						value = Integer.parseInt(contentEntries[2]);
					}
					else {
						address = Integer.parseInt(contentEntries[0], 16);
						value = Integer.parseInt(contentEntries[1],16);
					}

                    if(writeSingleRegister){
                        getRegisterFactory().getFunctionCodeFactory().getWriteSingleRegister(address, value);
                    } else if(writeSingleCoil){
                        getRegisterFactory().getFunctionCodeFactory().getWriteSingleCoil(address, value == 1 ? 65280 : 0);
                    }
	                return MessageResult.createSuccess(messageEntry);
				}
			} else {
				return doQueryMessage(messageEntry);
			}

        } catch (ModbusException e) {
            getLogger().severe("Message will fail, "+e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
       // Note: ConnectionExceptions (due to timeout) will be thrown, so the session fails & retries the message
   }
   
   public List getMessageCategories() {
       List theCategories = new ArrayList();
       // General Parameters
       MessageCategorySpec cat = new MessageCategorySpec("Modbus general messages");
       MessageSpec msgSpec = addBasicMsg("Write multiple registers", "WriteMultipleRegisters", false);
       cat.addMessageSpec(msgSpec);
       msgSpec = addBasicMsg("Write single register", "WriteSingleRegisters", false);
       cat.addMessageSpec(msgSpec);
       theCategories.add(cat);
       return doGetMessageCategories(theCategories);
   }
   
   protected MessageResult doQueryMessage(MessageEntry messageEntry) throws IOException {
	   return MessageResult.createSuccess(messageEntry);
   }
   protected List doGetMessageCategories(List theCategories) {
	   return theCategories;
   }
   
   protected MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
       MessageSpec msgSpec = new MessageSpec(keyId, advanced);
       MessageTagSpec tagSpec = new MessageTagSpec(tagName);
       tagSpec.add(new MessageValueSpec());
       msgSpec.add(tagSpec);
       return msgSpec;
   }
   
   public String writeMessage(Message msg) {
       return msg.write(this);
   }
   public String writeTag(MessageTag msgTag) {
       StringBuffer buf = new StringBuffer();
       
       // a. Opening tag
       buf.append("<");
       buf.append( msgTag.getName() );
       
       // b. Attributes
       for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
           MessageAttribute att = (MessageAttribute)it.next();
           if (att.getValue()==null || att.getValue().length()==0) {
			continue;
		}
           buf.append(" ").append(att.getSpec().getName());
           buf.append("=").append('"').append(att.getValue()).append('"');
       }
       buf.append(">");
       
       // c. sub elements
       for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
           MessageElement elt = (MessageElement)it.next();
           if (elt.isTag()) {
			buf.append( writeTag((MessageTag)elt) );
		} else if (elt.isValue()) {
               String value = writeValue((MessageValue)elt);
               if (value==null || value.length()==0) {
				return "";
			}
               buf.append(value);
           }
       }
       
       // d. Closing tag
       buf.append("</");
       buf.append( msgTag.getName() );
       buf.append(">");
       
       return buf.toString();    
   }
   
   public String writeValue(MessageValue value) {
       return value.getValue();
   }	
	
}
