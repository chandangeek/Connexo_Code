package com.energyict.protocolimpl.edf.messages.objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MovingPeak extends ComplexCosemObject {

	protected final static String ELEMENTNAME = "movingPeak";
	protected final static String MOVINGPEAKELEMENTS = "movingPeakScript";
	protected final static String ATTRIBUTESCRIPTID = "scriptId";
	protected final static String ATTRIBUTESERVICEID = "serviceId";
	protected final static String ATTRIBUTECLASSID = "classId";
	protected final static String ATTRIBUTELOGICALNAME = "logicalName";
	protected final static String ATTRIBUTEINDEX = "index";


	private List scripts = new ArrayList();

	public MovingPeak() {
		super();
	}

	public MovingPeak(Element element){
		super(element);
		NodeList scriptNodes = element.getElementsByTagName(MOVINGPEAKELEMENTS);
		for (int i=0; i< scriptNodes.getLength(); i++){
			Element scriptElement = (Element) scriptNodes.item(i);
			int scriptId = new Integer(scriptElement.getAttribute(ATTRIBUTESCRIPTID)).intValue();
			int serviceId = new Integer(scriptElement.getAttribute(ATTRIBUTESERVICEID)).intValue();
			int classId = new Integer(scriptElement.getAttribute(ATTRIBUTECLASSID)).intValue();
			String logicalName = scriptElement.getAttribute(ATTRIBUTELOGICALNAME);
			int index = new Integer(scriptElement.getAttribute(ATTRIBUTEINDEX)).intValue();
			MovingPeakScript script = new MovingPeakScript(scriptId, serviceId, classId, logicalName, index);
			scripts.add(script);
		}
	}

        public String toString() {
            StringBuffer strBuff = new StringBuffer();
            for (int i=0; i< scripts.size(); i++) {
                MovingPeakScript script = (MovingPeakScript) scripts.get(i);
                strBuff.append(script+"\n");
            }
            return strBuff.toString();
        }

	public void addScript(int scriptId, int serviceId, int classId,
			String logicalName, int index){
		scripts.add(new MovingPeakScript(scriptId,serviceId,classId,
			logicalName,index));
	}

	public List getScripts() {
		return scripts;
	}

	public void setScripts(List scripts) {
		this.scripts = scripts;
	}

	public MovingPeakScript getScript(int index){
		if (index < 0 || index >= scripts.size()){
			return null;
		} else {
			return (MovingPeakScript) scripts.get(index);
		}
	}

	public Element generateXMLElement(Document document){
			Element root = document.createElement(ELEMENTNAME);
			for (Iterator it = scripts.iterator();it.hasNext();){
				MovingPeakScript script = (MovingPeakScript) it.next();
				Element scriptElement = document.createElement(MOVINGPEAKELEMENTS);
				scriptElement.setAttribute(ATTRIBUTESCRIPTID, new Integer(script.getScriptId()).toString());
				scriptElement.setAttribute(ATTRIBUTESERVICEID, new Integer(script.getServiceId()).toString());
				scriptElement.setAttribute(ATTRIBUTECLASSID, new Integer(script.getClassId()).toString());
				scriptElement.setAttribute(ATTRIBUTELOGICALNAME, script.getLogicalName());
				scriptElement.setAttribute(ATTRIBUTEINDEX, new Integer(script.getIndex()).toString());
				root.appendChild(scriptElement);
			}
			return root;
	}



}
