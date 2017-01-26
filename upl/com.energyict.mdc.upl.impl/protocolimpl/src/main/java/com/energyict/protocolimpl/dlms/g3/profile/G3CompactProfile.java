package com.energyict.protocolimpl.dlms.g3.profile;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

public class G3CompactProfile {

    private static final int UNSIGNED32_LENGTH = 4;
    private final G3LoadProfileEntry[] entries;

    public G3CompactProfile(final byte[] data) throws IOException {
        this(data, 0);
    }

    public G3CompactProfile(final byte[] data, final int offset) throws IOException {
        int ptr = offset;

        if (data[ptr++] != AxdrType.COMPACT_ARRAY.getTag()) {
            entries = new G3LoadProfileEntry[0];
        } else {
            if (data[ptr++] != AxdrType.STRUCTURE.getTag()) {
                throw new IOException("Unable to parse G3 compact array! Expected Structure tag " +
                        "[" + AxdrType.STRUCTURE.getTag() + "] but received [" + data[ptr - 1] + "] instead.");
            }

            if (data[ptr++] != 1) {
                throw new IOException("Unable to parse G3 compact array! Expected only [1] item in the structure" +
                        " but found [" + data[ptr - 1] + "] instead.");
            }

            if (data[ptr++] != AxdrType.DOUBLE_LONG_UNSIGNED.getTag()) {
                throw new IOException("Unable to parse G3 compact array! Expected Unsigned32 tag [" +
                        AxdrType.DOUBLE_LONG_UNSIGNED.getTag() + "]" + " but received [" + data[ptr - 1] + "] instead.");
            }

            final int length = DLMSUtils.getAXDRLength(data, ptr);
            ptr += DLMSUtils.getAXDRLengthOffset(data, ptr);

            if ((length % UNSIGNED32_LENGTH) != 0) {
                throw new IOException("Not an integer number of unsigned32 data values in the compact array!");
            }

            final int nrOfEntries = length / UNSIGNED32_LENGTH;
            this.entries = new G3LoadProfileEntry[nrOfEntries];
            for (int i = 0; i < nrOfEntries; i++) {
                final Long value = new Long(ProtocolUtils.getLong(data, ptr, UNSIGNED32_LENGTH));
                ptr += UNSIGNED32_LENGTH;
                entries[i] = new G3LoadProfileEntry(value);
            }
        }

    }

    public G3LoadProfileEntry[] getEntries() {
        return entries;
    }

}
