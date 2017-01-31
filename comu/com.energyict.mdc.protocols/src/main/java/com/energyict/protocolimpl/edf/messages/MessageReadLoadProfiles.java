/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edf.messages;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;

public class MessageReadLoadProfiles extends MessageContent {

	protected final static String ELEMENTNAME = "onDemandReadLoadProfiles";
	protected final static String FROMATTRIBUTE = "from";
	protected final static String TOATTRIBUTE = "to";

	private Date from = null;
	private Date to = null;

	public MessageReadLoadProfiles() {
		super();
	}

	public MessageReadLoadProfiles(Date from, Date to) {
		super();
		this.from = from;
		this.to = to;
	}

	public MessageReadLoadProfiles(Element element) {
		super(element);
		String fromString = element.getAttribute(FROMATTRIBUTE);
		if (!fromString.isEmpty()) {
			from = new Date(Long.parseLong(fromString));
		}
		String toString = element.getAttribute(TOATTRIBUTE);
		if (!toString.isEmpty()) {
			to = new Date(Long.parseLong(toString));
		}
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		root.setAttribute(ORDINALATTRIBUTENAME,""+getOrdinal());
		if (from != null){
			root.setAttribute(FROMATTRIBUTE, ""+ from.getTime());
		}
		if (to != null){
			root.setAttribute(TOATTRIBUTE, ""+ to.getTime());
		}
		return root;
	}

}