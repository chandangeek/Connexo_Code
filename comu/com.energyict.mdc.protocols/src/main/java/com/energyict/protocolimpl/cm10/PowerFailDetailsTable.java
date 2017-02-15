/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.cm10;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PowerFailDetailsTable {

	private CM10 cm10Protocol;

	private byte[] timPf;
	private byte[] timPr;
	private byte[] dialStructure;
	private byte[] hrOut;
	private byte[] secOut;
	private byte[] lpfCnt;
	private List recentPf = new ArrayList();
	private List longestPf = new ArrayList();


	public PowerFailDetailsTable(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}




	public void parse(byte[] data) throws IOException {
		cm10Protocol.getLogger().info("length events: " + data.length);
		timPf =ProtocolUtils.getSubArray(data, 0, 5);
		timPr =ProtocolUtils.getSubArray(data, 6, 11);
		dialStructure =ProtocolUtils.getSubArray(data, 12, 155);
		hrOut =ProtocolUtils.getSubArray(data, 156, 157);
		secOut =ProtocolUtils.getSubArray(data, 158, 159);
		lpfCnt =ProtocolUtils.getSubArray(data, 160, 161);

        Calendar now = ProtocolUtils.getCalendar(cm10Protocol.getTimeZone());
        now.setTime(cm10Protocol.getTime());

		int offset = 162;
		for (int i = 0; i < 12; i++) {
			PowerFailEntry entry = new PowerFailEntry(cm10Protocol);
			entry.parse(ProtocolUtils.getSubArray(data, offset, offset + 5), now);
			if (entry.isValid())
				recentPf.add(entry);
			offset = offset + 6;
		}

		offset = 234;
		for (int i = 0; i < 3; i++) {
			PowerFailEntry entry = new PowerFailEntry(cm10Protocol);
			entry.parse(ProtocolUtils.getSubArray(data, offset, offset + 5), now);
			if (entry.isValid())
				longestPf.add(entry);
			offset = offset + 6;
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("");
		buf.append("timPf = " + ProtocolUtils.outputHexString(timPf)).append("\n");
		buf.append("timPr = " + ProtocolUtils.outputHexString(timPr)).append("\n");
		buf.append("dialStructure = " + ProtocolUtils.outputHexString(dialStructure)).append("\n");
		buf.append("hrOut = " + ProtocolUtils.outputHexString(hrOut)).append("\n");
		buf.append("secOut = " + ProtocolUtils.outputHexString(secOut)).append("\n");
		buf.append("lpfCnt = " + ProtocolUtils.outputHexString(lpfCnt)).append("\n\n");

		buf.append("most recent power failures:\n");
		for (int i = 0; i < recentPf.size(); i++) {
			PowerFailEntry entry = (PowerFailEntry) recentPf.get(i);
			buf.append(i).append(": ").append(entry.toString()).append("\n");

		}

		buf.append("longest power failures:\n");
		for (int i = 0; i < longestPf.size(); i++) {
			PowerFailEntry entry = (PowerFailEntry) longestPf.get(i);
			buf.append(i).append(": ").append(entry.toString()).append("\n");
		}

		return buf.toString();
	}

	public boolean isPowerUp(Date endOfInterval) throws IOException {
		Calendar cal = Calendar.getInstance(cm10Protocol.getTimeZone());
		cal.setTime(endOfInterval);
		cal.add(Calendar.SECOND, - cm10Protocol.getProfileInterval());
		Date startOfInterval = cal.getTime();
		for (int i = 0; i < recentPf.size(); i++) {
			PowerFailEntry entry = (PowerFailEntry) recentPf.get(i);
			if (entry.isPowerUpInterval(startOfInterval, endOfInterval))
				return true;
		}
		for (int i = 0; i < longestPf.size(); i++) {
			PowerFailEntry entry = (PowerFailEntry) longestPf.get(i);
			if (entry.isPowerUpInterval(startOfInterval, endOfInterval))
				return true;
		}
		return false;
	}

	public boolean isPowerDown(Date endOfInterval) throws IOException {
		Calendar cal = Calendar.getInstance(cm10Protocol.getTimeZone());
		cal.setTime(endOfInterval);
		cal.add(Calendar.SECOND, - cm10Protocol.getProfileInterval());
		Date startOfInterval = cal.getTime();
		for (int i = 0; i < recentPf.size(); i++) {
			PowerFailEntry entry = (PowerFailEntry) recentPf.get(i);
			if (entry.isPowerDownInterval(startOfInterval, endOfInterval))
				return true;
		}
		for (int i = 0; i < longestPf.size(); i++) {
			PowerFailEntry entry = (PowerFailEntry) longestPf.get(i);
			if (entry.isPowerDownInterval(startOfInterval, endOfInterval))
				return true;
		}
		return false;
	}

	public List getEvents() {
		List events = new ArrayList();
		int size = recentPf.size();
		for (int i = 0; i < size; i++) {
			PowerFailEntry entry = (PowerFailEntry) recentPf.get(i);
			if (entry.isValid())
				events.add(entry);
		}
		size = longestPf.size();
		for (int i = 0; i < size; i++) {
			PowerFailEntry entry = (PowerFailEntry) longestPf.get(i);
			if ((entry.isValid()) && (!contains(events, entry)))
				events.add(entry);
		}
		Collections.sort(events);
		return events;
	}

	protected boolean contains(List events, PowerFailEntry entry) {
		int size = events.size();
		for (int i = 0; i < size; i++) {
			PowerFailEntry entryInEvents = (PowerFailEntry) events.get(i);
			if (entryInEvents.equals(entry))
				return true;
		}
		return false;
	}



}
