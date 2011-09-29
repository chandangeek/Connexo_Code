package com.elster.protocolimpl.lis200.objects;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

public class ArchiveIntervalObject extends AbstractObject {

	/** The address of object */
	private String startAddress = "";

	/** The instance of the object */
	private int instance = 1;

	private String[] timeUnits = null;
	
	public ArchiveIntervalObject(ProtocolLink link, String address) {
		super(link);
		String[] addressPart = address.split(":");
		try {
			instance = Integer.parseInt(addressPart[0]);
		}
		catch (Exception e) {
			instance = 1;
		}
		startAddress = addressPart[1];
	}

	@Override
	protected String getInitialAddress() {
		return startAddress;
	}

	@Override
	protected int getObjectInstance() {
		return instance;
	}

	public int getIntervalSeconds() throws IOException {
	
		if (timeUnits == null) {
			getTimeUnits();
		}
		
		/*               s  m   h   D   M   Y*/
	    int factors[] = {1, 60, 60, 24, 30, 365};
        int factor = 1;
        
		String value = this.getValue();
		String[] data = value.split("[*]");
		
		for (int i = 0; i < 6;) {
			if (data[1].equalsIgnoreCase(timeUnits[i])) {
				break;
			}
			i++;
			factor *= factors[i];	
		}
		return Integer.parseInt(data[0]) * factor;
	}

	private void getTimeUnits() throws IOException {
		String sa_save = startAddress;
		int    in_save = instance;
		
		timeUnits = new String[6];

		try {
		startAddress = "1E0.0";
		for (int i = 1; i < 7; i++) {
			instance = i;
			timeUnits[i - 1] = this.getValue();
		}
		}
		catch (Exception e) {
			/* DL240 has fix names */
			String[] s = {"s", "m", "h", "D", "M", "Y"};
			timeUnits = s.clone();
		}
		startAddress = sa_save;
		instance = in_save;
	}
	
	
}