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

import com.energyict.cbo.*;
import com.energyict.dialer.core.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.protocol.discover.*;
import com.energyict.protocolimpl.mbus.core.*;
import com.energyict.protocolimpl.mbus.core.connection.iec870.*;
import com.energyict.protocolimpl.mbus.core.discover.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 *
 * @author kvds
 */
public class Generic extends MBus {
    
    
    final int DEBUG=1;
    
    RegisterFactory registerFactory=null;
    
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
            discoverResult.setDeviceName(cIField72h.getMeter3LetterId()+" V"+cIField72h.getVersion()+" "+cIField72h.getDeviceType().getShortDescription());
                    
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
        getMBusConnection().sendSND_NKE(); 
    }
    protected void doTheDisConnect() throws IOException {
        
    }
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        
    }
    protected List doTheGetOptionalKeys() {
        List list = new ArrayList();
        return list;
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "NOT YET IMPLEMENTED";
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.5 $";
    }    
    
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }
    
    private Properties getProperties(int address) {
        Properties properties = new Properties();
        properties.setProperty(MeterProtocol.ADDRESS,""+address);
        properties.setProperty("Retries", "1");
        properties.setProperty("Timeout", "250");
        return properties;        
    }    
    
    static public void main(String[] args) {
        
        try {
            // ********************** EictRtuModbus **********************
            DiscoverTools discoverTools = null;
            for (int address = 29;address<30;address++) {
                System.out.println("---------------------> discover address "+address);
                try {
                    Generic generic = new Generic();
                    

                    SerialCommunicationSettings settings = new SerialCommunicationSettings(2400,8, 'E', 1);

                    if ((args==null) || (args.length<=1))
                        discoverTools = new DiscoverTools("COM4",settings);
                    else
                        discoverTools = new DiscoverTools(args[1],settings); //"/dev/ttyXR0";
                    
                    discoverTools.setProperties(generic.getProperties(address));
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
}
