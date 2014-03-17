package com.energyict.protocolimpl.base.protocolcollections;

import com.energyict.protocol.ProtocolCollection;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolImplFactory;
import com.energyict.protocol.ProtocolInstantiator;
import com.energyict.protocol.SmartMeterProtocol;

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

    private void buildDefaultProtocols() {
        this.protocolClasses = new ArrayList();
        this.protocolNames = new ArrayList();

        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.elster.apollo5.AS300DPET");  this.protocolNames.add("Elster AS300D-PET DLMS");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.elster.AS300P.AS300P");  this.protocolNames.add("Elster AS300-P DLMS (SSWG EC)");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.elster.apollo.AS300");  this.protocolNames.add("Elster AS300-P DLMS (SSWG IC)");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.Z igbeeGas");  this.protocolNames.add("Elster BK-G4E  (SSWG EC) DLMS");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ZigbeeGas");  this.protocolNames.add("Elster BK-G4E  (SSWG IC) DLMS");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr40.elster.MBusDevice");  this.protocolNames.add("Elster BK-Gx DLMS (NTA DSMR4.0) Mbus Slave");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110R");  this.protocolNames.add("EnergyICT AM110R DLMS (SSWG EC)");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd.InHomeDisplay");  this.protocolNames.add("EnergyICT AM110R DLMS (SSWG EC) Zigbee IHD");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub");  this.protocolNames.add("EnergyICT AM110R DLMS (SSWG IC)");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.InHomeDisplay");  this.protocolNames.add("EnergyICT AM110R DLMS (SSWG IC) Zigbee IHD");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP");  this.protocolNames.add("EnergyICT WebRTU KP DLMS (NTA DSMR2.3)");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice");  this.protocolNames.add("EnergyICT WebRTU KP DLMS (NTA DSMR2.3) Mbus Slave");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.webrtuz3.WebRTUZ3");  this.protocolNames.add("EnergyICT WebRTU Z3 DLMS");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.webrtuz3.EMeter");  this.protocolNames.add("EnergyICT WebRTU Z3 DLMS EMeter Slave");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.webrtuz3.MbusDevice");  this.protocolNames.add("EnergyICT WebRTU Z3 DLMS Mbus Slave");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.eict.webrtuz3.SlaveDevice");  this.protocolNames.add("EnergyICT WebRTU Z3 DLMS TIC Slave");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm.Kaifa");  this.protocolNames.add("IBM Kaifa DLMS (NTA DSMR4.0)");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm.MBusDevice");  this.protocolNames.add("IBM Kaifa DLMS (NTA DSMR4.0) Mbus Slave");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372");  this.protocolNames.add("Iskraemeco Mx372 DLMS (PRE-NTA)");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.MbusDevice");  this.protocolNames.add("Iskraemeco Mx372 DLMS (PRE-NTA) Mbus Slave");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.Mx382");  this.protocolNames.add("Iskraemeco Mx382 DLMS (NTA DSMR2.3)");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra.MbusDevice");  this.protocolNames.add("Iskraemeco Mx382 DLMS (NTA DSMR2.3) Mbus Slave");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.actaris.sl7000.ActarisSl7000");  this.protocolNames.add("Itron SL7000 DLMS");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350");  this.protocolNames.add("Landis+Gyr E350 XEMEX DLMS (NTA DSMR4.0)");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.MBusDevice");  this.protocolNames.add("Landis+Gyr E350 XEMEX DLMS (NTA DSMR4.0) Mbus Slave");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD");  this.protocolNames.add("Landis+Gyr ICG Family DLMS");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.REMIDatalogger");  this.protocolNames.add("XEMEX ReMI DLMS (NTA DSMR4.0)");
        this.protocolClasses.add("com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.MbusDevice");  this.protocolNames.add("XEMEX ReMI DLMS (NTA DSMR4.0) Mbus Slave");
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
            throw new ProtocolException("instantiateProtocol(), ClassNotFoundException, "+e.getMessage());
        }
        catch(InstantiationException e) {
            throw new ProtocolException("instantiateProtocol(), InstantiationException, "+e.getMessage());
        }
        catch(IllegalAccessException e) {
            throw new ProtocolException("instantiateProtocol(), IllegalAccessException, "+e.getMessage());
        }
        catch(Exception e) {
            throw new ProtocolException("instantiateProtocol(), Exception, "+e.getMessage());
        }
    }
}
