/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;

import com.energyict.protocolimpl.mbus.core.CIField72h;
import com.energyict.protocolimpl.mbus.core.MBus;
import com.energyict.protocolimpl.mbus.core.discover.DiscoverProtocolInfo;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    @Override
    public String getProtocolDescription() {
        return "Generic Mbus";
    }

    final int DEBUG=0;

    // temporary
    // KV_TO_DO The Discover interface in core code does not return a list of secondary addresses. therefor
    // we cache this in the Generic protocol instantiation. and return the elements until iteration is complete...
    List<DiscoverResult> discoverResults=null;
    int discoverResultIndex=0;

    @Inject
    public Generic(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        if (DEBUG>=1) {
            System.out.println("Generic, discover(" + discoverTools + ")");
        }

        if (Integer.parseInt(discoverTools.getProperties().getProperty("SecondaryAddressing","0")) == 1) {
            return discoverSecondaryAddresses(discoverTools);
        }
        else {
            return discoverPrimaryAddresses(discoverTools);
        }
    }


    public DiscoverResult discoverSecondaryAddresses(DiscoverTools discoverTools) {

    	if (discoverResults==null) {
    		discoverResults = new ArrayList<>();
	        try {
	            setProperties(discoverTools.getProperties());
	            init(null, null, TimeZone.getTimeZone("ECT"), Logger.getLogger("name"));

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
		        		// absorb
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
		        		// absorb
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
	        	if (discoverResults.size() > discoverResultIndex) {
                    return discoverResults.get(discoverResultIndex++);
                }
                else {
                    return null;
                }
	        }
        }
        else {
        	if (discoverResults.size() > discoverResultIndex) {
                return discoverResults.get(discoverResultIndex++);
            }
            else {
                return null;
            }
        }
    }

    public DiscoverResult discoverPrimaryAddresses(DiscoverTools discoverTools) {

        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMBUS();
        try {
            setProperties(discoverTools.getProperties());
            init(null, null, TimeZone.getTimeZone("ECT"), Logger.getLogger("name"));
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
    	    	//absorb
    	    }
    	}

    }
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
    	if (getInfoTypeSecondaryAddressing() == 1) {
    		setInfoTypeDeviceID("253");
    	}
    }
    protected List<String> doTheGetOptionalKeys() {
        return Collections.emptyList();
    }

    public String getFirmwareVersion() throws IOException {
        return "NOT YET IMPLEMENTED";
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

}
