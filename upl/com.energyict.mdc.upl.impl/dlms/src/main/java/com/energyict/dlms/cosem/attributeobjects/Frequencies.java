/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;

/**
 * This class extends an {@link Array} to give a fancy toString
 * method when showing PLC channel frequencies.
 *
 * @author jme
 */
public class Frequencies extends Array implements Comparable<Frequencies> {

	private static final int	FM_INDEX	= 0;
	private static final int	FS_INDEX	= 1;

	/**
	 * @param frequencies
	 * @return
	 * @throws IOException
	 */
	public static Frequencies fromLongArray(long[][] frequencies) throws IOException {
		long[][] cleanedFrequencies = removeInvalidFrequencies(frequencies);
		Array array = new Array();
		for (int channelNr = 0; channelNr < cleanedFrequencies.length; channelNr++) {
			Structure frequencyPair = new Structure();
			for (int frequencyType = 0; frequencyType < cleanedFrequencies[channelNr].length; frequencyType++) {
				frequencyPair.addDataType(new Unsigned32(cleanedFrequencies[channelNr][frequencyType]));
			}
			array.addDataType(frequencyPair);
		}
		byte[] berEncodedByteArray = array.getBEREncodedByteArray();
		return new Frequencies(berEncodedByteArray);
	}

	/**
	 * @return
	 */
	private static long[][] removeInvalidFrequencies(final long[][] frequencies) {
		List<long[]> validFrequenciesList = new ArrayList<long[]>();
		if (frequencies == null) {
			throw new IllegalArgumentException("Frequencies.fromLongArray(long[][] frequencies): Argument frequencies cannot be null!");
		} else if (frequencies[0].length != 2) {
			throw new IllegalArgumentException("Frequencies.fromLongArray(long[][] frequencies): Argument frequencies should contain a mark & space field!");
		} else {
			for (int i = 0; i < frequencies.length; i++) {
				if (!containsInvalidFrequency(frequencies[i])) {
					validFrequenciesList.add(frequencies[i]);
				}
			}
		}

		long[][] returnValue = new long[validFrequenciesList.size()][validFrequenciesList.size() > 0 ? validFrequenciesList.get(0).length : 0];
		for (int i = 0; i < validFrequenciesList.size(); i++) {
			for (int j = 0; j < validFrequenciesList.get(i).length; j++) {
				returnValue[i][j] = validFrequenciesList.get(i)[j];
			}
		}

		return returnValue;
	}

	/**
	 * @param frequencyGroup
	 * @return
	 */
	private static boolean containsInvalidFrequency(long[] frequencyGroup) {
		for (int i = 0; i < frequencyGroup.length; i++) {
			if (frequencyGroup[i] == -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param berEncodedData
	 * @param offset
	 * @param level
	 * @throws IOException
	 */
	public Frequencies(byte[] berEncodedData, int offset, int level) throws IOException {
		super(berEncodedData, offset, level);
	}

	/**
	 * @param berEncodedData
	 * @throws IOException
	 */
	public Frequencies(byte[] berEncodedData) throws IOException {
		super(berEncodedData, 0, 0);
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

	/**
	 * @param frequencies
	 * @return 0 if the object equals, -1 if an error occurred, and 1 if not equal
	 */
	public int compareTo(Frequencies frequencies) {
		if (frequencies != null) {
			byte[] other = frequencies.getBEREncodedByteArray();
			byte[] me = getBEREncodedByteArray();
			return Arrays.equals(me, other) ? 0 : 1;
		} else {
			return -1;
		}
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Frequencies) {
			return (compareTo((Frequencies) object) == 0);
		} else {
			return false;
		}
	}

}
