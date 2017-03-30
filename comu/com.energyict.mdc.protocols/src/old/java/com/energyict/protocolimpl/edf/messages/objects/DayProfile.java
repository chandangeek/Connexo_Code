/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edf.messages.objects;

import com.energyict.mdc.common.ApplicationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DayProfile extends ComplexCosemObject {

	protected final static String ELEMENTNAME = "dayProfile";
	protected final static String DAYIDNAME = "dayId";
	protected final static String SEGMENTSNAME = "segments";

	private int dayId;
	private List segments = new ArrayList();

	public DayProfile() {
		super();
	}

	public DayProfile(int dayId) {
		super();
		this.dayId = dayId;
	}

	public DayProfile(Element element) {
		super(element);
		NodeList dayIds = element.getElementsByTagName(DAYIDNAME);
		if (dayIds.getLength() != 0){
			dayId = Integer.parseInt(dayIds.item(0).getFirstChild().getNodeValue());
		} else {
			throw new ApplicationException("Cannot create DayProfile");
		}
		NodeList listNode = element.getElementsByTagName(SEGMENTSNAME);
		if (listNode.getLength() != 0){
			NodeList childs = listNode.item(0).getChildNodes();
			for (int i=0; i< childs.getLength(); i++){
				Element segmentElement = (Element) childs.item(i);
				DayProfileSegment segment = new DayProfileSegment(segmentElement);
				segments.add(segment);
			}
		} else {
			throw new ApplicationException("Cannot create DayProfile");
		}
	}

        public String toString() {
            // Generated code by ToStringBuilder
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("DayProfile:\n");
            strBuff.append("   dayId="+getDayId()+"\n");
            strBuff.append("   segments="+getSegments()+"\n");
            return strBuff.toString();
        }

	public int getDayId() {
		return dayId;
	}

	public void setDayId(int dayId) {
		this.dayId = dayId;
	}

	public List getSegments() {
		return segments;
	}

	public void setSegments(List segments) {
		this.segments = segments;
	}

	public void addSegment(DayProfileSegment segment){
		this.segments.add(segment);
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		Element dayIdElement = document.createElement(DAYIDNAME);
		dayIdElement.appendChild(document.createTextNode(""+dayId));
		root.appendChild(dayIdElement);
		Element segmentsElement = document.createElement(SEGMENTSNAME);
		for (Iterator it = segments.iterator();it.hasNext();){
			DayProfileSegment profile = (DayProfileSegment) it.next();
			segmentsElement.appendChild(profile.generateXMLElement(document));
		}
		root.appendChild(segmentsElement);
		return root;
	}

}
