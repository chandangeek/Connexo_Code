/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * GenericModbusDiscover.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.discover;


import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;

import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.functioncode.MandatoryDeviceIdentification;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Logger;
/**
 *
 * @author Koen
 */
public class GenericModbusDiscover extends Modbus {

    @Override
    public String getProtocolDescription() {
        return "Generic modbus discover";
    }

    private static final int DEBUG=0;

    private final Clock clock;

    @Inject
    public GenericModbusDiscover(PropertySpecService propertySpecService, Clock clock) {
        super(propertySpecService);
        this.clock = clock;
    }

    protected void doTheConnect() throws IOException {

    }

    protected void doTheDisConnect() throws IOException {

    }

    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","25").trim()));
    }


    protected List<String> doTheGetOptionalKeys() {
        return Collections.emptyList();
    }

    public String getFirmwareVersion() throws IOException {
        return "THIS PROTOCOL IS ONLY FOR DISCOVERY";
    }

    public String getProtocolVersion() {
        return "$Revision: 1.4 $";
    }

    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    public Date getTime() throws IOException {
        return Date.from(this.clock.instant());
    }




    public DiscoverResult discoverHoldingRegister(DiscoverTools discoverTools) {
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();
        discoverResult.setDiscovered(false);
        discoverResult.setResult("");

        try {
            if (DEBUG>=1) {
                System.out.println("GenericModbusDiscover, discoverHoldingRegister...");
            }
            Iterator it = DiscoverProtocolInfo.getSupportedDevicesList().iterator();
            while(it.hasNext()) {
                DiscoverProtocolInfo dpi = (DiscoverProtocolInfo)it.next();
                if (dpi.isDiscoverMethodHoldingRegister()) {
                    int value=0;
                    try {
                        value = ((BigDecimal)getRegisterFactory().findRegister(dpi.getDeviceType()).value()).intValue();
                    }
                    catch(ModbusException e) {
                        //e.printStackTrace();
                    }
                    catch(ProtocolConnectionException e) {
                    	//System.out.println(e.getMessage());
                    	continue;
                    }
                    boolean found=false;
                    StringTokenizer strTok = new StringTokenizer(dpi.getDetectionString(),";");
                    while(strTok.countTokens()>0) {
                        String detectionToken = strTok.nextToken();

                        int detectiontokenValue;
                        if (detectionToken.indexOf("0x") == 0) {
                            detectiontokenValue = Integer.parseInt(detectionToken.substring(2), 16);
                        }
                        else {
                            detectiontokenValue = Integer.parseInt(detectionToken);
                        }

                        if (value==detectiontokenValue) {
                            discoverResult.setDiscovered(true);
                            discoverResult.setProtocolName(dpi.getProtocolName());
                            discoverResult.setAddress(discoverTools.getAddress());
                            discoverResult.setResult(""+value);
                            discoverResult.setDeviceTypeName(dpi.getDeviceType());
                            found=true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
        }
        catch(Exception e) {
            discoverResult.setResult(e.toString());
        }
        return discoverResult;
    }

    private DiscoverResult discoverSlaveId(DiscoverTools discoverTools) {
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();

        discoverResult.setDiscovered(false);
        discoverResult.setResult("");

        try {
            if (DEBUG>=1) {
                System.out.println("GenericModbusDiscover, discoverSlaveId...");
            }

            String str = getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getAdditionalDataAsString();
            if (DEBUG>=1) {
                System.out.println("getReportSlaveId().getAdditionalDataAsString()=" + str);
            }
            Iterator it = DiscoverProtocolInfo.getSupportedDevicesList().iterator();
            while(it.hasNext()) {
                DiscoverProtocolInfo dpi = (DiscoverProtocolInfo)it.next();
                if (dpi.isDiscoverMethodSlaveId()) {
                    if (str.toLowerCase().contains(dpi.getDetectionString().toLowerCase())) {
                        discoverResult.setDiscovered(true);
                        discoverResult.setProtocolName(dpi.getProtocolName());
                        discoverResult.setAddress(discoverTools.getAddress());
                        discoverResult.setResult(str);
                        discoverResult.setDeviceTypeName(dpi.getDeviceType());

                        discoverResult.setShortDeviceTypeName("");
                        discoverResult.setDeviceName(str.replace('.','-').replace('/',' ')); // '.' and '/' are not allowed in EIServer as character in a device name!

                        break;
                    }
                }
            }
        }
        catch(Exception e) {
            discoverResult.setResult(e.toString());
        }
        return discoverResult;
    }

    private DiscoverResult discoverMeterId(DiscoverTools discoverTools) {
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();

        discoverResult.setDiscovered(false);
        discoverResult.setResult("");

        try {
            if (DEBUG>=1) {
                System.out.println("GenericModbusDiscover, discoverMeterId...");
            }
            MandatoryDeviceIdentification mdi = getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification();

            if (DEBUG>=1) {
                System.out.println(mdi);
            }

            Iterator it = DiscoverProtocolInfo.getSupportedDevicesList().iterator();
            while(it.hasNext()) {
                DiscoverProtocolInfo dpi = (DiscoverProtocolInfo)it.next();
                if (dpi.isDiscoverMethodMeterId()) {
                    if ((mdi.getVendorName().toLowerCase().contains(dpi.getMeterId()[0].toLowerCase())) && (mdi.getProductCode().toLowerCase().contains(dpi.getMeterId()[1].toLowerCase()))) {
                        discoverResult.setDiscovered(true);
                        discoverResult.setProtocolName(dpi.getProtocolName());
                        discoverResult.setAddress(discoverTools.getAddress());
                        discoverResult.setResult(mdi.getVendorName()+", "+mdi.getProductCode());
                        discoverResult.setDeviceTypeName(dpi.getDeviceType());
                        break;
                    }
                }
            }


        }
        catch(Exception e) {
            discoverResult.setResult(e.toString());
        }
        return discoverResult;
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {

        if (DEBUG>=1) {
            System.out.println("GenericModbusDiscover, discover(" + discoverTools + ")");
        }

        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();

        try {
            setProperties(discoverTools.getProperties());
            if (getInfoTypeHalfDuplex() != 0) {
                setHalfDuplexController(null);
            }
            init(null, null, TimeZone.getTimeZone("ECT"), Logger.getLogger("name"));
            connect();

            discoverResult = discoverSlaveId(discoverTools);
            if (!discoverResult.isDiscovered()) {
                discoverResult = discoverMeterId(discoverTools);
            }
            if (!discoverResult.isDiscovered()) {
                discoverResult = discoverHoldingRegister(discoverTools);
            }


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

}
