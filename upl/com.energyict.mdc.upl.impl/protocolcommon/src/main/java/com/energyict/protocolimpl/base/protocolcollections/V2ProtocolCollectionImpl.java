package com.energyict.protocolimpl.base.protocolcollections;

import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.protocol.ProtocolCollection;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolImplFactory;
import com.energyict.protocol.ProtocolInstantiator;

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
public class V2ProtocolCollectionImpl implements ProtocolCollection {

    List<String> protocolClasses, protocolNames;

    public V2ProtocolCollectionImpl(){
        buildDefaultProtocols();
    }

    public void buildDefaultProtocols() {
        this.protocolClasses = new ArrayList();
        this.protocolNames = new ArrayList();
        this.protocolClasses.add("com.energyict.protocolimplv2.ace4000.ACE4000Outbound");  this.protocolNames.add("Actaris ACE4000 MeterXML");
        this.protocolClasses.add("com.energyict.protocolimplv2.ace4000.ACE4000MBus");  this.protocolNames.add("Actaris ACE4000 MeterXML Mbus Device");
        this.protocolClasses.add("com.energyict.protocolimplv2.nta.elster.AM100");  this.protocolNames.add("Elster AS220/AS1440 AM100 DLMS (PRE-NTA)");
        this.protocolClasses.add("com.energyict.protocolimplv2.nta.elster.MbusDevice");  this.protocolNames.add("Elster AS220/AS1440 AM100 DLMS (PRE-NTA) Mbus Slave");
        this.protocolClasses.add("com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155");  this.protocolNames.add("Elster MTU155 CTR");
        this.protocolClasses.add("com.energyict.protocolimplv2.eict.eiweb.EIWeb");  this.protocolNames.add("EnergyICT RTU EIWeb");
        this.protocolClasses.add("com.energyict.protocolimplv2.eict.rtuplusserver.g3.RtuPlusServer");  this.protocolNames.add("EnergyICT RTU+Server2 G3 DLMS");
        this.protocolClasses.add("com.energyict.protocolimplv2.eict.rtuplusserver.idis.RtuPlusServer");  this.protocolNames.add("EnergyICT RTU+Server2 IDIS DLMS");
        this.protocolClasses.add("com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP");  this.protocolNames.add("EnergyICT WebRTU KP DLMS (NTA DSMR2.3)");
        this.protocolClasses.add("com.energyict.protocolimplv2.nta.dsmr23.eict.MbusDevice");  this.protocolNames.add("EnergyICT WebRTU KP DLMS (NTA DSMR2.3) Mbus Slave");
        this.protocolClasses.add("com.energyict.protocolimplv2.eict.gatewayz3.GateWayZ3");  this.protocolNames.add("EnergyICT WebRTU Z3 DLMS Gateway");
        this.protocolClasses.add("com.energyict.protocolimplv2.edp.JanzB280");  this.protocolNames.add("Janz B280 DLMS");
        this.protocolClasses.add("com.energyict.protocolimplv2.edp.CX20009");  this.protocolNames.add("SagemCom CX2000-9 DLMS");
        this.protocolClasses.add("com.energyict.protocolimplv2.elster.garnet.GarnetConcentrator");  this.protocolNames.add("Elster Concentrator Garnet");
        this.protocolClasses.add("com.energyict.protocolimplv2.elster.garnet.A100C");  this.protocolNames.add("Elster A100C Garnet");
        this.protocolClasses.add("com.energyict.protocolimplv2.abnt.elster.A1055"); this.protocolNames.add("Elster A1055 ABNT");
        this.protocolClasses.add("com.energyict.protocolimplv2.dlms.idis.am130.AM130"); this.protocolNames.add("AM130 DLMS (IDIS P2)");
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
        return getDeviceProtocolInstance(getProtocolClassName(index)).getVersion();
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

    private DeviceProtocol getDeviceProtocolInstance(String className) throws IOException {
        try {
            return (DeviceProtocol) Class.forName(className).newInstance();
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