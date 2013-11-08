package com.energyict.protocolimpl.eig.nexus1272.parse;

import java.util.HashMap;
import java.util.Map;

public class LinePoint {
		private int line;
		private int point;
		String description;
		int dataSize;
		private int channel;
		private boolean scaled;
		
		public LinePoint (int line, int point) {
			this.line = line;
			this.point = point;
			this.channel = -1;
			this.dataSize = 4;
			this.scaled = line >= 536 && line <= 590 && line != 581 && line!=582 ;
		}
		
		public LinePoint (int line, int point, int channel) {
			this.line = line;
			this.point = point;
			this.channel = channel;
			this.dataSize = 4;
		}
		
		public String getDescription() {
			String desc = descriptions.get(line + "." + point);
			if (desc == null) {
				return "No mapping for line & point [" + line + "." + point + "]";
			}
			return desc;
		}
		
		public final static Map <String, String> descriptions = new HashMap <String, String> ();
		static {
			descriptions.put("584.0", "Positive Wh (Quadrant 1+4) in the Interval, Scaled Primary");
			descriptions.put("584.5", "Negative Wh (Quadrant 2+3) in the Interval, Scaled Primary");
			descriptions.put("583.1", "Positive VARh (Quadrant 1+2) in the Interval, Scaled Primary");
			descriptions.put("583.2", "Negative VARh (Quadrant 3+4) in the Interval, Scaled Primary");
			
			
		}
		public int getLine() {
			return line;
		}

		public int getPoint() {
			return point;
		}

		public int getChannel() {
			return channel;
		}
		
		public String toString() {
			return "Line " + line + " point " + point + ":\t dataSize " + dataSize + " channel " + channel + " \n\tDescription " + getDescription();
		}

		public boolean isScaled() {
			//TODO this works, but can make cleaner
			return scaled;
		}
		
	}