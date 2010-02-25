/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;

/**
 * This class extends an {@link Array} to give a fancy toString
 * method when showing PLC channel frequencies.
 *
 * @author jme
 */
public class Frequencies extends Array {

	private static final int	FS_INDEX	= 0;
	private static final int	FM_INDEX	= 1;

	public Frequencies(byte[] berEncodedData, int offset, int level) throws IOException {
		super(berEncodedData, offset, level);
	}

	/**
	 * @param channel (1 based, so value can be 1-6)
	 * @return
	 */
	public long getMarkFrequency(int channel) {
		if (!isValidChannel(channel)) {
			throw new IllegalArgumentException("ChannelNumber " + channel + " is not valid!");
		}
		return getDataType(channel-1).getStructure().getDataType(FM_INDEX).longValue();
	}

	/**
	 * @param channel (1 based, so value can be 1-6)
	 * @return
	 */
	public long getSpaceFrequency(int channel) {
		if (!isValidChannel(channel)) {
			throw new IllegalArgumentException("ChannelNumber " + channel + " is not valid!");
		}
		return getDataType(channel-1).getStructure().getDataType(FS_INDEX).longValue();
	}

	public int getNumberOfChannels() {
		return nrOfDataTypes();
	}

	private boolean isValidChannel(int channel) {
		return ((channel >= 1) && (channel <= (nrOfDataTypes()+1)));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int channelNr = 0; channelNr < getNumberOfChannels(); channelNr++) {
			if (getDataType(channelNr).isStructure()) {
				Structure struct = getDataType(channelNr).getStructure();
				if (struct.nrOfDataTypes() == 2) {
					sb.append("[").append(channelNr+1).append("]=(");
					sb.append(getSpaceFrequency(channelNr+1)).append("Hz");
					sb.append(",");
					sb.append(getMarkFrequency(channelNr+1)).append("Hz");
					sb.append("); ");
				}
			}
		}
		return sb.toString();
	}

}
