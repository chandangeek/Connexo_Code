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

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.functioncode.MandatoryDeviceIdentification;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 *
 * @author Koen
 */
@Deprecated //Never released, technical class
public class GenericModbusDiscover extends Modbus {

    final int DEBUG=0;

    public GenericModbusDiscover(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected void doTheConnect() throws IOException {
    }

    @Override
    protected void doTheDisConnect() throws IOException {
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        super.setProperties(properties);
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getTypedProperty(PK_INTERFRAME_TIMEOUT, "25").trim()));
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "THIS PROTOCOL IS ONLY FOR DISCOVERY";
    }

    @Override
    public String getProtocolVersion() {
        return "$Revision: 1.4 $";
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    @Override
    public Date getTime() throws IOException {
        return new Date();
    }

    private DiscoverResult discoverHoldingRegister(DiscoverTools discoverTools) {
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();
        discoverResult.setDiscovered(false);
        discoverResult.setResult("");

        try {
            if (DEBUG>=1) {
                System.out.println("GenericModbusDiscover, discoverHoldingRegister...");
            }
            for (Object o : DiscoverProtocolInfo.getSupportedDevicesList()) {
                DiscoverProtocolInfo dpi = (DiscoverProtocolInfo) o;
                if (dpi.isDiscoverMethodHoldingRegister()) {
                    int value = 0;
                    try {
                        value = ((BigDecimal) getRegisterFactory().findRegister(dpi.getDeviceType()).value()).intValue();
                    } catch (ModbusException e) {
                        //e.printStackTrace();
                    } catch (ProtocolConnectionException e) {
                        //System.out.println(e.getMessage());
                        continue;
                    }
                    boolean found = false;
                    StringTokenizer strTok = new StringTokenizer(dpi.getDetectionString(), ";");
                    while (strTok.countTokens() > 0) {
                        String detectionToken = strTok.nextToken();

                        int detectiontokenValue = 0;
                        if (detectionToken.indexOf("0x") == 0) {
                            detectiontokenValue = Integer.parseInt(detectionToken.substring(2), 16);
                        } else {
                            detectiontokenValue = Integer.parseInt(detectionToken);
                        }

                        if (value == detectiontokenValue) {
                            discoverResult.setDiscovered(true);
                            discoverResult.setProtocolName(dpi.getProtocolName());
                            discoverResult.setAddress(discoverTools.getAddress());
                            discoverResult.setResult("" + value);
                            discoverResult.setDeviceTypeName(dpi.getDeviceType());
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
        }
        catch (Exception e) {
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
            for (Object o : DiscoverProtocolInfo.getSupportedDevicesList()) {
                DiscoverProtocolInfo dpi = (DiscoverProtocolInfo) o;
                if (dpi.isDiscoverMethodSlaveId()) {
                    if (str.toLowerCase().contains(dpi.getDetectionString().toLowerCase())) {
                        discoverResult.setDiscovered(true);
                        discoverResult.setProtocolName(dpi.getProtocolName());
                        discoverResult.setAddress(discoverTools.getAddress());
                        discoverResult.setResult(str);
                        discoverResult.setDeviceTypeName(dpi.getDeviceType());

                        discoverResult.setShortDeviceTypeName("");
                        discoverResult.setDeviceName(str.replace('.', '-').replace('/', ' ')); // '.' and '/' are not allowed in EIServer as character in a device name!

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

            for (Object o : DiscoverProtocolInfo.getSupportedDevicesList()) {
                DiscoverProtocolInfo dpi = (DiscoverProtocolInfo) o;
                if (dpi.isDiscoverMethodMeterId()) {
                    if ((mdi.getVendorName().toLowerCase().contains(dpi.getMeterId()[0].toLowerCase())) && (mdi.getProductCode().toLowerCase().contains(dpi.getMeterId()[1].toLowerCase()))) {
                        discoverResult.setDiscovered(true);
                        discoverResult.setProtocolName(dpi.getProtocolName());
                        discoverResult.setAddress(discoverTools.getAddress());
                        discoverResult.setResult(mdi.getVendorName() + ", " + mdi.getProductCode());
                        discoverResult.setDeviceTypeName(dpi.getDeviceType());
                        break;
                    }
                }
            }


        }
        catch (Exception e) {
            discoverResult.setResult(e.toString());
        }
        return discoverResult;
    }

    @Override
    public DiscoverResult discover(DiscoverTools discoverTools) {

        if (DEBUG>=1) {
            System.out.println("GenericModbusDiscover, discover(" + discoverTools + ")");
        }

        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();

        try {
            setProperties(com.energyict.cpo.TypedProperties.copyOf(discoverTools.getProperties()));
            if (getInfoTypeHalfDuplex() != 0) {
                setHalfDuplexController(discoverTools.getDialer().getHalfDuplexController());
            }
            init(discoverTools.getDialer().getInputStream(),discoverTools.getDialer().getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
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
        catch (Exception e) {
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