/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edf.messages.objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Calendar;

public class MeterClock extends ComplexCosemObject {

	protected final static String ELEMENTNAME = "meterClock";
	protected final static String OCTETSTRINGELEMENTNAME = "octetString";

	private CosemCalendar cosemCalendar = new CosemCalendar();

	public MeterClock() {
		super();
	}

	public MeterClock(Calendar timestamp, boolean daylightSavingTimeActive) {
		super();		
		cosemCalendar = new CosemCalendar(timestamp,daylightSavingTimeActive);
	}

	public MeterClock(Element element) {
		super(element);
		Element calendarElement = (Element) element.getFirstChild();
		cosemCalendar = new CosemCalendar(calendarElement);
	}

        public String toString() {
            return cosemCalendar.toString();
        }
        
	public CosemCalendar getCosemCalendar() {
		return cosemCalendar;
	}

	public void setCosemCalendar(CosemCalendar calendar) {
		this.cosemCalendar = calendar;
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		root.appendChild(cosemCalendar.generateXMLElement(document));
		return root;
	}

}
