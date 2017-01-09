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

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;
import com.energyict.protocolimpl.modbus.core.connection.ModbusTCPConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 *
 * @author Koen
 *
 * Changes:
 * 02/01/2009|JME - Added call to doTheGetOptionalKeys() in method doGetOptionalKeys() to read the optional keys from the abstract method, implemented by the protocol who's is extending Modbus
 * 19/03/2009|JME - Added setter for InfoTypeResponseTimeout property.
 *
 */
public abstract class Modbus extends AbstractProtocol implements MessageProtocol {

    protected static final String PK_INTERFRAME_TIMEOUT = "InterframeTimeout";
    protected static final String PK_PHYSICAL_LAYER = "PhysicalLayer";
    protected static final String PK_RESPONSE_TIMEOUT = "ResponseTimeout";
    protected static final String PK_FIRST_TIME_DELAY = "FirstTimeDelay";
    public static final String PK_METER_FIRMWARE_VERSION = "MeterFirmwareVersion";

    public Modbus(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected abstract void doTheConnect() throws IOException;
    protected abstract void doTheDisConnect() throws IOException;
    protected abstract void initRegisterFactory();

    protected ModbusConnection modbusConnection;
    private AbstractRegisterFactory registerFactory=null;
    private int  interframeTimeout;

    private String networkId;
    private boolean virtualLoadProfile;
    private int responseTimeout;
    private int physicalLayer;
    private int firstTimeDelay;
    private String meterFirmwareVersion;
    private int connection;
    private int nodeAddress;

    private int registerOrderFixedPoint;
    private int registerOrderFloatingPoint;

    @Override
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

    @Override
    protected void doDisconnect() throws IOException {
        doTheDisConnect();
    }

    @Override
    public void setTime() throws IOException {
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        throw new UnsupportedException();
    }

    public BigDecimal getRegisterMultiplier(int address) throws IOException {
        throw new UnsupportedException();
    }

    protected void setInfoTypePhysicalLayer(int physicalLayer) {
    	this.physicalLayer=physicalLayer;
    }

    public void setConnectionMode(int connection) {
        this.connection = connection;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec(PK_INTERFRAME_TIMEOUT, false));
        propertySpecs.add(this.stringSpec("NetworkId", false));
        propertySpecs.add(this.integerSpec("VirtualLoadProfile", false));
        propertySpecs.add(this.integerSpec(PK_PHYSICAL_LAYER, false));
        propertySpecs.add(this.integerSpec(PK_RESPONSE_TIMEOUT, false));
        propertySpecs.add(this.integerSpec("RegisterOrderFixedPoint", false));
        propertySpecs.add(this.integerSpec("RegisterOrderFloatingPoint", false));
        propertySpecs.add(this.integerSpec(PK_FIRST_TIME_DELAY, false));
        propertySpecs.add(this.stringSpec(PK_METER_FIRMWARE_VERSION, false));
        propertySpecs.add(this.integerSpec("Connection", false));
        propertySpecs.add(this.integerSpec("NodeAddress", false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setForcedDelay(Integer.parseInt(properties.getTypedProperty(PROP_FORCED_DELAY, "10").trim()));
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getTypedProperty(PK_INTERFRAME_TIMEOUT, "15").trim()));
        setNetworkId(properties.getTypedProperty("NetworkId", ""));
        setVirtualLoadProfile(Integer.parseInt(properties.getTypedProperty("VirtualLoadProfile", "0").trim())==1);

        physicalLayer = Integer.parseInt(properties.getTypedProperty(PK_PHYSICAL_LAYER, "0").trim());
        responseTimeout = Integer.parseInt(properties.getTypedProperty(PK_RESPONSE_TIMEOUT, "200").trim());
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(PROP_TIMEOUT, "2000").trim()));
        setInfoTypeProtocolRetriesProperty(Integer.parseInt(properties.getTypedProperty(PROP_RETRIES, "2").trim()));

        setRegisterOrderFixedPoint(Integer.parseInt(properties.getTypedProperty("RegisterOrderFixedPoint", "1").trim()));
        setRegisterOrderFloatingPoint(Integer.parseInt(properties.getTypedProperty("RegisterOrderFloatingPoint", "1").trim()));
        firstTimeDelay = Integer.parseInt(properties.getTypedProperty(PK_FIRST_TIME_DELAY, "0").trim());
        meterFirmwareVersion = properties.getTypedProperty(PK_METER_FIRMWARE_VERSION, "");
        connection = Integer.parseInt(properties.getTypedProperty("Connection", "0").trim());
        nodeAddress = Integer.parseInt(properties.getTypedProperty("NodeAddress", "255").trim());    // Only used in Modbus TCP/IP mode
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        if (connection == 1) {  // 1: use Modbus TCP/IP Frame Format
            modbusConnection = new ModbusTCPConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInterframeTimeout(), responseTimeout, physicalLayer, nodeAddress);
        } else {                // use normal Modbus Frame Format
            modbusConnection = new ModbusConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInterframeTimeout(), responseTimeout, physicalLayer);
        }
        return modbusConnection;
    }

    @Override
    public Date getTime() throws IOException {
        return new Date(); // KV_TO_DO
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 0; // KV_TO_DO
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
    }

    @Override
    public String getRegister(String name) throws IOException {
        StringTokenizer strTok = new StringTokenizer(name,",");
        int functioncode=getTokVal(strTok.nextToken());
        int[] vals = new int[strTok.countTokens()];
        int i = 0;
        while (strTok.countTokens()>0) {
			vals[i++]=getTokVal(strTok.nextToken());
		}
        return getRegisterFactory().getFunctionCodeFactory().getRequest(functioncode, vals).toString();
    }

    private int getTokVal(String tok) {
        if (tok.contains("0x")) {
			return Integer.parseInt(tok.substring(2), 16);
		} else {
			return Integer.parseInt(tok);
		}
    }


    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            return new RegisterValue(obisCode,getRegisterFactory().findRegister(obisCode).quantityValue());
        } catch (ModbusException e) {
            getLogger().warning("Failed to read register " + obisCode.toString() + " - " + e.getMessage());
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
        // Note: ConnectionExceptions (due to timeout) are not catched, but will be thrown, so the session fails & retries
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(getRegisterFactory().findRegister(obisCode).getName());
    }

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        if (extendedLogging==0) {
            StringBuilder builder = new StringBuilder();
            for (Object o : getRegisterFactory().getRegisters()) {
                AbstractRegister ar = (AbstractRegister) o;
                if (ar.getObisCode() == null) {
                    builder.append(ar.getReg()).append("(").append(ar.getRange()).append("), ").append(ar.getName()).append("\n");
                } else {
                    builder.append(ar.getObisCode()).append(", ").append(ar.getReg()).append("(").append(ar.getRange()).append("), ").append(ar.getName()).append("\n");
                }
            }
            return builder.toString();
        }
        else if (extendedLogging==1) {
            StringBuilder builder = new StringBuilder();
            for (Object o : getRegisterFactory().getRegisters()) {
                AbstractRegister ar = (AbstractRegister) o;
                if (ar.getObisCode() != null) {
                    builder.append(readRegister(ar.getObisCode())).append("\n");
                }
            }
            return builder.toString();
        } else {
			return "";
		}
    }

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

    @Override
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

    @Override
   public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		try {
            boolean writeMultipleRegisters = messageEntry.getContent().contains("<WriteMultipleRegisters");
            boolean writeMultipleCoils = messageEntry.getContent().contains("<WriteMultipleCoils");
            boolean writeSingleRegister = messageEntry.getContent().contains("<WriteSingleRegisters");
            boolean writeSingleCoil = messageEntry.getContent().contains("<WriteSingleCoil");

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
							values[i-1]=Integer.parseInt(contentEntries[i], 16);
						}
					}

                    if (writeMultipleRegisters) {
                        getRegisterFactory().getFunctionCodeFactory().getWriteMultipleRegisters(address, values.length, convertToBytesArray(values));
                    } else if (writeMultipleCoils) {
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
						value = Integer.parseInt(contentEntries[1], 16);
					}

                    if (writeSingleRegister) {
                        getRegisterFactory().getFunctionCodeFactory().getWriteSingleRegister(address, value);
                    } else if (writeSingleCoil) {
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

    @Override
   public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();
        // General Parameters
        MessageCategorySpec cat = new MessageCategorySpec("Modbus general messages");
        cat.addMessageSpec(addBasicMsg("Write multiple registers", "WriteMultipleRegisters", false));
        cat.addMessageSpec(addBasicMsg("Write single register", "WriteSingleRegisters", false));
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

    @Override
   public String writeMessage(Message msg) {
       return msg.write(this);
   }

    @Override
   public String writeTag(MessageTag msgTag) {
       StringBuilder builder = new StringBuilder();

       // a. Opening tag
       builder.append("<");
       builder.append( msgTag.getName() );

       // b. Attributes
       for (Iterator<MessageAttribute> it = msgTag.getAttributes().iterator(); it.hasNext();) {
           MessageAttribute att = it.next();
           if (att.getValue()==null || att.getValue().isEmpty()) {
			continue;
		}
           builder.append(" ").append(att.getSpec().getName());
           builder.append("=").append('"').append(att.getValue()).append('"');
       }
       builder.append(">");

       // c. sub elements
       for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
           MessageElement elt = (MessageElement)it.next();
           if (elt.isTag()) {
			builder.append( writeTag((MessageTag)elt) );
		} else if (elt.isValue()) {
               String value = writeValue((MessageValue)elt);
               if (value==null || value.isEmpty()) {
				return "";
			}
               builder.append(value);
           }
       }

       // d. Closing tag
       builder.append("</");
       builder.append( msgTag.getName() );
       builder.append(">");

       return builder.toString();
   }

    @Override
   public String writeValue(MessageValue value) {
       return value.getValue();
   }

    @Override
    public Logger getLogger() {
        return super.getLogger();
    }

}