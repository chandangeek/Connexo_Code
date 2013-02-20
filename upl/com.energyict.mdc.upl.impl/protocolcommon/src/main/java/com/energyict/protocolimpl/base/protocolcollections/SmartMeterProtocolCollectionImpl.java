package com.energyict.protocolimpl.base.protocolcollections;

import com.energyict.protocol.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 26/03/12
 * Time: 9:40
 * To change this template use File | Settings | File Templates.
 */
public class SmartMeterProtocolCollectionImpl implements ProtocolCollection {

    List<String> protocolClasses, protocolNames;

    public SmartMeterProtocolCollectionImpl(){
        buildDefaultProtocols();
    }

    public void buildDefaultProtocols() {
        this.protocolClasses = new ArrayList();
        this.protocolNames = new ArrayList();


        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub");this.protocolNames.add("UkHub");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.InHomeDisplay");this.protocolNames.add("UkHub InHomeDisplay");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ZigbeeGas");this.protocolNames.add("UkHub ZigBee Gas");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.webrtuz3.WebRTUZ3");this.protocolNames.add("Smart WebRtu Z3");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.webrtuz3.EMeter");this.protocolNames.add("Smart WebRtu Z3 E-meter");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.webrtuz3.MbusDevice");this.protocolNames.add("Smart WebRtu Z3 Mbus Meter");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.elster.apollo.AS300");this.protocolNames.add("Elster AS300 (UkHub)");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD");this.protocolNames.add("Landis and Gyr ZMD");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP");this.protocolNames.add("DSMR 2.3 WebRtu KP");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice");this.protocolNames.add("DSMR 2.3 WebRtu KP - Mbus");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.Mx382");this.protocolNames.add("DSMR 2.3 Iskra Mx382");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.MbusDevice");this.protocolNames.add("DSMR 2.3 Iskra Mx382 - Mbus");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350");this.protocolNames.add("DSMR 4.0 Landis and Gyr E350");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372");this.protocolNames.add("Pre-NTA Iskra Mx372");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.MbusDevice");this.protocolNames.add("Pre-NTA Iskra Mx372 - Mbus");

    }

    public String getProtocolName(int index) throws IOException {
        return this.protocolNames.get(index);
    }
    public String getProtocolClassName(int index) throws IOException {
        return this.protocolClasses.get(index);
    }

    /**
     * Getter for property protocolClasses.
     * @return Value of property protocolClasses.
     */
    public List<String> getProtocolClasses() {
        return this.protocolClasses;
    }

    public String getProtocolVersion(int index) throws IOException {
        ProtocolInstantiator pi = ProtocolImplFactory.getProtocolInstantiator(getProtocolClassName(index));
        return (String)this.protocolNames.get(index)+": "+pi.getMeterProtocol().getProtocolVersion();
    }
    public String getProtocolRevision(int index) throws IOException {
        return getSmartMeterProtocolInstance(getProtocolClassName(index)).getVersion();
    }

    public String getProtocolVersions() throws IOException {
        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<this.protocolClasses.size();i++) {
            strBuff.append(getProtocolVersion(i)+"\n");
        }
        return strBuff.toString();
    }
    public int getSize() {
        return this.protocolClasses.size();
    }
    /**
     * Getter for property protocolNames.
     * @return Value of property protocolNames.
     */
    public List<String> getProtocolNames() {
        return this.protocolNames;
    }

    private SmartMeterProtocol getSmartMeterProtocolInstance(String className) throws IOException {
        try {
            return (SmartMeterProtocol) Class.forName(className).newInstance();
        }
        catch(ClassNotFoundException e) {
            throw new IOException("instantiateProtocol(), ClassNotFoundException, "+e.getMessage());
        }
        catch(InstantiationException e) {
            throw new IOException("instantiateProtocol(), InstantiationException, "+e.getMessage());
        }
        catch(IllegalAccessException e) {
            throw new IOException("instantiateProtocol(), IllegalAccessException, "+e.getMessage());
        }
        catch(Exception e) {
            throw new IOException("instantiateProtocol(), Exception, "+e.getMessage());
        }
    }
}
