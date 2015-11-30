/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;

import java.io.IOException;
import java.util.Arrays;

/**
 * This class extends an {@link com.energyict.dlms.axrdencoding.Array} to give a fancy toString
 * method when showing PLC channel frequencies.
 * <p/>
 * structure : {
 *     double-long-unsigned : 76800,   // Fs
 *     double-long-unsigned : 72000    // Fm
 * }
 *
 * @author jme
 */
public class Frequencies extends Array implements Comparable<Frequencies> {

    private FrequencyGroup[] frequencyGroups = new FrequencyGroup[0];

	/**
	 * @param berEncodedData
	 * @param offset
	 * @param level
	 * @throws java.io.IOException
	 */
	public Frequencies(byte[] berEncodedData, int offset, int level) throws IOException {
		super(berEncodedData, offset, level);
        frequencyGroups = new FrequencyGroup[nrOfDataTypes()];
        for (int i = 0; i < nrOfDataTypes(); i++) {
            frequencyGroups[i] = new FrequencyGroup(getDataType(i).getBEREncodedByteArray());
	}
    }

	/**
	 * @param berEncodedData
	 * @throws java.io.IOException
	 */
	public Frequencies(byte[] berEncodedData) throws IOException {
        this(berEncodedData, 0, 0);
	}

    public FrequencyGroup[] getFrequencyGroups() {
        return frequencyGroups;
		}

	public int getNumberOfChannels() {
        return frequencyGroups.length;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
        for (int channelIndex = 0; channelIndex < frequencyGroups.length; channelIndex++) {
            sb.append("[").append(channelIndex + 1).append("]=");
            sb.append(frequencyGroups[channelIndex]);
					sb.append("); ");
				}
		return sb.toString();
	}

    public boolean isOldFormat() {
        return frequencyGroups[0].isOldFormat();
    }

	/**
	 * @param frequencies
	 * @return 0 if the object equals, -1 if an error occurred, and 1 if not equal
	 */
	public int compareTo(Frequencies frequencies) {
		if (frequencies != null) {
            byte[] other = (frequencies.isOldFormat() != isOldFormat()) ? Frequencies.toOldFormat(frequencies).getBEREncodedByteArray() : frequencies.getBEREncodedByteArray();
            byte[] me = (frequencies.isOldFormat() != isOldFormat()) ? Frequencies.toOldFormat(this).getBEREncodedByteArray() : getBEREncodedByteArray();
			return Arrays.equals(me, other) ? 0 : 1;
		} else {
			return -1;
		}
	}

    private static AbstractDataType toOldFormat(Frequencies frequencies) {
        if (frequencies.isOldFormat()) {
            return frequencies;
        } else {
            FrequencyGroup[] group = new FrequencyGroup[frequencies.getNumberOfChannels()];
            for (int i = 0; i < group.length; i++) {
                FrequencyGroup frequencyGroup = frequencies.getFrequencyGroups()[i];
                group[i] = FrequencyGroup.createFrequencyGroup(frequencyGroup.getFs(), frequencyGroup.getFm());
            }
            return Frequencies.fromFrequencyGroups(group);
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

    public static Frequencies fromFrequencyGroups(FrequencyGroup[] frequencyGroups) {
        FrequencyGroup[] groups = removeEmptyGroups(frequencyGroups);
        checkFrequencyGroups(groups);
        try {
            Array frequencies = new Array();
            for (int i = 0; i < groups.length; i++) {
                frequencies.addDataType(groups[i]);
}
            return new Frequencies(frequencies.getBEREncodedByteArray());
        } catch (IOException e) {
            // Absorb
        }
        return null;
    }

    private static FrequencyGroup[] removeEmptyGroups(FrequencyGroup[] frequencyGroups) {
        int correct = 0;
        for (int i = 0; i < frequencyGroups.length; i++) {
            FrequencyGroup frequencyGroup = frequencyGroups[i];
            if (frequencyGroup != null) {
                correct++;
            }
        }
        FrequencyGroup[] corrected = new FrequencyGroup[correct];
        int ptr = 0;
        for (int i = 0; i < frequencyGroups.length; i++) {
            FrequencyGroup frequencyGroup = frequencyGroups[i];
            if (frequencyGroup != null) {
                corrected[ptr++] = frequencyGroup;
            }
        }
        return corrected;
    }

    private static void checkFrequencyGroups(FrequencyGroup[] frequencyGroups) {
        if ((frequencyGroups == null) || (frequencyGroups.length == 0)) {
            throw new IllegalArgumentException("Unable to create Frequencies object. You need to provide at least one FrequencyGroup");
        }
        boolean sameFormat = frequencyGroups[0].isOldFormat();
        for (int i = 0; i < frequencyGroups.length; i++) {
            if (frequencyGroups[i].isOldFormat() != sameFormat) {
                throw new IllegalArgumentException("Unable to create Frequencies object. All the FrequencyGroups should have the same format.");
            }
        }
    }

}