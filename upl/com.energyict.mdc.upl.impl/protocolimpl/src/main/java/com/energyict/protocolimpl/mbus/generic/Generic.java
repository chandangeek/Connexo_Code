/*
 * Generic.java
 *
 * Created on 2 oktober 2007, 10:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.generic;

import com.energyict.cbo.SerialCommunicationSettings;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.mbus.core.CIField72h;
import com.energyict.protocolimpl.mbus.core.MBus;
import com.energyict.protocolimpl.mbus.core.discover.DiscoverProtocolInfo;
import com.energyict.protocolimplv2.MdcManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 *
 * @author kvds
 */
public class Generic extends MBus {
    
    
    final int DEBUG=0;
    
    RegisterFactory registerFactory=null;
    
    // temporary
    // KV_TO_DO The Discover interface in core code does not return a list of secondary addresses. therefor
    // we cache this in the Generic protocol instantiation. and return the elements until iteration is complete... 
    List<DiscoverResult> discoverResults=null;
    int discoverResultIndex=0;
    
    /**
     * Creates a new instance of Generic 
     */
    public Generic() {
    }
    
    
//    public Date getTime() throws IOException {
//        return getRegisterFactory().getTime();
//    }
    
    public DiscoverResult discover(DiscoverTools discoverTools) {
        if (DEBUG>=1) System.out.println("Generic, discover("+discoverTools+")");
        
        if (Integer.parseInt(discoverTools.getProperties().getProperty("SecondaryAddressing","0")) == 1)
        	return discoverSecondaryAddresses(discoverTools);
        else
        	return discoverPrimaryAddresses(discoverTools);
    }
        	
    
    public DiscoverResult discoverSecondaryAddresses(DiscoverTools discoverTools) {
    	
    	if (discoverResults==null) {
    		discoverResults = new ArrayList();
	        try {
	            setProperties(discoverTools.getProperties());
	            init(discoverTools.getDialer().getInputStream(),discoverTools.getDialer().getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
	            
	            getMBusConnection().setRTUAddress(253);
	            getMBusConnection().setTimeout(3000);
	            getMBusConnection().setRetries(2);
	            
	            try {
	            	System.out.println("Send SND_NKE to address 255...");
		        	getMBusConnection().sendSND_NKE(255);
		        	try {
		        		Thread.sleep(1000);
		        	}
		        	catch(InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
                    }
		        }
		        catch(IOException e) {
		        	// absorb
		        }
	            try {
		        	System.out.println("Send SND_NKE to address 253...");
		        	getMBusConnection().sendSND_NKE(253);
		        	try {
		        		Thread.sleep(1000);
		        	}
		        	catch(InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
                    }
		        }
		        catch(IOException e) {
		        	// absorb
		        }
		        
		        getMBusConnection().flushInputStream();
	            
	            Iterator<CIField72h> it = discoverDeviceSerialNumbers().iterator();
	            while(it.hasNext()) {
	            	CIField72h cIField72h = it.next();
	            	
	    	        DiscoverResult discoverResult = new DiscoverResult();
	    	        discoverResult.setProtocolMBUS();
	                discoverResult.setDiscovered(true);
	                discoverResult.setAddress(discoverTools.getAddress());
	                discoverResult.setResult(cIField72h.getMeter3LetterId()+", "+cIField72h.getVersion());
	                discoverResult.setProtocolName(DiscoverProtocolInfo.getUnknown().getProtocolName());
	                discoverResult.setDeviceTypeName(DiscoverProtocolInfo.getUnknown().getDeviceType());
	                discoverResult.setShortDeviceTypeName(DiscoverProtocolInfo.getUnknown().getShortDeviceType());
	                String str = cIField72h.getMeter3LetterId()+" V"+cIField72h.getVersion()+" "+cIField72h.getDeviceType().getShortDescription();
	                discoverResult.setDeviceName(str.replace('.','-').replace('/',' ')); // '.' and '/' are not allowed in EIServer as character in a device name!
	                
	                
	                Iterator it2 = DiscoverProtocolInfo.getSupportedDevicesList().iterator();
	                while(it2.hasNext()) {
	                    DiscoverProtocolInfo dpi = (DiscoverProtocolInfo)it2.next();
	                    if ((dpi.getVersion() == cIField72h.getVersion()) && 
	                        (dpi.getManufacturer().compareTo(cIField72h.getMeter3LetterId())==0) &&
	                        (dpi.getMedium() == cIField72h.getDeviceType().getId())) {
	                        discoverResult.setProtocolName(dpi.getProtocolName());
	                        discoverResult.setDeviceTypeName(dpi.getDeviceType());
	                        
	                        break;
	                    }
	                }

                    // temporary
                    // KV_TO_DO this is a tricky way to exposure all fields of the mbus header in case of secondary addressing.
                    // We do this to not to change the core code DiscoverResult. Better should be to add a Properties object into
                    // DiscoverResult for all specific properties. Also the fact serialnumber has 4 '_' separated fields means we
                    // did a secondary address discover...
                    discoverResult.setSerialNumber(""+cIField72h.getIdentificationNumber()+
                    		                       "_"+CIField72h.getManufacturer3Letter(cIField72h.getManufacturerIdentification())+
                    		                 	   "_"+Integer.toHexString(cIField72h.getVersion())+
                    		                 	   "_"+Integer.toHexString(cIField72h.getDeviceType().getId()));	   
	                
	                discoverResults.add(discoverResult);
	            	
	            } // while(it.hasNext()) {
	        }
	        catch(Exception e) {
	            return null;
	        }
	        finally {
	        	if (discoverResults.size() > discoverResultIndex)
	        		return discoverResults.get(discoverResultIndex++);
	        	else
	        		return null;
	        }
        }
        else {
        	if (discoverResults.size() > discoverResultIndex)
        		return discoverResults.get(discoverResultIndex++);
        	else
        		return null;
        }
    } // public DiscoverResult discoverSecondaryAddresses(DiscoverTools discoverTools)
        
    public DiscoverResult discoverPrimaryAddresses(DiscoverTools discoverTools) {
        
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMBUS();
        try {
            setProperties(discoverTools.getProperties());
            init(discoverTools.getDialer().getInputStream(),discoverTools.getDialer().getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            connect();
            
            getMBusConnection().setTimeout(3000);
            getMBusConnection().setRetries(2);
            
            CIField72h cIField72h = getCIField72h();
            
            discoverResult.setDiscovered(true);
            discoverResult.setAddress(discoverTools.getAddress());
            discoverResult.setResult(cIField72h.getMeter3LetterId()+", "+cIField72h.getVersion());
            discoverResult.setProtocolName(DiscoverProtocolInfo.getUnknown().getProtocolName());
            discoverResult.setDeviceTypeName(DiscoverProtocolInfo.getUnknown().getDeviceType());
            discoverResult.setShortDeviceTypeName(DiscoverProtocolInfo.getUnknown().getShortDeviceType());
            String str = cIField72h.getMeter3LetterId()+" V"+cIField72h.getVersion()+" "+cIField72h.getDeviceType().getShortDescription();
            discoverResult.setDeviceName(str.replace('.','-').replace('/',' ')); // '.' and '/' are not allowed in EIServer as character in a device name!
            
            
            Iterator it = DiscoverProtocolInfo.getSupportedDevicesList().iterator();
            while(it.hasNext()) {
                DiscoverProtocolInfo dpi = (DiscoverProtocolInfo)it.next();
                if ((dpi.getVersion() == cIField72h.getVersion()) && 
                    (dpi.getManufacturer().compareTo(cIField72h.getMeter3LetterId())==0) &&
                    (dpi.getMedium() == cIField72h.getDeviceType().getId())) {
                    discoverResult.setProtocolName(dpi.getProtocolName());
                    discoverResult.setDeviceTypeName(dpi.getDeviceType());
                    discoverResult.setSerialNumber(""+cIField72h.getIdentificationNumber());
                    break;
                }
            }
            
            //System.out.println(cIField72h.header());
            
            return discoverResult;
        }
        catch(Exception e) {
            discoverResult.setDiscovered(false);
            discoverResult.setResult(e.toString());
            return discoverResult;
        }
        finally {
           try { 
              disconnect();
           }
           catch(IOException e) {
               // absorb
           }
        }
    }
    
    protected void doTheConnect() throws IOException {
    	if (getInfoTypeSecondaryAddressing() == 0) {
             getMBusConnection().sendSND_NKE(); 
    	}
    	else {
    		// getMBusConnection().sendSND_NKE(); I think it IS allowed to send the SND_NKE here to clear the current selection
    		// however, a wrong secondary addressing also clears the selection bit in the device...
    		try {
    			getMBusConnection().selectSecondaryAddress(Long.parseLong(getInfoTypeSerialNumber(),16),getInfoTypeHeaderManufacturerCode(),getInfoTypeHeaderVersion(),getInfoTypeHeaderMedium(),false);
    		}
    		catch(NumberFormatException e) {
    			throw new IOException("Generic, secondary addressing used, configure a valid serial number! SerialNumber "+getInfoTypeSerialNumber()+" is invalid!");
    		}
    	}
    }
    protected void doTheDisConnect() throws IOException {
    	if (getInfoTypeSecondaryAddressing() == 1) {
    		try {
    		   Thread.sleep(500);	
    		   getMBusConnection().sendSND_NKE();
    		}
    	    catch(IOException e) {
    	    	// absorb
    	    }
    	    catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
            }
    	}
        
    }
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
    	if (getInfoTypeSecondaryAddressing() == 1) {
    		setInfoTypeDeviceID("253");
    	}
    }
    protected List doTheGetOptionalKeys() {
        List list = new ArrayList();
        return list;
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "NOT YET IMPLEMENTED";
    }

    /**
     * The protocol version date
     */
    public String getProtocolVersion() {
        return "$Date$";
    }    
    
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }
    
    private Properties getProperties(int address, int secondaryAddressing) {
        Properties properties = new Properties();
        properties.setProperty(MeterProtocol.ADDRESS,""+address);
        properties.setProperty("Retries", "1");
        properties.setProperty("Timeout", "250");
        properties.setProperty("SecondaryAddressing", ""+secondaryAddressing);
        return properties;        
    }    
    
    static public void main1(String[] args) {
        try {
            // ********************** Dialer **********************
            Dialer dialer = DialerFactory.getDirectDialer().newDialer();
            dialer.init("COM1"); // "/dev/ttyXR0";
            dialer.getSerialCommunicationChannel().setParams(2400,
                                                             SerialCommunicationChannel.DATABITS_8,
                                                             SerialCommunicationChannel.PARITY_EVEN,
                                                             SerialCommunicationChannel.STOPBITS_1);
            dialer.connect();
            
            // ********************** Properties **********************
            Properties properties = new Properties();
            properties.setProperty("ProfileInterval", "60");
            properties.setProperty("SecondaryAddressing", "1");
            properties.setProperty(MeterProtocol.ADDRESS,"253");
            
            
            properties.setProperty("SerialNumber","1234FFFF");
            properties.setProperty("HeaderManufacturerCode","FFFF");
            properties.setProperty("HeaderVersion","FF");
            properties.setProperty("HeaderMedium","FF");
            
            properties.setProperty("Retries","2");
            //properties.setProperty("SerialNumber","08072197"); //65553712");
            //properties.setProperty("HalfDuplex", "-1");
            // ********************** EictRtuModbus **********************
            Generic generic = new Generic();
            
            generic.setProperties(properties); 
            generic.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            generic.connect();
            System.out.println(generic.getRegistersInfo(0));
//            try {
//            	generic.getMBusConnection().sendSND_NKE(253);
//            }
//            catch(IOException e) {
//            	// absorb
//            }
//            try {
//            	generic.getMBusConnection().sendSND_NKE(255);
//            }
//            catch(IOException e) {
//            	// absorb
//            }
//            //generic.getMBusConnection().selectSecondaryAddress(65553712); //,0x0FF0FFFF);
//            //generic.getMBusConnection().selectSecondaryAddress("12345678_4d25_1_4");
//            //generic.getMBusConnection().selectSecondaryAddress("12345678_19749_1_4");
//            //System.out.println(generic.getMBusConnection().sendREQ_UD2());
//            
//            Iterator<CIField72h> it = generic.discoverDeviceSerialNumbers().iterator();
//            
//            while(it.hasNext()) {
//            	System.out.println(it.next().header());
//            }
            
            System.out.println("************************************************************************************************");

            
             
            
            
            dialer.disConnect();
            generic.disconnect();
                    
                    
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }    
    
    static public void main2(String[] args) {
        
        try {
            // ********************** EictRtuModbus **********************
            DiscoverTools discoverTools = null;
            for (int address = 0;address<5;address++) {
                System.out.println("---------------------> discover address "+address);
                try {
                    Generic generic = new Generic();
                    

                    SerialCommunicationSettings settings = new SerialCommunicationSettings(2400,8, 'E', 1);

                    if ((args==null) || (args.length<=1))
                        //discoverTools = new DiscoverTools("COM1",settings);
                    	discoverTools = new DiscoverTools("/dev/ttyXR2",settings);
                    else
                        discoverTools = new DiscoverTools(args[1],settings); //"/dev/ttyXR0";
                    
                    discoverTools.setProperties(generic.getProperties(address,0));
                    discoverTools.setAddress(address);
                    discoverTools.init();
                    discoverTools.connect();

                    DiscoverResult o = generic.discover(discoverTools);
                    if (o.isDiscovered()) System.out.println(o);
                    
                }
                catch(Exception e) {
                    System.out.println(e.toString());
                }
                finally {
                    try {
                        discoverTools.disconnect();
                    }
                    catch(Exception e) {
                       System.out.println(e.toString());
                    }
                }
            }
            
        }
        catch(Exception e) {
            //e.printStackTrace();
        }
    }
    
    static public void main(String[] args) {
        
        try {
            // ********************** EictRtuModbus **********************
            DiscoverTools discoverTools = null;
            System.out.println("---------------------> discover secondary addresses");
            try {
                Generic generic = new Generic();
                

                SerialCommunicationSettings settings = new SerialCommunicationSettings(2400,8, 'E', 1);

                if ((args==null) || (args.length<=1))
                    discoverTools = new DiscoverTools("COM1",settings);
                else
                    discoverTools = new DiscoverTools(args[1],settings); //"/dev/ttyXR0";
                
                discoverTools.setProperties(generic.getProperties(253,1));
                discoverTools.setAddress(253);
                discoverTools.init();
                discoverTools.connect();

                DiscoverResult o=null;
                do {
	                o = generic.discover(discoverTools);
	                if ((o!= null) &&(o.isDiscovered()))
	                	System.out.println(o);
                } while(o != null);
                
            }
            catch(Exception e) {
                System.out.println(e.toString());
            }
            finally {
                try {
                    discoverTools.disconnect();
                }
                catch(Exception e) {
                   System.out.println(e.toString());
                }
            }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }
    }    
    
}
