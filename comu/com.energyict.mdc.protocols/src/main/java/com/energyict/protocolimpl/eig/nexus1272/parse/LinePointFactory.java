/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.eig.nexus1272.parse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LinePointFactory {

	private Map <String, LinePoint> lpCache = new HashMap <String, LinePoint> (); 
	
	 public LinePoint getLinePoint (int line, int point, int channel) {
		 LinePoint lp = lpCache.get(line + "." + point);
		 if (lp==null) {
			lp = new LinePoint(line, point, channel);
		 }
		 return lp;
	 }
	 
	 public void addLinePoint (int line, int point, int channel) throws IOException {
		 if (lpCache.get(line + "." + point)!=null) {
			 throw new IOException("Line point already in cache");
		 }
		
		 lpCache.put(line + "." + point, new LinePoint(line, point, channel));
	 }
}
