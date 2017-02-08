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

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.mbus.core.MBus;

import java.io.IOException;

/**
 *
 * @author kvds
 */
public class Generic extends MBus {

    final int DEBUG=0;

    public Generic(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
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

    @Override
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
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            }
    	}
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
    	if (getInfoTypeSecondaryAddressing() == 1) {
    		setInfoTypeDeviceID("253");
    	}
    }

    @Override
    public String getFirmwareVersion() {
        return "NOT YET IMPLEMENTED";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

}