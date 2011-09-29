package com.elster.protocolimpl.lis200.objects;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

public class IntervalObject extends SimpleObject {

	protected String[] timeUnits = null;

	public IntervalObject(ProtocolLink link, String address) {
		super(link, address);
	}

	public IntervalObject(ProtocolLink link, int instance, String address) {
		super(link, instance, address);
	}
	
	public int getIntervalSeconds() throws IOException {

		getTimeUnits();
		
		/* s m h D M Y */
		int factors[] = { 1, 60, 60, 24, 30, 365 };
		int factor = 1;

		String value = this.getValue();
		String[] data = value.split("[*]");

		for (int i = 0; i < timeUnits.length;) {
			if (data[1].equalsIgnoreCase(timeUnits[i])) {
				break;
			}
			i++;
			factor *= factors[i];
		}
		return Integer.parseInt(data[0]) * factor;
	}

	/**
	 * get stored names for date units
	 * If device has no register for date units, set defaults...
	 * 
	 * @throws java.io.IOException - in case of an error
	 */
	private void getTimeUnits() throws IOException {
		if (timeUnits == null) {

			String sa_save = startAddress;
			int in_save = instance;

			timeUnits = new String[6];

			try {
				startAddress = "1E0.0";
				for (int i = 0; i < timeUnits.length; i++) {
					instance = i + 1;
					timeUnits[i] = this.getValue().trim();
				}
			} catch (Exception e) {
				/* DL240 has fix names */
				@SuppressWarnings({"MismatchedReadAndWriteOfArray"})
                String[] s = { "s", "m", "h", "D", "M", "Y" };
				timeUnits = s.clone();
			}
			startAddress = sa_save;
			instance = in_save;
		}
	}

}