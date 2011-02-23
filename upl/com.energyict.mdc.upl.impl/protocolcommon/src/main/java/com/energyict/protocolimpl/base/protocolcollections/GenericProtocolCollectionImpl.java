package com.energyict.protocolimpl.base.protocolcollections;

import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.protocol.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Copyrights EnergyICT
 * Date: 22-jun-2010
 * Time: 14:30:01
 * </p>
 */
public class GenericProtocolCollectionImpl implements ProtocolCollection{

    List protocolclasses,protocolnames;

    /** Creates a new instance of ProtocolCollection */
    public GenericProtocolCollectionImpl() {
        buildDefaultProtocols();
    }

    public void buildDefaultProtocols() {
        this.protocolclasses = new ArrayList();
        this.protocolnames = new ArrayList();


        this.protocolclasses.add("com.energyict.genericprotocolimpl.actarisace4000.ActarisACE4000");this.protocolnames.add("Actaris ACE4000");
        this.protocolclasses.add("com.energyict.genericprotocolimpl.actarisplcc3g.Concentrator");this.protocolnames.add("Actaris PLCC 3G concentrator");
        this.protocolclasses.add("com.energyict.genericprotocolimpl.gatewayz3.GateWayZ3");this.protocolnames.add("WebRTU GateWay Z3");
        this.protocolclasses.add("com.energyict.genericprotocolimpl.iskragprs.IskraMx37x");this.protocolnames.add("Iskra Mx372 GPRS");
        this.protocolclasses.add("com.energyict.genericprotocolimpl.iskrap2lpc.Concentrator");this.protocolnames.add("Iskra P2LPC Concentrator");
        this.protocolclasses.add("com.energyict.genericprotocolimpl.lgadvantis.Meter");this.protocolnames.add("L&G Advantis");
        this.protocolclasses.add("com.energyict.genericprotocolimpl.nta.eict.WebRTUKP");this.protocolnames.add("NTA WebRTU KP");
        this.protocolclasses.add("com.energyict.genericprotocolimpl.nta.elster.AM100");this.protocolnames.add("NTA Elster AM100");
        this.protocolclasses.add("com.energyict.genericprotocolimpl.nta.iskra.Mx382");this.protocolnames.add("NTA Iskra Mx382");
        this.protocolclasses.add("com.energyict.genericprotocolimpl.webrtuz3.WebRTUZ3");this.protocolnames.add("WebRTU Z3 / MUC");
        this.protocolclasses.add("com.energyict.genericprotocolimpl.elster.ctr.MTU155");this.protocolnames.add("Elster MTU155 (CTR)");

    }

    public String getProtocolName(int index) throws IOException {
        return (String)this.protocolnames.get(index);
    }
    public String getProtocolClassName(int index) throws IOException {
        return (String)this.protocolclasses.get(index);
    }

    /**
     * Getter for property protocolClasses.
     * @return Value of property protocolClasses.
     */
    public java.util.List getProtocolClasses() {
        return this.protocolclasses;
    }

    public String getProtocolVersion(int index) throws IOException {
        ProtocolInstantiator pi = ProtocolImplFactory.getProtocolInstantiator(getProtocolClassName(index));
        return (String)this.protocolnames.get(index)+": "+pi.getMeterProtocol().getProtocolVersion();
    }
    public String getProtocolRevision(int index) throws IOException {
        return getGenericProtocoLinstance(getProtocolClassName(index)).getVersion();
    }

    public String getProtocolVersions() throws IOException {
        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<this.protocolclasses.size();i++) {
            strBuff.append(getProtocolVersion(i)+"\n");
        }
        return strBuff.toString();
    }
    public int getSize() {
        return this.protocolclasses.size();
    }
    /**
     * Getter for property protocolNames.
     * @return Value of property protocolNames.
     */
    public java.util.List getProtocolNames() {
        return this.protocolnames;
    }

    private GenericProtocol getGenericProtocoLinstance(String className) throws IOException {
                try {
            return (GenericProtocol) Class.forName(className).newInstance();
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
