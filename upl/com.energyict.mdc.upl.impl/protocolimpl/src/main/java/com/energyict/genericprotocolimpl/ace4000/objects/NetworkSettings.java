package com.energyict.genericprotocolimpl.ace4000.objects;

import com.energyict.genericprotocolimpl.ace4000.objects.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author gna
 *
 */
public class NetworkSettings extends AbstractActarisObject {

    private String dnsIPAddress;
    private String username;
    private String password;
    private String apn;
    private String port;
    private String ip;

    public String getApn() {
        return apn;
    }

    public void setApn(String apn) {
        this.apn = apn;
    }

    public String getDnsIPAddress() {
        return dnsIPAddress;
    }

    public void setDnsIPAddress(String dnsIPAddress) {
        this.dnsIPAddress = dnsIPAddress;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public NetworkSettings(ObjectFactory of) {
		super(of);
	}

	protected String prepareXML(){
		Document doc = createDomDocument();

		Element root = doc.createElement(XMLTags.MPULL);
		doc.appendChild(root);
		Element md = doc.createElement(XMLTags.METERDATA);
		root.appendChild(md);
		Element s = doc.createElement(XMLTags.SERIALNUMBER);
		s.setTextContent(getObjectFactory().getAce4000().getMasterSerialNumber());
		md.appendChild(s);
		Element t = doc.createElement(XMLTags.TRACKER);
		t.setTextContent(Integer.toString(getTrackingID(), 16));
		md.appendChild(t);

		Element cf = doc.createElement(XMLTags.CONFIGURATION);
		md.appendChild(cf);
		Element ipdef = doc.createElement(XMLTags.SYSTEMIPADDRESS);
		ipdef.setTextContent(getIp());
		cf.appendChild(ipdef);
		Element ns = doc.createElement(XMLTags.NETWORKSETTINGS);
		cf.appendChild(ns);
		Element dnsip = doc.createElement(XMLTags.DNSIPADDRESS);
		dnsip.setTextContent(getDnsIPAddress());
		ns.appendChild(dnsip);
		Element gun = doc.createElement(XMLTags.GPRSUSERNAME);
		gun.setTextContent(getUsername());
		ns.appendChild(gun);
		Element gpw = doc.createElement(XMLTags.GPRSPASSWORD);
		gpw.setTextContent(getPassword());
		ns.appendChild(gpw);
		Element gapn = doc.createElement(XMLTags.GPRSACCESSPOINT);
		gapn.setTextContent(getApn());
		ns.appendChild(gapn);
		Element csprt = doc.createElement(XMLTags.SYSTEMIPPORTNR);
		csprt.setTextContent(getPort());
		ns.appendChild(csprt);

		String msg = convertDocumentToString(doc);
		return (msg.substring(msg.indexOf("?>")+2));
	}

	protected void parse(Element element) {
	}
}